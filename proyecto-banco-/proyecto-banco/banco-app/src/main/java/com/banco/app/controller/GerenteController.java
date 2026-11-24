package com.banco.app.controller;

import com.banco.amortizacion.CalculadoraAmortizacion;
import com.banco.amortizacion.CuotaAmortizacion;
import com.banco.app.data.BancoContext;
import com.banco.app.data.RepositorioDatos;
import com.banco.app.model.Cliente;
import com.banco.app.model.Cuenta;
import com.banco.app.model.Prestamo;
import com.banco.app.model.Rol;
import com.banco.app.model.SaldoInsuficienteException;
import com.banco.app.model.Transaccion;
import com.banco.app.model.Usuario;

import java.util.ArrayList;
import java.util.List;

/** Controlador de la vista del Gerente: ver clientes y préstamos, y cargar pagos. */
public class GerenteController {

    private final CalculadoraAmortizacion calculadora = new CalculadoraAmortizacion();
    private final RepositorioDatos repositorio;

    public GerenteController(RepositorioDatos repositorio) {
        this.repositorio = repositorio;
    }

    /** @throws IllegalStateException si el usuario en sesión no es gerente */
    public List<Cliente> getClientes() {
        exigirGerente();
        return BancoContext.getInstance().getClientes();
    }

    /** @return los préstamos (activos y pagados) de un cliente */
    public List<Prestamo> getPrestamosDe(String idCliente) {
        exigirGerente();
        return BancoContext.getInstance().getPrestamosDe(idCliente);
    }

    /** @return todas las transacciones del cliente, de la más reciente a la más antigua */
    public List<Transaccion> getTransaccionesDe(String idCliente) {
        exigirGerente();
        Cuenta cuenta = BancoContext.getInstance().getCuentaDe(idCliente);
        if (cuenta == null) {
            return new ArrayList<>();
        }
        return cuenta.getUltimasTransacciones(Integer.MAX_VALUE);
    }

    /**
     * Registra el pago de la siguiente cuota: calcula la cuota con la librería, la debita de la
     * cuenta del cliente (PAGO_CUOTA), llama a prestamo.registrarPago() y guarda en el CSV.
     * Si el cliente no tiene saldo, no se modifica nada.
     *
     * @throws SaldoInsuficienteException si el cliente no tiene saldo para la cuota
     * @throws IllegalArgumentException   si el préstamo no existe
     * @throws IllegalStateException      si no es gerente, el préstamo ya está pagado, o el pago
     *                                    quedó registrado pero no se pudo guardar en los CSV
     */
    public void registrarPago(String idPrestamo) throws SaldoInsuficienteException {
        exigirGerente();
        BancoContext ctx = BancoContext.getInstance();

        Prestamo prestamo = ctx.getPrestamo(idPrestamo);
        if (prestamo == null) {
            throw new IllegalArgumentException("No existe el préstamo " + idPrestamo + ".");
        }
        if (!prestamo.isActivo()) {
            throw new IllegalStateException("El préstamo " + idPrestamo + " ya está pagado.");
        }
        Cuenta cuenta = ctx.getCuentaDe(prestamo.getIdCliente());
        if (cuenta == null) {
            throw new IllegalStateException("El cliente del préstamo no tiene una cuenta.");
        }

        // La cuota número N sale de la tabla (la última se ajusta para dejar el saldo en 0.00)
        List<CuotaAmortizacion> tabla = calculadora.generarTabla(
                prestamo.getMontoOriginal(), prestamo.getTasaAnual(), prestamo.getPlazoMeses());
        int numero = prestamo.getCuotasPagadas() + 1;

        // Primero se debita (puede lanzar SaldoInsuficienteException sin haber cambiado nada)
        cuenta.pagarCuota(tabla.get(numero - 1).getCuota(), "Cuota " + numero + " de " + idPrestamo);
        prestamo.registrarPago();

        if (!repositorio.guardar()) {
            throw new IllegalStateException("El pago quedó registrado, pero no se pudo guardar "
                    + "en los archivos CSV. Usa el botón Guardar para reintentar.");
        }
    }

    private void exigirGerente() {
        Usuario u = BancoContext.getInstance().getUsuarioActual();
        if (u == null || u.getRol() != Rol.GERENTE) {
            throw new IllegalStateException("Solo un gerente puede realizar esta operación.");
        }
    }
}

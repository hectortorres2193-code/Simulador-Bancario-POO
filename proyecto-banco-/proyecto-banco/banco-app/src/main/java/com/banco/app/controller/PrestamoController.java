package com.banco.app.controller;

import com.banco.amortizacion.CalculadoraAmortizacion;
import com.banco.amortizacion.CuotaAmortizacion;
import com.banco.app.data.BancoContext;
import com.banco.app.data.RepositorioDatos;
import com.banco.app.model.Cuenta;
import com.banco.app.model.Prestamo;
import com.banco.app.model.Rol;
import com.banco.app.model.Usuario;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/** Controlador del módulo de préstamos. Consume la librería de amortización. */
public class PrestamoController {

    /** Tasa anual fija del banco (0.12 = 12 %). La librería la recibe por parámetro, no la conoce. */
    private static final BigDecimal TASA_ANUAL = new BigDecimal("0.12");

    private final CalculadoraAmortizacion calculadora = new CalculadoraAmortizacion();
    private final RepositorioDatos repositorio;

    public PrestamoController(RepositorioDatos repositorio) {
        this.repositorio = repositorio;
    }

    /** @return la tasa anual que aplica el banco (para mostrarla en pantalla) */
    public BigDecimal getTasaAnual() {
        return TASA_ANUAL;
    }

    /**
     * Proyecta la tabla de amortización SIN registrar nada (botón "Simular").
     *
     * @throws IllegalArgumentException si el monto o el plazo son inválidos
     */
    public List<CuotaAmortizacion> simularPrestamo(BigDecimal monto, int meses) {
        return calculadora.generarTabla(monto, TASA_ANUAL, meses);
    }

    /**
     * Botón "Aceptar Préstamo". Sigue el diagrama de secuencia (docs/diagrama-secuencia.png):
     * validar -> librería -> crear Prestamo -> cuenta.acreditarPrestamo -> agregarPrestamo -> guardar CSV.
     *
     * @return la tabla de amortización del préstamo aceptado
     * @throws IllegalArgumentException si monto <= 0 o meses <= 0
     * @throws IllegalStateException    si no hay un cliente en sesión, o si el préstamo quedó
     *                                  en memoria pero no se pudo guardar en los CSV
     */
    public List<CuotaAmortizacion> aceptarPrestamo(BigDecimal monto, int meses) {
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que 0.");
        }
        if (meses <= 0) {
            throw new IllegalArgumentException("El plazo debe ser de al menos 1 mes.");
        }

        BancoContext ctx = BancoContext.getInstance();
        Usuario usuario = ctx.getUsuarioActual();
        if (usuario == null || usuario.getRol() != Rol.CLIENTE) {
            throw new IllegalStateException("Solo un cliente con sesión iniciada puede pedir un préstamo.");
        }
        Cuenta cuenta = ctx.getCuentaDe(usuario.getId());
        if (cuenta == null) {
            throw new IllegalStateException("El cliente no tiene una cuenta.");
        }

        BigDecimal capital = monto.setScale(2, RoundingMode.HALF_UP);
        List<CuotaAmortizacion> tabla = calculadora.generarTabla(capital, TASA_ANUAL, meses);

        Prestamo prestamo = new Prestamo(ctx.siguienteId("P"), usuario.getId(),
                cuenta.getIdCuenta(), capital, TASA_ANUAL, meses);
        cuenta.acreditarPrestamo(capital, "Préstamo " + prestamo.getIdPrestamo());
        ctx.agregarPrestamo(prestamo);

        if (!repositorio.guardar()) {
            throw new IllegalStateException("El préstamo quedó registrado, pero no se pudo guardar "
                    + "en los archivos CSV. Usa el botón Guardar para reintentar.");
        }
        return tabla;
    }

    /** @return los préstamos ACTIVOS del cliente en sesión */
    public List<Prestamo> getPrestamosActivos() {
        BancoContext ctx = BancoContext.getInstance();
        Usuario usuario = ctx.getUsuarioActual();
        if (usuario == null) {
            throw new IllegalStateException("No hay una sesión iniciada.");
        }
        List<Prestamo> activos = new ArrayList<>();
        for (Prestamo p : ctx.getPrestamosDe(usuario.getId())) {
            if (p.isActivo()) {
                activos.add(p);
            }
        }
        return activos;
    }
}

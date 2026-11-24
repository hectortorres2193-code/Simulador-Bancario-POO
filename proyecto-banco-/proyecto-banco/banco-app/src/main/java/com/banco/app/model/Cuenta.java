package com.banco.app.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Cuenta bancaria de un cliente, con su saldo y su historial de transacciones.
 * Todos los montos se manejan con 2 decimales (HALF_UP). No permite sobregiros.
 */
public class Cuenta {

    private final String idCuenta;
    private final String idCliente;
    private BigDecimal saldo;
    private final List<Transaccion> transacciones;

    public Cuenta(String idCuenta, String idCliente, BigDecimal saldo) {
        this.idCuenta = idCuenta;
        this.idCliente = idCliente;
        this.saldo = (saldo == null ? BigDecimal.ZERO : saldo).setScale(2, RoundingMode.HALF_UP);
        this.transacciones = new ArrayList<>();
    }

    /**
     * Suma el monto al saldo y registra una transacción DEPOSITO.
     *
     * @throws IllegalArgumentException si el monto es nulo o <= 0
     */
    public void depositar(BigDecimal monto) {
        registrarIngreso(monto, TipoTransaccion.DEPOSITO, "Depósito en caja");
    }

    /**
     * Resta el monto al saldo y registra una transacción RETIRO.
     *
     * @throws SaldoInsuficienteException si el monto supera el saldo (no se permiten sobregiros)
     * @throws IllegalArgumentException   si el monto es nulo o <= 0
     */
    public void retirar(BigDecimal monto) throws SaldoInsuficienteException {
        registrarEgreso(monto, TipoTransaccion.RETIRO, "Retiro en caja");
    }

    /** Suma el préstamo al saldo y registra una transacción DESEMBOLSO_PRESTAMO. */
    public void acreditarPrestamo(BigDecimal monto) {
        acreditarPrestamo(monto, "Préstamo acreditado");
    }

    /** Igual que {@link #acreditarPrestamo(BigDecimal)}, con una descripción para el historial. */
    public void acreditarPrestamo(BigDecimal monto, String descripcion) {
        registrarIngreso(monto, TipoTransaccion.DESEMBOLSO_PRESTAMO, descripcion);
    }

    /**
     * Debita la cuota de un préstamo y registra una transacción PAGO_CUOTA.
     *
     * @throws SaldoInsuficienteException si el monto supera el saldo
     */
    public void pagarCuota(BigDecimal monto) throws SaldoInsuficienteException {
        pagarCuota(monto, "Pago de cuota");
    }

    /** Igual que {@link #pagarCuota(BigDecimal)}, con una descripción para el historial. */
    public void pagarCuota(BigDecimal monto, String descripcion) throws SaldoInsuficienteException {
        registrarEgreso(monto, TipoTransaccion.PAGO_CUOTA, descripcion);
    }

    /** Agrega una transacción al historial sin modificar el saldo (la usa el repositorio al cargar el CSV). */
    public void agregarTransaccion(Transaccion t) {
        if (t == null) {
            throw new IllegalArgumentException("La transacción no puede ser nula.");
        }
        transacciones.add(t);
    }

    /** @return las últimas n transacciones, de la más reciente a la más antigua. */
    public List<Transaccion> getUltimasTransacciones(int n) {
        if (n <= 0) {
            return new ArrayList<>();
        }
        List<Transaccion> copia = new ArrayList<>(transacciones);
        Collections.reverse(copia); // si hay empate de fecha, gana la registrada después
        copia.sort(Comparator.comparing(Transaccion::getFecha).reversed());
        return new ArrayList<>(copia.subList(0, Math.min(n, copia.size())));
    }

    public BigDecimal getSaldo() { return saldo; }

    public String getIdCuenta() { return idCuenta; }

    public String getIdCliente() { return idCliente; }

    /** Historial completo, en orden de registro (solo lectura; la usa el repositorio para guardar). */
    public List<Transaccion> getTransacciones() { return Collections.unmodifiableList(transacciones); }

    // ---------------- auxiliares privados ----------------

    private void registrarIngreso(BigDecimal monto, TipoTransaccion tipo, String descripcion) {
        BigDecimal m = normalizar(monto);
        saldo = saldo.add(m);
        registrar(tipo, m, descripcion);
    }

    private void registrarEgreso(BigDecimal monto, TipoTransaccion tipo, String descripcion)
            throws SaldoInsuficienteException {
        BigDecimal m = normalizar(monto);
        if (saldo.compareTo(m) < 0) {
            throw new SaldoInsuficienteException(
                    "Saldo insuficiente: disponible $" + saldo.toPlainString()
                            + ", se intentó $" + m.toPlainString() + ".");
        }
        saldo = saldo.subtract(m);
        registrar(tipo, m, descripcion);
    }

    private void registrar(TipoTransaccion tipo, BigDecimal monto, String descripcion) {
        String id = "T" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        LocalDateTime ahora = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        transacciones.add(new Transaccion(id, idCuenta, tipo, monto, ahora, descripcion));
    }

    /** Valida que el monto sea > 0 y lo redondea a 2 decimales. */
    private BigDecimal normalizar(BigDecimal monto) {
        if (monto == null || monto.setScale(2, RoundingMode.HALF_UP).signum() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que 0.");
        }
        return monto.setScale(2, RoundingMode.HALF_UP);
    }
}

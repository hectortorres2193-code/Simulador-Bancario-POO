package com.banco.app.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Préstamo (deuda) de un cliente. La tabla de amortización la calcula la librería, no esta clase. */
public class Prestamo {

    private final String idPrestamo;
    private final String idCliente;
    private final String idCuenta;
    private final BigDecimal montoOriginal;
    private final BigDecimal tasaAnual;
    private final int plazoMeses;
    private int cuotasPagadas;
    private EstadoPrestamo estado;
    private final LocalDate fechaInicio;

    /** Constructor para un préstamo NUEVO: 0 cuotas pagadas, estado ACTIVO, fecha de hoy. */
    public Prestamo(String idPrestamo, String idCliente, String idCuenta,
                    BigDecimal monto, BigDecimal tasaAnual, int plazoMeses) {
        this(idPrestamo, idCliente, idCuenta, monto, tasaAnual, plazoMeses,
                0, EstadoPrestamo.ACTIVO, LocalDate.now());
    }

    /** Constructor completo, usado por el repositorio al leer prestamos.csv. */
    public Prestamo(String idPrestamo, String idCliente, String idCuenta,
                    BigDecimal monto, BigDecimal tasaAnual, int plazoMeses,
                    int cuotasPagadas, EstadoPrestamo estado, LocalDate fechaInicio) {
        this.idPrestamo = idPrestamo;
        this.idCliente = idCliente;
        this.idCuenta = idCuenta;
        this.montoOriginal = monto;
        this.tasaAnual = tasaAnual;
        this.plazoMeses = plazoMeses;
        this.cuotasPagadas = cuotasPagadas;
        this.estado = estado;
        this.fechaInicio = fechaInicio;
    }

    /** Registra el pago de una cuota: suma 1 a cuotasPagadas y pasa a PAGADO si ya no quedan pendientes. */
    public void registrarPago() {
        if (!isActivo()) {
            throw new IllegalStateException("El préstamo " + idPrestamo + " ya está pagado.");
        }
        cuotasPagadas++;
        if (cuotasPagadas >= plazoMeses) {
            estado = EstadoPrestamo.PAGADO;
        }
    }

    /** @return plazoMeses - cuotasPagadas */
    public int getCuotasPendientes() {
        return Math.max(0, plazoMeses - cuotasPagadas);
    }

    /** @return true si el estado es ACTIVO */
    public boolean isActivo() {
        return estado == EstadoPrestamo.ACTIVO;
    }

    public String getIdPrestamo() { return idPrestamo; }

    public String getIdCliente() { return idCliente; }

    public String getIdCuenta() { return idCuenta; }

    public BigDecimal getMontoOriginal() { return montoOriginal; }

    public BigDecimal getTasaAnual() { return tasaAnual; }

    public int getPlazoMeses() { return plazoMeses; }

    public int getCuotasPagadas() { return cuotasPagadas; }

    public EstadoPrestamo getEstado() { return estado; }

    public LocalDate getFechaInicio() { return fechaInicio; }
}

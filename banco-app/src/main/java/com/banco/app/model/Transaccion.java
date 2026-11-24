package com.banco.app.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Movimiento de una cuenta (depósito, retiro, desembolso o pago de cuota). */
public class Transaccion {

    private final String idTransaccion;
    private final String idCuenta;
    private final TipoTransaccion tipo;
    private final BigDecimal monto;
    private final LocalDateTime fecha;
    private final String descripcion;

    public Transaccion(String idTransaccion, String idCuenta, TipoTransaccion tipo,
                       BigDecimal monto, LocalDateTime fecha, String descripcion) {
        this.idTransaccion = idTransaccion;
        this.idCuenta = idCuenta;
        this.tipo = tipo;
        this.monto = monto;
        this.fecha = fecha;
        this.descripcion = descripcion;
    }

    public String getIdTransaccion() { return idTransaccion; }

    public String getIdCuenta() { return idCuenta; }

    public TipoTransaccion getTipo() { return tipo; }

    public BigDecimal getMonto() { return monto; }

    public LocalDateTime getFecha() { return fecha; }

    public String getDescripcion() { return descripcion; }
}

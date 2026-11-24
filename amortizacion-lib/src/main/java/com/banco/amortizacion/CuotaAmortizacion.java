package com.banco.amortizacion;

import java.math.BigDecimal;

/**
 * Una fila de la tabla de amortización. Es inmutable.
 */
public class CuotaAmortizacion {

    private final int numero;
    private final BigDecimal cuota;
    private final BigDecimal interes;
    private final BigDecimal capital;
    private final BigDecimal saldoRestante;

    public CuotaAmortizacion(int numero, BigDecimal cuota, BigDecimal interes,
                             BigDecimal capital, BigDecimal saldoRestante) {
        this.numero = numero;
        this.cuota = cuota;
        this.interes = interes;
        this.capital = capital;
        this.saldoRestante = saldoRestante;
    }

    public int getNumero() { return numero; }

    public BigDecimal getCuota() { return cuota; }

    public BigDecimal getInteres() { return interes; }

    public BigDecimal getCapital() { return capital; }

    public BigDecimal getSaldoRestante() { return saldoRestante; }
}

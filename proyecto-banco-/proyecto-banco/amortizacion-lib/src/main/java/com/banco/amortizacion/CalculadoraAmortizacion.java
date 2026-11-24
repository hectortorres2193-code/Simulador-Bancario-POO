package com.banco.amortizacion;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Motor de amortización (sistema francés: cuota mensual fija).
 *
 * <p><b>Librería independiente:</b> no importa {@code java.awt}, {@code javax.swing}
 * ni {@code javafx}, ni conoce clases del banco (por ejemplo, Cliente). Es un
 * resolvedor matemático genérico.</p>
 *
 * <p>Fórmula: {@code cuota = P*r / (1 - (1+r)^-n)}, con {@code r = tasaAnual / 12}
 * y {@code n = meses}. Todos los montos se redondean a 2 decimales (HALF_UP).</p>
 */
public class CalculadoraAmortizacion {

    private static final MathContext MC = MathContext.DECIMAL128;
    private static final BigDecimal MESES_POR_ANIO = BigDecimal.valueOf(12);

    /**
     * Calcula la cuota mensual fija.
     *
     * @param monto     capital solicitado (mayor que 0)
     * @param tasaAnual tasa anual como fracción (0.12 equivale a 12 %)
     * @param meses     plazo en meses (mayor que 0)
     * @return cuota mensual con 2 decimales
     * @throws IllegalArgumentException si algún dato es inválido
     */
    public BigDecimal calcularCuotaMensual(BigDecimal monto, BigDecimal tasaAnual, int meses) {
        validar(monto, tasaAnual, meses);
        BigDecimal capital = redondear(monto);
        BigDecimal r = tasaMensual(tasaAnual);

        if (r.signum() == 0) {
            // Sin interés: el capital se reparte en partes iguales.
            return capital.divide(BigDecimal.valueOf(meses), 2, RoundingMode.HALF_UP);
        }
        BigDecimal factor = BigDecimal.ONE.add(r).pow(meses, MC);                    // (1+r)^n
        BigDecimal denominador = BigDecimal.ONE.subtract(BigDecimal.ONE.divide(factor, MC)); // 1-(1+r)^-n
        return capital.multiply(r, MC).divide(denominador, MC).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Genera la tabla de amortización completa (una fila por mes).
     * La última cuota se ajusta para que el saldo final sea exactamente 0.00.
     *
     * @param monto     capital solicitado
     * @param tasaAnual tasa anual como fracción
     * @param meses     plazo en meses
     * @return lista (no modificable) de cuotas con número, cuota, interés, capital y saldo restante
     * @throws IllegalArgumentException si algún dato es inválido
     */
    public List<CuotaAmortizacion> generarTabla(BigDecimal monto, BigDecimal tasaAnual, int meses) {
        BigDecimal cuotaFija = calcularCuotaMensual(monto, tasaAnual, meses); // también valida
        BigDecimal r = tasaMensual(tasaAnual);
        BigDecimal saldo = redondear(monto);
        List<CuotaAmortizacion> tabla = new ArrayList<>(meses);

        for (int i = 1; i <= meses; i++) {
            BigDecimal interes = redondear(saldo.multiply(r, MC));
            BigDecimal cuota = cuotaFija;
            BigDecimal capital = cuota.subtract(interes);

            if (i == meses || capital.compareTo(saldo) > 0) {
                capital = saldo;                 // última cuota: liquida lo que quede
                cuota = capital.add(interes);
            }
            saldo = saldo.subtract(capital);
            tabla.add(new CuotaAmortizacion(i, cuota, interes, capital, saldo));
        }
        return Collections.unmodifiableList(tabla);
    }

    /**
     * Suma los intereses de toda la tabla.
     *
     * @param tabla tabla generada por {@link #generarTabla}
     * @return total de intereses a pagar
     * @throws IllegalArgumentException si la tabla es nula
     */
    public BigDecimal calcularTotalIntereses(List<CuotaAmortizacion> tabla) {
        if (tabla == null) {
            throw new IllegalArgumentException("La tabla de amortización no puede ser nula.");
        }
        BigDecimal total = BigDecimal.ZERO;
        for (CuotaAmortizacion fila : tabla) {
            total = total.add(fila.getInteres());
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Valida los datos de entrada.
     *
     * @throws IllegalArgumentException si monto <= 0, tasa < 0 o meses <= 0
     */
    private void validar(BigDecimal monto, BigDecimal tasaAnual, int meses) {
        if (monto == null || redondear(monto).signum() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que 0.");
        }
        if (tasaAnual == null || tasaAnual.signum() < 0) {
            throw new IllegalArgumentException("La tasa anual no puede ser negativa.");
        }
        if (meses <= 0) {
            throw new IllegalArgumentException("El plazo debe ser de al menos 1 mes.");
        }
    }

    private BigDecimal tasaMensual(BigDecimal tasaAnual) {
        return tasaAnual.divide(MESES_POR_ANIO, MC);
    }

    private BigDecimal redondear(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}

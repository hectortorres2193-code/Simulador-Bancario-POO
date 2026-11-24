package com.banco.amortizacion;

import java.math.BigDecimal;
import java.util.List;

/**
 * Prueba en consola (sin JUnit). Ejecutar su main desde el IDE.
 * Imprime OK / FALLO por cada verificación y termina con código 1 si algo falla.
 */
public class PruebaAmortizacion {

    private static int fallos = 0;

    private static void check(String nombre, boolean ok) {
        System.out.println((ok ? "  OK    " : "  FALLO ") + nombre);
        if (!ok) fallos++;
    }

    private static boolean igual(BigDecimal a, String b) {
        return a.compareTo(new BigDecimal(b)) == 0;
    }

    public static void main(String[] args) {
        // Salida en UTF-8 para que los acentos se vean bien en la consola de NetBeans
        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        System.setErr(new java.io.PrintStream(System.err, true, java.nio.charset.StandardCharsets.UTF_8));
        CalculadoraAmortizacion calc = new CalculadoraAmortizacion();

        System.out.println("Cuota y tabla (5000 al 12 % a 12 meses)");
        BigDecimal cuota = calc.calcularCuotaMensual(new BigDecimal("5000"), new BigDecimal("0.12"), 12);
        check("cuota = 444.24", igual(cuota, "444.24"));

        List<CuotaAmortizacion> t = calc.generarTabla(new BigDecimal("5000"), new BigDecimal("0.12"), 12);
        check("12 filas", t.size() == 12);
        check("fila 1: interés 50.00, capital 394.24, saldo 4605.76",
                igual(t.get(0).getInteres(), "50.00") && igual(t.get(0).getCapital(), "394.24")
                        && igual(t.get(0).getSaldoRestante(), "4605.76"));
        check("fila 2: interés 46.06, saldo 4207.58",
                igual(t.get(1).getInteres(), "46.06") && igual(t.get(1).getSaldoRestante(), "4207.58"));
        check("saldo final = 0.00", igual(t.get(11).getSaldoRestante(), "0.00"));
        BigDecimal sumaCapital = BigDecimal.ZERO;
        for (CuotaAmortizacion f : t) sumaCapital = sumaCapital.add(f.getCapital());
        check("la suma del capital = monto", igual(sumaCapital, "5000.00"));
        BigDecimal sumaCuotas = BigDecimal.ZERO;
        for (CuotaAmortizacion f : t) sumaCuotas = sumaCuotas.add(f.getCuota());
        check("total de intereses = suma de cuotas - monto",
                calc.calcularTotalIntereses(t).compareTo(sumaCuotas.subtract(new BigDecimal("5000"))) == 0);

        System.out.println("Otros casos");
        check("tasa 0: 1200 a 12 meses = 100.00",
                igual(calc.calcularCuotaMensual(new BigDecimal("1200"), BigDecimal.ZERO, 12), "100.00"));
        check("10000 al 18 % a 24 meses = 499.24 (fórmula en double)", compararConDouble(10000, 0.18, 24, calc));
        check("1 mes: cuota = capital + interés",
                igual(calc.generarTabla(new BigDecimal("1000"), new BigDecimal("0.12"), 1).get(0).getCuota(), "1010.00"));
        check("360 meses termina en saldo 0.00",
                igual(calc.generarTabla(new BigDecimal("250000"), new BigDecimal("0.09"), 360).get(359).getSaldoRestante(), "0.00"));

        System.out.println("Validaciones (deben lanzar IllegalArgumentException)");
        check("monto 0", lanza(calc, BigDecimal.ZERO, "0.12", 12));
        check("monto null", lanza(calc, null, "0.12", 12));
        check("tasa negativa", lanza(calc, BigDecimal.TEN, "-0.1", 12));
        check("meses 0", lanza(calc, BigDecimal.TEN, "0.12", 0));

        System.out.println(fallos == 0 ? "\nTODO OK" : "\nFALLARON " + fallos);
        System.exit(fallos == 0 ? 0 : 1);
    }

    private static boolean lanza(CalculadoraAmortizacion c, BigDecimal monto, String tasa, int meses) {
        try {
            c.generarTabla(monto, new BigDecimal(tasa), meses);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    private static boolean compararConDouble(double p, double tasaAnual, int n, CalculadoraAmortizacion calc) {
        double r = tasaAnual / 12;
        double esperada = p * r / (1 - Math.pow(1 + r, -n));
        BigDecimal obtenida = calc.calcularCuotaMensual(BigDecimal.valueOf(p), BigDecimal.valueOf(tasaAnual), n);
        return Math.abs(obtenida.doubleValue() - esperada) < 0.01;
    }
}

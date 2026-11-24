package com.banco.app;

import com.banco.amortizacion.CalculadoraAmortizacion;
import com.banco.amortizacion.CuotaAmortizacion;
import com.banco.app.controller.CuentaController;
import com.banco.app.controller.GerenteController;
import com.banco.app.controller.LoginController;
import com.banco.app.controller.PersistenciaController;
import com.banco.app.data.BancoContext;
import com.banco.app.data.CsvRepositorio;
import com.banco.app.data.RepositorioDatos;
import com.banco.app.model.Cliente;
import com.banco.app.model.Cuenta;
import com.banco.app.model.Prestamo;
import com.banco.app.model.Rol;
import com.banco.app.model.TipoTransaccion;
import com.banco.app.model.Transaccion;
import com.banco.app.model.Usuario;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Programa de prueba en consola: carga los CSV de ejemplo (carpeta datos-prueba/) y muestra los datos
 * de cada cliente y del gerente usando los mismos controladores que usará la interfaz.
 *
 * <p>Solo LEE: no guarda ni modifica ningún archivo. Se puede indicar otra carpeta como argumento.</p>
 */
public class MainPrueba {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static void main(String[] args) {
        // Salida en UTF-8 para que los acentos se vean bien en la consola de NetBeans
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));

        String carpeta = localizarCarpetaDatos(args);
        RepositorioDatos repositorio = new CsvRepositorio(carpeta);
        PersistenciaController persistencia = new PersistenciaController(repositorio);
        LoginController login = new LoginController();
        CuentaController cuenta = new CuentaController();
        GerenteController gerente = new GerenteController(repositorio);
        BancoContext ctx = BancoContext.getInstance();
        CalculadoraAmortizacion calculadora = new CalculadoraAmortizacion();

        persistencia.cargarAlIniciar();

        System.out.println("==============================================================");
        System.out.println(" DATOS DE EJEMPLO  (carpeta: " + carpeta + ")");
        System.out.println("==============================================================");
        System.out.println("Usuarios: " + ctx.getUsuarios().size() + "  (" + ctx.getClientes().size()
                + " clientes)   Cuentas: " + ctx.getCuentas().size() + "   Préstamos: " + ctx.getPrestamos().size());

        // ---------------- Clientes ----------------
        for (Cliente c : ctx.getClientes()) {
            login.iniciarSesion(c.getUsername(), c.getPassword()); // igual que lo hará la pantalla de login

            System.out.println();
            System.out.println("--------------------------------------------------------------");
            System.out.println(" CLIENTE: " + cuenta.getNombreCliente() + "   (usuario: " + c.getUsername()
                    + ", id: " + c.getId() + ", rol: " + login.getRolActual() + ")");
            System.out.println("--------------------------------------------------------------");
            Cuenta cta = ctx.getCuentaDe(c.getId());
            System.out.println(" Cuenta " + cta.getIdCuenta() + "     Saldo: " + dinero(cuenta.getSaldo()));

            System.out.println(" Últimas transacciones (la más reciente primero):");
            List<Transaccion> ultimas = cuenta.getUltimasTransacciones(5);
            for (Transaccion t : ultimas) {
                System.out.printf("   %s  %-20s %12s  %s%n", t.getFecha().format(FECHA), t.getTipo(),
                        conSigno(t), t.getDescripcion());
            }
            if (cta.getTransacciones().size() > ultimas.size()) {
                System.out.println("   (+" + (cta.getTransacciones().size() - ultimas.size()) + " más antiguas)");
            }

            List<Prestamo> prestamos = ctx.getPrestamosDe(c.getId());
            if (prestamos.isEmpty()) {
                System.out.println(" Préstamos: ninguno");
            }
            for (Prestamo p : prestamos) {
                BigDecimal tasaPct = p.getTasaAnual().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
                System.out.println(" Préstamo " + p.getIdPrestamo() + ": " + dinero(p.getMontoOriginal()) + " a "
                        + p.getPlazoMeses() + " meses, " + tasaPct + " % anual, inicio " + p.getFechaInicio());
                System.out.println("   Estado: " + p.getEstado() + "   Cuotas pagadas: " + p.getCuotasPagadas()
                        + " de " + p.getPlazoMeses() + "   Pendientes: " + p.getCuotasPendientes());

                // Tabla de amortización calculada por la librería independiente
                List<CuotaAmortizacion> tabla = calculadora.generarTabla(
                        p.getMontoOriginal(), p.getTasaAnual(), p.getPlazoMeses());
                System.out.println("   Tabla de amortización (calculada por amortizacion-lib):");
                System.out.printf("   %3s %11s %10s %11s %12s   %s%n", "#", "Cuota", "Interés", "Capital", "Saldo", "Estado");
                for (CuotaAmortizacion f : tabla) {
                    System.out.printf("   %3d %11s %10s %11s %12s   %s%n", f.getNumero(), f.getCuota().toPlainString(),
                            f.getInteres().toPlainString(), f.getCapital().toPlainString(),
                            f.getSaldoRestante().toPlainString(),
                            f.getNumero() <= p.getCuotasPagadas() ? "pagada" : "pendiente");
                }
                System.out.println("   Total de intereses: " + dinero(calculadora.calcularTotalIntereses(tabla)));
            }
            login.cerrarSesion();
        }

        // ---------------- Gerente ----------------
        for (Usuario u : ctx.getUsuarios()) {
            if (u.getRol() != Rol.GERENTE) {
                continue;
            }
            login.iniciarSesion(u.getUsername(), u.getPassword());
            System.out.println();
            System.out.println("--------------------------------------------------------------");
            System.out.println(" GERENTE: " + u.getNombre() + "   (usuario: " + u.getUsername() + ", rol: "
                    + login.getRolActual() + ")");
            System.out.println("--------------------------------------------------------------");
            System.out.println(" Clientes que puede ver:");
            for (Cliente c : gerente.getClientes()) {
                int activos = 0;
                for (Prestamo p : gerente.getPrestamosDe(c.getId())) {
                    if (p.isActivo()) activos++;
                }
                System.out.printf("   %-16s saldo %12s   transacciones: %d   préstamos activos: %d%n",
                        c.getNombre(), dinero(ctx.getCuentaDe(c.getId()).getSaldo()),
                        gerente.getTransaccionesDe(c.getId()).size(), activos);
            }
            login.cerrarSesion();
        }

        System.out.println();
        System.out.println("Fin. Este programa solo lee: no modificó ningún archivo.");
    }

    /** Usa el argumento si lo hay; si no, busca datos-prueba/ desde la raíz o desde banco-app/. */
    private static String localizarCarpetaDatos(String[] args) {
        if (args.length > 0) {
            return args[0];
        }
        for (String ruta : new String[]{"datos-prueba", "../datos-prueba", "datos"}) {
            if (Files.isDirectory(Paths.get(ruta))) {
                return ruta;
            }
        }
        return "datos-prueba";
    }

    private static String dinero(BigDecimal valor) {
        return String.format(Locale.US, "$ %,.2f", valor);
    }

    /** Depósitos y préstamos suman (+); retiros y pagos de cuota restan (-). */
    private static String conSigno(Transaccion t) {
        boolean suma = t.getTipo() == TipoTransaccion.DEPOSITO || t.getTipo() == TipoTransaccion.DESEMBOLSO_PRESTAMO;
        return (suma ? "+ " : "- ") + String.format(Locale.US, "%,.2f", t.getMonto());
    }
}

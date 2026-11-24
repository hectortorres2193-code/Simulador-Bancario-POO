package com.banco.app;

import com.banco.amortizacion.CuotaAmortizacion;
import com.banco.app.controller.CuentaController;
import com.banco.app.controller.GerenteController;
import com.banco.app.controller.LoginController;
import com.banco.app.controller.PersistenciaController;
import com.banco.app.controller.PrestamoController;
import com.banco.app.data.BancoContext;
import com.banco.app.data.CsvRepositorio;
import com.banco.app.model.EstadoPrestamo;
import com.banco.app.model.Prestamo;
import com.banco.app.model.Rol;
import com.banco.app.model.SaldoInsuficienteException;
import com.banco.app.model.TipoTransaccion;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Prueba en consola (sin JUnit) de TODA la lógica de negocio + persistencia, sin interfaz.
 * Ejecutar su main desde la raíz del proyecto. Trabaja sobre una copia temporal de datos-prueba/.
 */
public class PruebaNegocio {

    private static int fallos = 0;

    private static void check(String nombre, boolean ok) {
        System.out.println((ok ? "  OK    " : "  FALLO ") + nombre);
        if (!ok) fallos++;
    }

    private static boolean igual(BigDecimal a, String b) {
        return a.compareTo(new BigDecimal(b)) == 0;
    }

    public static void main(String[] args) throws Exception {
        // Salida en UTF-8 para que los acentos se vean bien en la consola de NetBeans
        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        System.setErr(new java.io.PrintStream(System.err, true, java.nio.charset.StandardCharsets.UTF_8));
        // Funciona ejecutando desde la raíz del proyecto o desde la carpeta banco-app/ (NetBeans)
        Path origen = Files.isDirectory(Paths.get("datos-prueba")) ? Paths.get("datos-prueba") : Paths.get("..", "datos-prueba");
        Path tmp = Files.createTempDirectory("banco-prueba");
        for (String f : new String[]{"usuarios.csv", "cuentas.csv", "transacciones.csv", "prestamos.csv"}) {
            Files.copy(origen.resolve(f), tmp.resolve(f), StandardCopyOption.REPLACE_EXISTING);
        }

        CsvRepositorio repo = new CsvRepositorio(tmp.toString());
        PersistenciaController persistencia = new PersistenciaController(repo);
        LoginController login = new LoginController();
        CuentaController cuenta = new CuentaController();
        PrestamoController prestamo = new PrestamoController(repo);
        GerenteController gerente = new GerenteController(repo);
        BancoContext ctx = BancoContext.getInstance();

        System.out.println("1. Carga desde CSV");
        persistencia.cargarAlIniciar();
        check("Singleton: misma instancia", ctx == BancoContext.getInstance());
        check("4 usuarios, 3 cuentas, 1 préstamo",
                ctx.getUsuarios().size() == 4 && ctx.getCuentas().size() == 3 && ctx.getPrestamos().size() == 1);
        check("Luis tiene 6 transacciones", ctx.getCuentaDe("U002").getTransacciones().size() == 6);
        check("acentos bien leídos (UTF-8)", ctx.buscarUsuario("luis").getNombre().equals("Luis Gómez"));

        System.out.println("2. Login");
        check("contraseña incorrecta -> false", !login.iniciarSesion("ana", "mal"));
        check("usuario inexistente -> false", !login.iniciarSesion("nadie", "1234"));
        check("ANA / 1234 -> true (usuario sin distinguir mayúsculas)", login.iniciarSesion("ANA", "1234"));
        check("rol CLIENTE", login.getRolActual() == Rol.CLIENTE);
        check("nombre Ana Torres", cuenta.getNombreCliente().equals("Ana Torres"));

        System.out.println("3. Caja (depósitos y retiros)");
        check("saldo inicial 1500.00", igual(cuenta.getSaldo(), "1500.00"));
        cuenta.depositar(new BigDecimal("250.50"));
        check("depositar 250.50 -> 1750.50", igual(cuenta.getSaldo(), "1750.50"));
        try { cuenta.retirar(new BigDecimal("100")); } catch (SaldoInsuficienteException e) { }
        check("retirar 100 -> 1650.50", igual(cuenta.getSaldo(), "1650.50"));
        boolean sobregiro = false;
        try { cuenta.retirar(new BigDecimal("99999")); } catch (SaldoInsuficienteException e) { sobregiro = true; }
        check("retirar 99999 -> SaldoInsuficienteException", sobregiro);
        check("el saldo no cambió tras el sobregiro", igual(cuenta.getSaldo(), "1650.50"));
        boolean invalido = false;
        try { cuenta.depositar(new BigDecimal("-5")); } catch (IllegalArgumentException e) { invalido = true; }
        check("depositar -5 -> IllegalArgumentException", invalido);
        check("última transacción = RETIRO", cuenta.getUltimasTransacciones(1).get(0).getTipo() == TipoTransaccion.RETIRO);

        System.out.println("4. Préstamos");
        List<CuotaAmortizacion> sim = prestamo.simularPrestamo(new BigDecimal("5000"), 12);
        check("simular: 12 cuotas de 444.24", sim.size() == 12 && igual(sim.get(0).getCuota(), "444.24"));
        check("simular no registra nada", ctx.getPrestamos().size() == 1);
        List<CuotaAmortizacion> tabla = prestamo.aceptarPrestamo(new BigDecimal("3000"), 6);
        check("aceptar 3000 a 6 meses: tabla de 6 filas, saldo final 0", tabla.size() == 6
                && igual(tabla.get(5).getSaldoRestante(), "0.00"));
        check("el dinero se suma al saldo: 4650.50", igual(cuenta.getSaldo(), "4650.50"));
        check("última transacción = DESEMBOLSO_PRESTAMO",
                cuenta.getUltimasTransacciones(1).get(0).getTipo() == TipoTransaccion.DESEMBOLSO_PRESTAMO);
        check("1 préstamo activo para Ana (P002)", prestamo.getPrestamosActivos().size() == 1
                && prestamo.getPrestamosActivos().get(0).getIdPrestamo().equals("P002"));
        boolean plazoMalo = false;
        try { prestamo.aceptarPrestamo(new BigDecimal("1000"), 0); } catch (IllegalArgumentException e) { plazoMalo = true; }
        check("plazo 0 -> IllegalArgumentException", plazoMalo);

        System.out.println("5. Persistencia: lo hecho hasta aquí ya quedó en los CSV");
        ctx.limpiar();
        persistencia.cargarAlIniciar();
        check("recarga: 2 préstamos", ctx.getPrestamos().size() == 2);
        check("recarga: saldo de Ana = 4650.50", igual(ctx.getCuentaDe("U001").getSaldo(), "4650.50"));
        check("recarga: Ana tiene 5 transacciones", ctx.getCuentaDe("U001").getTransacciones().size() == 5);

        System.out.println("6. Gerente");
        check("un cliente no puede ser gerente", !tieneAcceso(gerente));
        login.iniciarSesion("gerente", "admin");
        check("rol GERENTE", login.getRolActual() == Rol.GERENTE);
        check("ve 3 clientes", gerente.getClientes().size() == 3);
        check("ve el préstamo P001 de Luis", gerente.getPrestamosDe("U002").size() == 1);
        check("ve las transacciones de Luis", gerente.getTransaccionesDe("U002").size() == 6);
        gerente.registrarPago("P001");
        Prestamo p1 = ctx.getPrestamo("P001");
        check("pago 4: cuotas pagadas = 4", p1.getCuotasPagadas() == 4);
        check("la cuota se debita de Luis: 5167.28 - 444.24 = 4723.04", igual(ctx.getCuentaDe("U002").getSaldo(), "4723.04"));

        System.out.println("7. Pago sin saldo (no debe cambiar nada)");
        login.iniciarSesion("luis", "1234");
        try { cuenta.retirar(new BigDecimal("4623.04")); } catch (SaldoInsuficienteException e) { }
        login.iniciarSesion("gerente", "admin");
        boolean sinSaldo = false;
        try { gerente.registrarPago("P001"); } catch (SaldoInsuficienteException e) { sinSaldo = true; }
        check("SaldoInsuficienteException al cobrar la cuota", sinSaldo);
        check("cuotas pagadas sigue en 4", ctx.getPrestamo("P001").getCuotasPagadas() == 4);

        System.out.println("8. Terminar de pagar el préstamo");
        login.iniciarSesion("luis", "1234");
        cuenta.depositar(new BigDecimal("5000"));
        login.iniciarSesion("gerente", "admin");
        while (ctx.getPrestamo("P001").isActivo()) {
            try { gerente.registrarPago("P001"); } catch (SaldoInsuficienteException e) { break; }
        }
        check("P001 queda PAGADO con 12 cuotas", ctx.getPrestamo("P001").getEstado() == EstadoPrestamo.PAGADO
                && ctx.getPrestamo("P001").getCuotasPagadas() == 12);
        boolean yaPagado = false;
        try { gerente.registrarPago("P001"); } catch (IllegalStateException | SaldoInsuficienteException e) { yaPagado = true; }
        check("pagar uno ya pagado -> error", yaPagado);

        System.out.println("9. Guardar y recargar todo");
        check("guardar() devuelve true", persistencia.guardar());
        ctx.limpiar();
        persistencia.cargarAlIniciar();
        check("recarga: P001 PAGADO y P002 ACTIVO",
                ctx.getPrestamo("P001").getEstado() == EstadoPrestamo.PAGADO && ctx.getPrestamo("P002").isActivo());
        check("recarga: nombres con acento intactos", ctx.buscarUsuario("maria").getNombre().equals("María Pérez"));
        check("siguienteId(\"P\") = P003", ctx.siguienteId("P").equals("P003"));

        System.out.println("10. Sin archivos CSV: arranca vacío sin errores");
        CsvRepositorio vacio = new CsvRepositorio(tmp.resolve("no-existe").toString());
        boolean sinError = true;
        try { vacio.cargar(); } catch (Exception e) { sinError = false; }
        check("cargar() sin archivos no lanza error", sinError);
        check("contexto vacío", ctx.getUsuarios().isEmpty() && ctx.getCuentas().isEmpty() && ctx.getPrestamos().isEmpty());
        check("guardar() crea la carpeta y devuelve true", vacio.guardar()
                && Files.exists(tmp.resolve("no-existe").resolve("usuarios.csv")));

        System.out.println(fallos == 0 ? "\nTODO OK" : "\nFALLARON " + fallos);
        System.exit(fallos == 0 ? 0 : 1);
    }

    private static boolean tieneAcceso(GerenteController g) {
        try {
            g.getClientes();
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }
}

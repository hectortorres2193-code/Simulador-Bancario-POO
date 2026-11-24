package com.banco.app;

import com.banco.app.controller.CuentaController;
import com.banco.app.controller.GerenteController;
import com.banco.app.controller.LoginController;
import com.banco.app.controller.PersistenciaController;
import com.banco.app.controller.PrestamoController;
import com.banco.app.data.BancoContext;
import com.banco.app.data.CsvRepositorio;
import com.banco.app.data.RepositorioDatos;

/**
 * Punto de entrada de la aplicación. Crea el repositorio y los controladores (inyección de
 * dependencias) y carga los CSV. La interfaz gráfica se conecta aquí (ver TODO).
 *
 * <p>Para ver los datos de ejemplo en consola, ejecutar {@link MainPrueba}.</p>
 */
public class Main {

    public static void main(String[] args) {
        // Salida en UTF-8 para que los acentos se vean bien en la consola de NetBeans
        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        System.setErr(new java.io.PrintStream(System.err, true, java.nio.charset.StandardCharsets.UTF_8));

        RepositorioDatos repositorio = new CsvRepositorio("datos");

        // Controladores (la lógica ya está implementada)
        PersistenciaController persistencia = new PersistenciaController(repositorio);
        LoginController login = new LoginController();
        CuentaController cuenta = new CuentaController();
        PrestamoController prestamo = new PrestamoController(repositorio);
        GerenteController gerente = new GerenteController(repositorio);

        // Al arrancar: lee los CSV de la carpeta "datos" (si no existen, arranca vacío sin errores)
        persistencia.cargarAlIniciar();

        // TODO (interfaz): crear aquí la ventana y las pantallas, pasándoles los controladores de arriba.
        //                  Al cerrar la ventana llamar a persistencia.guardarAlCerrar().
        //                  Ver "Guía para la interfaz" en el README.
        BancoContext ctx = BancoContext.getInstance();
        System.out.println("Datos cargados: " + ctx.getUsuarios().size() + " usuarios, "
                + ctx.getCuentas().size() + " cuentas, " + ctx.getPrestamos().size() + " préstamos.");
        System.out.println("La interfaz aún no está conectada. Ejecuta MainPrueba para ver los datos de ejemplo.");
    }
}

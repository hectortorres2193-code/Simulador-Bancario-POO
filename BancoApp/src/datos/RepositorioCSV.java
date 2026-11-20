package datos;

import java.io.BufferedReader;   // Lee texto de un archivo linea por linea de forma eficiente
import java.io.BufferedWriter;   // Escribe texto en un archivo linea por linea
import java.io.File;             // Representa rutas de archivos y carpetas en el sistema
import java.io.FileReader;       // Abre un archivo fisico en modo lectura
import java.io.FileWriter;       // Abre o crea un archivo fisico en modo escritura
import java.io.IOException;      // Maneja excepciones si falla la lectura o escritura en disco
import java.time.LocalDateTime;  // Maneja fecha y hora exacta para registrar transacciones
import java.util.ArrayList;      // Lista dinamica para guardar las lineas leidas o por escribir
import java.util.List;           // Interfaz general de listas
import modelo.Cuenta;            // Clase Cuenta del paquete modelo
import modelo.Prestamo;          // Clase Prestamo del paquete modelo
import modelo.Transaccion;       // Clase Transaccion del paquete modelo
import modelo.Usuario;           // Clase Usuario del paquete modelo

/**
 * Implementacion de RepositorioDatos que lee y guarda la informacion del banco
 * utilizando archivos planos en formato CSV.
 */
public class RepositorioCSV implements RepositorioDatos {

    private String carpetaDatos;

    /**
     * Constructor por defecto; apunta a la carpeta local "datos".
     */
    public RepositorioCSV() {
        this.carpetaDatos = "datos";
    }

    /**
     * Constructor sobrecargado para indicar una ruta personalizada de datos.
     * @param carpetaDatos Ruta de la carpeta donde estan los archivos CSV.
     */
    public RepositorioCSV(String carpetaDatos) {
        this.carpetaDatos = carpetaDatos;
    }

    // Imprime un aviso en consola si un renglon del CSV esta incompleto o mal formateado
    private void avisoRenglon(String archivo, int renglon, String motivo) {
        System.err.println("Aviso en " + archivo + " (linea " + renglon + "): " + motivo);
    }

    // Lee un archivo CSV completo y devuelve sus renglones en una lista
    private List<String> leerArchivo(String nombre) {
        List<String> lineas = new ArrayList<>();
        // File.separator coloca '\' en Windows o '/' en Linux/Mac automaticamente
        File archivo = new File(carpetaDatos + File.separator + nombre);

        if (!archivo.exists()) {
            System.err.println("No se encontro el archivo: " + archivo.getPath());
            return lineas;
        }

        // El try-with-resources cierra el archivo automaticamente al terminar de leer
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            // Lee renglon por renglon hasta que readLine() devuelve null (fin del archivo)
            while ((linea = br.readLine()) != null) {
                if (!linea.trim().isEmpty()) {
                    lineas.add(linea);
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer " + nombre + ": " + e.getMessage());
        }
        return lineas;
    }

    // Escribe la lista de renglones en el archivo CSV y devuelve true si tuvo exito
    private boolean escribirArchivo(String nombre, List<String> lineas) {
        File dir = new File(carpetaDatos);
        if (!dir.exists()) {
            dir.mkdirs(); // Crea la carpeta datos si aun no existe
        }

        File archivo = new File(dir, nombre);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo))) {
            for (String linea : lineas) {
                bw.write(linea);
                bw.newLine();
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error al guardar " + nombre + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Carga usuarios, cuentas, prestamos y transacciones desde los archivos CSV
     * hacia el ContextoBanco en memoria. Si algun archivo no existe, continua sin fallar.
     */
    @Override
    public void cargar() {
        ContextoBanco ctx = ContextoBanco.getInstancia();
        ctx.limpiar(); // Limpia memoria previa para evitar datos repetidos

        // 1. Cargar usuarios (inicia en i = 1 para saltar la fila 0 de encabezados)
        List<String> lineasUsuarios = leerArchivo("usuarios.csv");
        for (int i = 1; i < lineasUsuarios.size(); i++) {
            String[] p = lineasUsuarios.get(i).split(",");
            if (p.length >= 5) {
                Usuario u = new Usuario(p[0].trim(), p[1].trim(), p[2].trim(), p[3].trim(), p[4].trim());
                ctx.agregarUsuario(u);
            } else {
                avisoRenglon("usuarios.csv", i + 1, "columnas insuficientes");
            }
        }

        // 2. Cargar cuentas
        List<String> lineasCuentas = leerArchivo("cuentas.csv");
        for (int i = 1; i < lineasCuentas.size(); i++) {
            String[] p = lineasCuentas.get(i).split(",");
            if (p.length >= 3) {
                try {
                    String numCuenta = p[0].trim();
                    String titular = p[1].trim();
                    double saldo = Double.parseDouble(p[2].trim());

                    Cuenta c = new Cuenta(numCuenta, saldo);
                    ctx.asociarCuenta(titular, c);
                } catch (NumberFormatException e) {
                    avisoRenglon("cuentas.csv", i + 1, "saldo invalido");
                }
            } else {
                avisoRenglon("cuentas.csv", i + 1, "columnas insuficientes");
            }
        }

        // 3. Cargar prestamos
        List<String> lineasPrestamos = leerArchivo("prestamos.csv");
        for (int i = 1; i < lineasPrestamos.size(); i++) {
            String[] p = lineasPrestamos.get(i).split(",");
            if (p.length >= 6) {
                try {
                    String titular = p[0].trim();
                    double monto = Double.parseDouble(p[1].trim());
                    int plazo = Integer.parseInt(p[2].trim());
                    double interes = Double.parseDouble(p[3].trim());
                    double pagoMensual = Double.parseDouble(p[4].trim());
                    double saldoPendiente = Double.parseDouble(p[5].trim());

                    Prestamo pr = new Prestamo(monto, plazo, interes, pagoMensual, saldoPendiente);
                    ctx.asociarPrestamo(titular, pr);
                } catch (NumberFormatException e) {
                    avisoRenglon("prestamos.csv", i + 1, "formato numerico invalido");
                }
            } else {
                avisoRenglon("prestamos.csv", i + 1, "columnas insuficientes");
            }
        }

        // 4. Cargar transacciones
        List<String> lineasTrans = leerArchivo("transacciones.csv");
        for (int i = 1; i < lineasTrans.size(); i++) {
            String[] p = lineasTrans.get(i).split(",");
            if (p.length >= 5) {
                try {
                    int id = Integer.parseInt(p[0].trim());
                    String numCuenta = p[1].trim();
                    LocalDateTime fecha = LocalDateTime.parse(p[2].trim());
                    String tipo = p[3].trim();
                    double monto = Double.parseDouble(p[4].trim());

                    Transaccion t = new Transaccion(id, fecha, tipo, monto);
                    ctx.asociarTransaccion(numCuenta, t);
                } catch (Exception e) {
                    avisoRenglon("transacciones.csv", i + 1, "dato o fecha invalida");
                }
            } else {
                avisoRenglon("transacciones.csv", i + 1, "columnas insuficientes");
            }
        }
    }

    /**
     * Guarda en disco los 4 archivos CSV con la informacion actual en memoria.
     * @return true si todos los archivos se guardaron correctamente, false si alguno fallo.
     */
    @Override
    public boolean guardar() {
        ContextoBanco ctx = ContextoBanco.getInstancia();

        // 1. Guardar usuarios
        List<String> outUsuarios = new ArrayList<>();
        outUsuarios.add("nombre,telefono,contrasena,tarjeta,rol");
        for (Usuario u : ctx.obtenerUsuarios()) {
            outUsuarios.add(u.toCSV());
        }
        boolean okUsuarios = escribirArchivo("usuarios.csv", outUsuarios);

        // 2. Guardar cuentas
        List<String> outCuentas = new ArrayList<>();
        outCuentas.add("numeroCuenta,titular,saldo");
        for (Cuenta c : ctx.obtenerCuentas()) {
            String titular = ctx.obtenerTitularDeCuenta(c);
            outCuentas.add(c.getNumeroCuenta() + "," + titular + "," + c.consultarSaldo());
        }
        boolean okCuentas = escribirArchivo("cuentas.csv", outCuentas);

        // 3. Guardar prestamos
        List<String> outPrestamos = new ArrayList<>();
        outPrestamos.add("titular,monto,plazo,interes,pagoMensual,saldoPendiente");
        for (Prestamo pr : ctx.obtenerPrestamos()) {
            String titular = ctx.obtenerTitularDePrestamo(pr);
            outPrestamos.add(titular + "," + pr.toCSV());
        }
        boolean okPrestamos = escribirArchivo("prestamos.csv", outPrestamos);

        // 4. Guardar transacciones
        List<String> outTrans = new ArrayList<>();
        outTrans.add("idTransaccion,numeroCuenta,fecha,tipo,monto");
        for (Transaccion t : ctx.obtenerTransacciones()) {
            String numCuenta = ctx.obtenerNumeroCuentaDeTransaccion(t);
            outTrans.add(t.toCSV(numCuenta));
        }
        boolean okTrans = escribirArchivo("transacciones.csv", outTrans);

        return okUsuarios && okCuentas && okPrestamos && okTrans;
    }
}
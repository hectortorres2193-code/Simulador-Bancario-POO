package com.banco.app.data;

import com.banco.app.model.Cliente;
import com.banco.app.model.Cuenta;
import com.banco.app.model.EstadoPrestamo;
import com.banco.app.model.Gerente;
import com.banco.app.model.Prestamo;
import com.banco.app.model.Rol;
import com.banco.app.model.TipoTransaccion;
import com.banco.app.model.Transaccion;
import com.banco.app.model.Usuario;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación de {@link RepositorioDatos} que lee y guarda la información del banco
 * en archivos planos CSV (separador ";", con encabezado, codificación UTF-8).
 *
 * <p>Archivos: usuarios.csv, cuentas.csv, transacciones.csv y prestamos.csv.
 * Los formatos exactos están en docs/README.md.</p>
 */
public class CsvRepositorio implements RepositorioDatos {

    private static final String SEPARADOR = ";";

    private final String carpetaDatos;

    /** Constructor por defecto: apunta a la carpeta local "datos". */
    public CsvRepositorio() {
        this("datos");
    }

    /** @param carpetaDatos ruta de la carpeta donde están (o se crearán) los archivos CSV */
    public CsvRepositorio(String carpetaDatos) {
        this.carpetaDatos = carpetaDatos;
    }

    // Imprime un aviso en consola si un renglón del CSV está incompleto o mal formateado
    private void avisoRenglon(String archivo, int renglon, String motivo) {
        System.err.println("Aviso en " + archivo + " (línea " + renglon + "): " + motivo);
    }

    // Lee un archivo CSV completo y devuelve sus renglones (el primero es el encabezado)
    private List<String> leerArchivo(String nombre) {
        List<String> lineas = new ArrayList<>();
        // File.separator coloca '\' en Windows o '/' en Linux/Mac automáticamente
        File archivo = new File(carpetaDatos + File.separator + nombre);

        if (!archivo.exists()) {
            System.err.println("No se encontró el archivo: " + archivo.getPath());
            return lineas; // sin archivo: se arranca vacío, sin lanzar error
        }

        // try-with-resources cierra el archivo automáticamente; UTF-8 evita que se dañen los acentos
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(archivo), StandardCharsets.UTF_8))) {
            String linea;
            boolean primera = true;
            while ((linea = br.readLine()) != null) {
                if (primera) {
                    linea = linea.replace("\uFEFF", ""); // quita el BOM que agregan algunos editores
                    primera = false;
                }
                if (!linea.trim().isEmpty()) {
                    lineas.add(linea);
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer " + nombre + ": " + e.getMessage());
        }
        return lineas;
    }

    // Escribe la lista de renglones en el archivo CSV y devuelve true si tuvo éxito
    private boolean escribirArchivo(String nombre, List<String> lineas) {
        File dir = new File(carpetaDatos);
        if (!dir.exists()) {
            dir.mkdirs(); // crea la carpeta datos si aún no existe
        }

        File archivo = new File(dir, nombre);
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(archivo), StandardCharsets.UTF_8))) {
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

    // ------------------------------------------------------------------
    // CARGAR
    // ------------------------------------------------------------------

    /**
     * Carga usuarios, cuentas, transacciones y préstamos desde los CSV hacia el
     * {@link BancoContext}. Si algún archivo no existe o un renglón es inválido,
     * continúa sin fallar (solo avisa en consola).
     */
    @Override
    public void cargar() {
        BancoContext ctx = BancoContext.getInstance();
        ctx.limpiar(); // evita datos repetidos si se recarga
        cargarUsuarios(ctx);
        cargarCuentas(ctx);
        cargarTransacciones(ctx);
        cargarPrestamos(ctx);
    }

    private void cargarUsuarios(BancoContext ctx) {
        List<String> lineas = leerArchivo("usuarios.csv");
        for (int i = 1; i < lineas.size(); i++) { // i = 1 salta el encabezado
            String[] p = lineas.get(i).split(SEPARADOR, -1);
            if (p.length < 5) {
                avisoRenglon("usuarios.csv", i + 1, "columnas insuficientes");
                continue;
            }
            try {
                Rol rol = Rol.valueOf(p[1].trim().toUpperCase());
                String id = p[0].trim();
                String username = p[2].trim();
                String password = p[3].trim();
                String nombre = p[4].trim();
                Usuario u = (rol == Rol.GERENTE)
                        ? new Gerente(id, nombre, username, password)
                        : new Cliente(id, nombre, username, password);
                ctx.getUsuarios().add(u);
            } catch (IllegalArgumentException e) {
                avisoRenglon("usuarios.csv", i + 1, "rol inválido (usa CLIENTE o GERENTE)");
            }
        }
    }

    private void cargarCuentas(BancoContext ctx) {
        List<String> lineas = leerArchivo("cuentas.csv");
        for (int i = 1; i < lineas.size(); i++) {
            String[] p = lineas.get(i).split(SEPARADOR, -1);
            if (p.length < 3) {
                avisoRenglon("cuentas.csv", i + 1, "columnas insuficientes");
                continue;
            }
            try {
                ctx.getCuentas().add(new Cuenta(p[0].trim(), p[1].trim(), numero(p[2])));
            } catch (NumberFormatException e) {
                avisoRenglon("cuentas.csv", i + 1, "saldo inválido");
            }
        }
    }

    private void cargarTransacciones(BancoContext ctx) {
        // Mapa idCuenta -> Cuenta para asociar cada transacción sin recorrer la lista cada vez
        Map<String, Cuenta> porId = new HashMap<>();
        for (Cuenta c : ctx.getCuentas()) {
            porId.put(c.getIdCuenta(), c);
        }

        List<String> lineas = leerArchivo("transacciones.csv");
        for (int i = 1; i < lineas.size(); i++) {
            String[] p = lineas.get(i).split(SEPARADOR, -1);
            if (p.length < 5) {
                avisoRenglon("transacciones.csv", i + 1, "columnas insuficientes");
                continue;
            }
            try {
                Cuenta cuenta = porId.get(p[1].trim());
                if (cuenta == null) {
                    avisoRenglon("transacciones.csv", i + 1, "la cuenta " + p[1].trim() + " no existe");
                    continue;
                }
                String descripcion = p.length > 5 ? p[5].trim() : "";
                cuenta.agregarTransaccion(new Transaccion(
                        p[0].trim(), cuenta.getIdCuenta(),
                        TipoTransaccion.valueOf(p[2].trim().toUpperCase()),
                        numero(p[3]), LocalDateTime.parse(p[4].trim()), descripcion));
            } catch (Exception e) {
                avisoRenglon("transacciones.csv", i + 1, "dato, tipo o fecha inválida");
            }
        }
    }

    private void cargarPrestamos(BancoContext ctx) {
        List<String> lineas = leerArchivo("prestamos.csv");
        for (int i = 1; i < lineas.size(); i++) {
            String[] p = lineas.get(i).split(SEPARADOR, -1);
            if (p.length < 9) {
                avisoRenglon("prestamos.csv", i + 1, "columnas insuficientes");
                continue;
            }
            try {
                ctx.agregarPrestamo(new Prestamo(
                        p[0].trim(), p[1].trim(), p[2].trim(),
                        numero(p[3]), numero(p[4]),
                        Integer.parseInt(p[5].trim()), Integer.parseInt(p[6].trim()),
                        EstadoPrestamo.valueOf(p[7].trim().toUpperCase()),
                        LocalDate.parse(p[8].trim())));
            } catch (Exception e) {
                avisoRenglon("prestamos.csv", i + 1, "dato, estado o fecha inválida");
            }
        }
    }

    // ------------------------------------------------------------------
    // GUARDAR
    // ------------------------------------------------------------------

    /**
     * Guarda en disco los 4 archivos CSV con la información actual en memoria.
     *
     * @return true si todos los archivos se guardaron correctamente, false si alguno falló
     */
    @Override
    public boolean guardar() {
        BancoContext ctx = BancoContext.getInstance();

        // 1. Usuarios
        List<String> outUsuarios = new ArrayList<>();
        outUsuarios.add("id;rol;username;password;nombre");
        for (Usuario u : ctx.getUsuarios()) {
            outUsuarios.add(fila(u.getId(), u.getRol().name(), u.getUsername(), u.getPassword(), u.getNombre()));
        }
        boolean okUsuarios = escribirArchivo("usuarios.csv", outUsuarios);

        // 2. Cuentas
        List<String> outCuentas = new ArrayList<>();
        outCuentas.add("idCuenta;idCliente;saldo");
        for (Cuenta c : ctx.getCuentas()) {
            outCuentas.add(fila(c.getIdCuenta(), c.getIdCliente(), dinero(c.getSaldo())));
        }
        boolean okCuentas = escribirArchivo("cuentas.csv", outCuentas);

        // 3. Transacciones (viven dentro de cada cuenta)
        List<String> outTrans = new ArrayList<>();
        outTrans.add("idTransaccion;idCuenta;tipo;monto;fecha;descripcion");
        for (Cuenta c : ctx.getCuentas()) {
            for (Transaccion t : c.getTransacciones()) {
                outTrans.add(fila(t.getIdTransaccion(), t.getIdCuenta(), t.getTipo().name(),
                        dinero(t.getMonto()), t.getFecha().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        t.getDescripcion()));
            }
        }
        boolean okTrans = escribirArchivo("transacciones.csv", outTrans);

        // 4. Préstamos
        List<String> outPrestamos = new ArrayList<>();
        outPrestamos.add("idPrestamo;idCliente;idCuenta;montoOriginal;tasaAnual;plazoMeses;cuotasPagadas;estado;fechaInicio");
        for (Prestamo p : ctx.getPrestamos()) {
            outPrestamos.add(fila(p.getIdPrestamo(), p.getIdCliente(), p.getIdCuenta(),
                    dinero(p.getMontoOriginal()), p.getTasaAnual().toPlainString(),
                    String.valueOf(p.getPlazoMeses()), String.valueOf(p.getCuotasPagadas()),
                    p.getEstado().name(), p.getFechaInicio().toString()));
        }
        boolean okPrestamos = escribirArchivo("prestamos.csv", outPrestamos);

        return okUsuarios && okCuentas && okTrans && okPrestamos;
    }

    // ------------------------------------------------------------------
    // auxiliares
    // ------------------------------------------------------------------

    /** Convierte texto a BigDecimal (acepta coma o punto decimal). */
    private BigDecimal numero(String texto) {
        return new BigDecimal(texto.trim().replace(',', '.'));
    }

    private String dinero(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    /** Une los campos con ";" quitando de cada uno los ";" y saltos de línea que romperían el CSV. */
    private String fila(String... campos) {
        List<String> limpios = new ArrayList<>();
        for (String c : campos) {
            String t = (c == null) ? "" : c;
            limpios.add(t.replace(SEPARADOR, ",").replace("\r", " ").replace("\n", " "));
        }
        return String.join(SEPARADOR, limpios);
    }
}

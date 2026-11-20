package datos;

import java.util.ArrayList; // Implementacion de lista dinamica que crece segun se agregan datos
import java.util.HashMap;   // Implementacion de diccionario para buscar rapido por clave (ej. nombre)
import java.util.List;      // Interfaz general para manejar colecciones ordenadas (listas)
import java.util.Map;       // Interfaz general para guardar pares Clave -> Valor
import modelo.Cuenta;       // Clase del paquete modelo que representa una cuenta bancaria
import modelo.Prestamo;     // Clase del paquete modelo que representa un prestamo
import modelo.Transaccion;  // Clase del paquete modelo que representa un movimiento (deposito/retiro)
import modelo.Usuario;      // Clase del paquete modelo que representa a un cliente o gerente

/**
 * Clase Singleton que centraliza los datos del banco en memoria.
 * Garantiza que exista una sola instancia compartida en toda la aplicacion.
 */
public class ContextoBanco {

    // Inicia en null hasta que se manda llamar getInstancia() por primera vez
    private static ContextoBanco instancia;

    // Listas principales donde se mantienen en memoria los registros cargados de los CSV
    private List<Usuario> usuarios;
    private List<Cuenta> cuentas;
    private List<Prestamo> prestamos;
    private List<Transaccion> transacciones;

    // Guarda al usuario que inicio sesion (vale null cuando nadie esta logueado)
    private Usuario usuarioActual;

    // Mapas para cruzar datos rapido sin tener que recorrer toda la lista cada vez
    private Map<String, Cuenta> mapaCuentasPorUsuario;       // Nombre usuario -> Cuenta
    private Map<String, Prestamo> mapaPrestamosPorUsuario;   // Nombre usuario -> Prestamo
    private Map<Integer, String> mapaCuentaPorTransaccion;   // ID objeto -> Numero de cuenta

    /**
     * Constructor privado para evitar que se creen instancias con 'new' desde otras clases.
     */
    private ContextoBanco() {
        this.usuarios = new ArrayList<>();
        this.cuentas = new ArrayList<>();
        this.prestamos = new ArrayList<>();
        this.transacciones = new ArrayList<>();
        this.mapaCuentasPorUsuario = new HashMap<>();
        this.mapaPrestamosPorUsuario = new HashMap<>();
        this.mapaCuentaPorTransaccion = new HashMap<>();
    }

    /**
     * Devuelve la unica instancia del banco; si aun no existe, la crea una sola vez.
     * @return Instancia unica de ContextoBanco.
     */
    public static synchronized ContextoBanco getInstancia() {
        if (instancia == null) {
            instancia = new ContextoBanco();
        }
        return instancia;
    }

    /**
     * Busca un usuario registrado por su nombre (sin importar mayusculas o espacios).
     * @param nombre Nombre del usuario a buscar.
     * @return El objeto Usuario si existe, o null si no se encontro.
     */
    public Usuario buscarUsuario(String nombre) {
        if (nombre == null) return null;

        for (Usuario u : usuarios) {
            // trim() quita espacios extra y equalsIgnoreCase ignora mayusculas/minusculas
            if (u.getNombre().equalsIgnoreCase(nombre.trim())) {
                return u;
            }
        }
        return null; // Regresa null si no encontro ningun usuario con ese nombre
    }

    /**
     * Busca un usuario por nombre y valida que tenga rol de CLIENTE.
     * @param nombre Nombre del cliente.
     * @return El objeto Usuario si es cliente, o null en caso contrario.
     */
    public Usuario obtenerCliente(String nombre) {
        Usuario u = buscarUsuario(nombre);
        // Verifica que exista (no sea null) y que su rol sea CLIENTE
        if (u != null && "CLIENTE".equalsIgnoreCase(u.getRol())) {
            return u;
        }
        return null;
    }

    /**
     * Obtiene la cuenta bancaria asociada a un usuario.
     * @param usuario Usuario titular de la cuenta.
     * @return Objeto Cuenta asociado, o null si no tiene.
     */
    public Cuenta obtenerCuenta(Usuario usuario) {
        if (usuario == null) return null;
        return mapaCuentasPorUsuario.get(usuario.getNombre());
    }

    /**
     * Obtiene el prestamo activo asociado a un cliente.
     * @param usuario Cliente a consultar.
     * @return Objeto Prestamo asociado, o null si no tiene deuda activa.
     */
    public Prestamo obtenerPrestamoCliente(Usuario usuario) {
        if (usuario == null) return null;
        return mapaPrestamosPorUsuario.get(usuario.getNombre());
    }

    public void agregarUsuario(Usuario usuario) {
        usuarios.add(usuario);
    }

    public void asociarCuenta(String nombreUsuario, Cuenta cuenta) {
        cuentas.add(cuenta);
        mapaCuentasPorUsuario.put(nombreUsuario, cuenta);
    }

    public void agregarPrestamo(Prestamo prestamo) {
        prestamos.add(prestamo);
        if (usuarioActual != null) {
            mapaPrestamosPorUsuario.put(usuarioActual.getNombre(), prestamo);
        }
    }

    public void asociarPrestamo(String nombreUsuario, Prestamo prestamo) {
        prestamos.add(prestamo);
        mapaPrestamosPorUsuario.put(nombreUsuario, prestamo);
    }

    public void agregarTransaccion(Transaccion transaccion) {
        transacciones.add(transaccion);
        if (usuarioActual != null) {
            Cuenta c = obtenerCuenta(usuarioActual);
            if (c != null) {
                // hashCode() da un identificador unico del objeto en memoria para asociarlo a la cuenta
                mapaCuentaPorTransaccion.put(transaccion.hashCode(), c.getNumeroCuenta());
            }
        }
    }

    public void asociarTransaccion(String numeroCuenta, Transaccion transaccion) {
        transacciones.add(transaccion);
        mapaCuentaPorTransaccion.put(transaccion.hashCode(), numeroCuenta);
    }

    public Usuario obtenerUsuarioActual() {
        return usuarioActual;
    }

    public void establecerUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
    }

    public List<Usuario> obtenerUsuarios() { return usuarios; }
    public List<Prestamo> obtenerPrestamos() { return prestamos; }
    public List<Cuenta> obtenerCuentas() { return cuentas; }
    public List<Transaccion> obtenerTransacciones() { return transacciones; }

    /**
     * Busca en el mapa el nombre del titular de una cuenta para guardarlo en el CSV.
     * @param cuenta Cuenta bancaria a consultar.
     * @return Nombre del titular o "Desconocido" si no se encuentra.
     */
    public String obtenerTitularDeCuenta(Cuenta cuenta) {
        for (Map.Entry<String, Cuenta> entrada : mapaCuentasPorUsuario.entrySet()) {
            if (entrada.getValue().equals(cuenta)) {
                return entrada.getKey();
            }
        }
        return "Desconocido";
    }

    /**
     * Busca en el mapa el nombre del titular de un prestamo para guardarlo en el CSV.
     * @param prestamo Prestamo a consultar.
     * @return Nombre del titular o "Desconocido" si no se encuentra.
     */
    public String obtenerTitularDePrestamo(Prestamo prestamo) {
        for (Map.Entry<String, Prestamo> entrada : mapaPrestamosPorUsuario.entrySet()) {
            if (entrada.getValue().equals(prestamo)) {
                return entrada.getKey();
            }
        }
        return "Desconocido";
    }

    /**
     * Obtiene el numero de cuenta al que pertenece una transaccion.
     * @param t Transaccion a consultar.
     * @return Numero de cuenta asociado o "CTA-0000" por defecto.
     */
    public String obtenerNumeroCuentaDeTransaccion(Transaccion t) {
        return mapaCuentaPorTransaccion.getOrDefault(t.hashCode(), "CTA-0000");
    }

    /**
     * Vacia todas las listas y mapas en memoria antes de recargar los archivos CSV.
     */
    public void limpiar() {
        usuarios.clear();
        cuentas.clear();
        prestamos.clear();
        transacciones.clear();
        mapaCuentasPorUsuario.clear();
        mapaPrestamosPorUsuario.clear();
        mapaCuentaPorTransaccion.clear();
        usuarioActual = null;
    }
}
package com.banco.app.data;

import com.banco.app.model.Cliente;
import com.banco.app.model.Cuenta;
import com.banco.app.model.Prestamo;
import com.banco.app.model.Transaccion;
import com.banco.app.model.Usuario;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Patrón Singleton: única instancia que administra en memoria los usuarios,
 * las cuentas (con sus transacciones) y los préstamos de toda la aplicación.
 */
public class BancoContext {

    // Inicia en null hasta que se llama getInstance() por primera vez
    private static BancoContext instancia;

    private final List<Usuario> usuarios = new ArrayList<>();
    private final List<Cuenta> cuentas = new ArrayList<>();
    private final List<Prestamo> prestamos = new ArrayList<>();
    private Usuario usuarioActual; // null cuando nadie ha iniciado sesión

    /** Constructor privado: nadie más puede crear instancias con new. */
    private BancoContext() {
    }

    /** @return la única instancia (se crea la primera vez que se pide). */
    public static synchronized BancoContext getInstance() {
        if (instancia == null) {
            instancia = new BancoContext();
        }
        return instancia;
    }

    /** @return el usuario con ese username (sin importar mayúsculas ni espacios), o null si no existe. */
    public Usuario buscarUsuario(String username) {
        if (username == null) {
            return null;
        }
        for (Usuario u : usuarios) {
            if (u.getUsername().equalsIgnoreCase(username.trim())) {
                return u;
            }
        }
        return null;
    }

    /** @return la cuenta del cliente, o null si no tiene. */
    public Cuenta getCuentaDe(String idCliente) {
        for (Cuenta c : cuentas) {
            if (c.getIdCliente().equals(idCliente)) {
                return c;
            }
        }
        return null;
    }

    /** @return solo los usuarios que son Cliente. */
    public List<Cliente> getClientes() {
        List<Cliente> clientes = new ArrayList<>();
        for (Usuario u : usuarios) {
            if (u instanceof Cliente) {
                clientes.add((Cliente) u);
            }
        }
        return clientes;
    }

    /** @return los préstamos de un cliente (activos y pagados). */
    public List<Prestamo> getPrestamosDe(String idCliente) {
        List<Prestamo> lista = new ArrayList<>();
        for (Prestamo p : prestamos) {
            if (p.getIdCliente().equals(idCliente)) {
                lista.add(p);
            }
        }
        return lista;
    }

    /** @return el préstamo con ese id, o null si no existe. */
    public Prestamo getPrestamo(String idPrestamo) {
        for (Prestamo p : prestamos) {
            if (p.getIdPrestamo().equals(idPrestamo)) {
                return p;
            }
        }
        return null;
    }

    public void agregarPrestamo(Prestamo p) {
        if (p == null) {
            throw new IllegalArgumentException("El préstamo no puede ser nulo.");
        }
        prestamos.add(p);
    }

    /** Genera un id nuevo con el prefijo dado y 3 dígitos (por ejemplo "P" -> "P002"). */
    public String siguienteId(String prefijo) {
        if (prefijo == null || prefijo.isEmpty()) {
            throw new IllegalArgumentException("El prefijo no puede estar vacío.");
        }
        int max = 0;
        List<String> ids = new ArrayList<>();
        for (Usuario u : usuarios) ids.add(u.getId());
        for (Cuenta c : cuentas) {
            ids.add(c.getIdCuenta());
            for (Transaccion t : c.getTransacciones()) ids.add(t.getIdTransaccion());
        }
        for (Prestamo p : prestamos) ids.add(p.getIdPrestamo());

        for (String id : ids) {
            if (id != null && id.startsWith(prefijo)) {
                try {
                    max = Math.max(max, Integer.parseInt(id.substring(prefijo.length())));
                } catch (NumberFormatException e) {
                    // id con otro formato (por ejemplo, transacciones nuevas): se ignora
                }
            }
        }
        return String.format(Locale.ROOT, "%s%03d", prefijo, max + 1);
    }

    public Usuario getUsuarioActual() { return usuarioActual; }

    public void setUsuarioActual(Usuario u) { this.usuarioActual = u; }

    // Listas completas: las usa el repositorio para cargar y guardar
    public List<Usuario> getUsuarios() { return usuarios; }

    public List<Cuenta> getCuentas() { return cuentas; }

    public List<Prestamo> getPrestamos() { return prestamos; }

    /** Vacía todas las listas en memoria antes de recargar los archivos CSV. */
    public void limpiar() {
        usuarios.clear();
        cuentas.clear();
        prestamos.clear();
        usuarioActual = null;
    }
}

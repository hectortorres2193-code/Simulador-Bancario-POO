package com.banco.app.controller;

import com.banco.app.data.BancoContext;
import com.banco.app.model.Rol;
import com.banco.app.model.Usuario;

/** Controlador de la pantalla de login. */
public class LoginController {

    /**
     * Valida las credenciales contra los usuarios cargados desde el CSV.
     * Si son correctas, deja al usuario como "usuario actual" del {@link BancoContext}.
     *
     * @return true si el login es correcto
     */
    public boolean iniciarSesion(String username, String password) {
        if (username == null || password == null || username.isBlank()) {
            return false;
        }
        BancoContext ctx = BancoContext.getInstance();
        Usuario u = ctx.buscarUsuario(username);
        if (u != null && u.autenticar(username, password)) {
            ctx.setUsuarioActual(u);
            return true;
        }
        return false;
    }

    /**
     * @return el rol del usuario que inició sesión (la vista lo usa para decidir qué pantalla mostrar)
     * @throws IllegalStateException si no hay sesión iniciada
     */
    public Rol getRolActual() {
        Usuario u = BancoContext.getInstance().getUsuarioActual();
        if (u == null) {
            throw new IllegalStateException("No hay una sesión iniciada.");
        }
        return u.getRol();
    }

    /** Cierra la sesión actual (no guarda datos; para eso está PersistenciaController). */
    public void cerrarSesion() {
        BancoContext.getInstance().setUsuarioActual(null);
    }
}

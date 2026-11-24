package com.banco.app.model;

/** Cliente del banco: tiene una cuenta y puede solicitar préstamos. */
public class Cliente extends Usuario {

    public Cliente(String id, String nombre, String username, String password) {
        super(id, nombre, username, password);
    }

    @Override
    public Rol getRol() {
        return Rol.CLIENTE;
    }
}

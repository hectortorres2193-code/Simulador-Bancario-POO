package com.banco.app.model;

/** Gerente: puede ver clientes y préstamos, y registrar pagos de cuotas. */
public class Gerente extends Usuario {

    public Gerente(String id, String nombre, String username, String password) {
        super(id, nombre, username, password);
    }

    @Override
    public Rol getRol() {
        return Rol.GERENTE;
    }
}

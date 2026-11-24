package com.banco.app.model;

/** Se lanza cuando una operación dejaría la cuenta en sobregiro. */
public class SaldoInsuficienteException extends Exception {

    private static final long serialVersionUID = 1L;

    public SaldoInsuficienteException(String mensaje) {
        super(mensaje);
    }
}

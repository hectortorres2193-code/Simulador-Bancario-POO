package com.banco.app.controller;

import com.banco.app.data.BancoContext;
import com.banco.app.model.Cuenta;
import com.banco.app.model.SaldoInsuficienteException;
import com.banco.app.model.Transaccion;
import com.banco.app.model.Usuario;

import java.math.BigDecimal;
import java.util.List;

/** Controlador del Dashboard y de las operaciones de caja (depósitos y retiros) del cliente en sesión. */
public class CuentaController {

    /** @throws IllegalStateException si no hay sesión iniciada */
    public String getNombreCliente() {
        return usuarioActual().getNombre();
    }

    /** @return el saldo actual de la cuenta del cliente en sesión */
    public BigDecimal getSaldo() {
        return cuentaActual().getSaldo();
    }

    /** @return las últimas n transacciones, de la más reciente a la más antigua */
    public List<Transaccion> getUltimasTransacciones(int n) {
        return cuentaActual().getUltimasTransacciones(n);
    }

    /**
     * Deposita efectivo en la cuenta (se actualiza en memoria al instante).
     *
     * @throws IllegalArgumentException si el monto es nulo o <= 0
     */
    public void depositar(BigDecimal monto) {
        cuentaActual().depositar(monto);
    }

    /**
     * Retira efectivo de la cuenta.
     *
     * @throws SaldoInsuficienteException si el retiro dejaría la cuenta en sobregiro
     * @throws IllegalArgumentException   si el monto es nulo o <= 0
     */
    public void retirar(BigDecimal monto) throws SaldoInsuficienteException {
        cuentaActual().retirar(monto);
    }

    private Usuario usuarioActual() {
        Usuario u = BancoContext.getInstance().getUsuarioActual();
        if (u == null) {
            throw new IllegalStateException("No hay una sesión iniciada.");
        }
        return u;
    }

    private Cuenta cuentaActual() {
        Cuenta c = BancoContext.getInstance().getCuentaDe(usuarioActual().getId());
        if (c == null) {
            throw new IllegalStateException("El usuario actual no tiene una cuenta.");
        }
        return c;
    }
}

package com.banco.app.controller;

import com.banco.app.data.RepositorioDatos;

/** Controlador de la persistencia: carga al iniciar, botón "Guardar" y guardado al cerrar la ventana. */
public class PersistenciaController {

    private final RepositorioDatos repositorio;

    public PersistenciaController(RepositorioDatos repositorio) {
        this.repositorio = repositorio;
    }

    /** Se llama una vez desde Main, al arrancar. Si no hay archivos CSV, la app arranca vacía. */
    public void cargarAlIniciar() {
        repositorio.cargar();
    }

    /**
     * Botón "Guardar".
     *
     * @return true si todos los archivos se guardaron; false si falló alguno (la vista debe avisar)
     */
    public boolean guardar() {
        return repositorio.guardar();
    }

    /**
     * La llama el WindowListener de VentanaPrincipal justo antes de cerrar.
     *
     * @return true si se guardó correctamente
     */
    public boolean guardarAlCerrar() {
        return repositorio.guardar();
    }
}

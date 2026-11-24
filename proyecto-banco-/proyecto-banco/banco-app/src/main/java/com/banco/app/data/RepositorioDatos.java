package com.banco.app.data;

/**
 * Interfaz para definir las operaciones básicas de persistencia (cargar y guardar).
 * Desacopla los controladores de la implementación específica en archivos CSV.
 * Trabaja sobre el {@link BancoContext} (Singleton), por eso no recibe parámetros.
 */
public interface RepositorioDatos {

    /** Lee los datos del almacenamiento y los carga en el contexto en memoria. No lanza error si faltan archivos. */
    void cargar();

    /**
     * Guarda el estado actual del banco en el almacenamiento.
     *
     * @return true si todos los archivos se guardaron sin errores, false si falló alguno
     */
    boolean guardar();
}

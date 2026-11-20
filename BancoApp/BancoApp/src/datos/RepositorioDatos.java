package datos;

/**
 * Interfaz para definir las operaciones basicas de persistencia (cargar y guardar).
 * Desacopla los controladores de la implementacion especifica en archivos CSV.
 */
public interface RepositorioDatos {

    /**
     * Lee los datos desde el almacenamiento y los carga en el contexto en memoria.
     */
    void cargar();

    /**
     * Guarda el estado actual del banco en el almacenamiento.
     * @return true si todos los archivos se guardaron sin errores, false si fallo alguno.
     */
    boolean guardar();
}
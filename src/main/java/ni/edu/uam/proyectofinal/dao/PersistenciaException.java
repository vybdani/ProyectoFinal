package ni.edu.uam.proyectofinal.dao;

/**
 * Error de acceso a datos con un mensaje pensado para mostrarse al usuario.
 * La causa técnica (excepción de Hibernate o de PostgreSQL) queda guardada en getCause().
 */
public class PersistenciaException extends RuntimeException {

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}

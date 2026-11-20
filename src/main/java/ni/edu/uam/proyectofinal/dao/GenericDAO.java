package ni.edu.uam.proyectofinal.dao;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import ni.edu.uam.proyectofinal.util.HibernateUtil;
import org.hibernate.PropertyValueException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.JDBCConnectionException;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * CRUD común a todas las entidades con Hibernate.
 *
 * <ul>
 *   <li>Cada operación abre su propia sesión y la cierra al terminar (try-with-resources).</li>
 *   <li>Las escrituras van dentro de una transacción: commit si todo sale bien y rollback
 *       si algo falla, siempre con la sesión todavía abierta.</li>
 *   <li>Las consultas usan parámetros con nombre, nunca valores concatenados.</li>
 * </ul>
 *
 * @param <T> entidad JPA que maneja el DAO
 */
public abstract class GenericDAO<T> {

    private final Class<T> tipo;
    private final String nombreEntidad;

    /**
     * @param tipo          clase de la entidad
     * @param nombreEntidad nombre para los mensajes de error, por ejemplo "el ejercicio"
     */
    protected GenericDAO(Class<T> tipo, String nombreEntidad) {
        this.tipo = tipo;
        this.nombreEntidad = nombreEntidad;
    }

    // ---------------------------------------------------------------- CRUD

    /** INSERT: guarda una entidad nueva y la devuelve con su id generado. */
    public T guardar(T entidad) {
        return enTransaccion("guardar " + nombreEntidad, session -> {
            session.persist(entidad);
            return entidad;
        });
    }

    /** SELECT por clave primaria; devuelve null si no existe. */
    public T buscarPorId(Long id) {
        return consultar("buscar " + nombreEntidad, session -> session.get(tipo, id));
    }

    /** SELECT de todos los registros. */
    public List<T> listarTodos() {
        return consultar("listar " + nombreEntidad, session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<T> consulta = cb.createQuery(tipo);
            consulta.select(consulta.from(tipo));
            return session.createQuery(consulta).list();
        });
    }

    /** UPDATE: copia los cambios de una entidad (desconectada) a la base de datos. */
    public T actualizar(T entidad) {
        return enTransaccion("actualizar " + nombreEntidad, session -> session.merge(entidad));
    }

    /** DELETE por id. Las relaciones con cascade se borran junto con la entidad. */
    public void eliminar(Long id) {
        ejecutar("eliminar " + nombreEntidad, session -> {
            T entidad = session.get(tipo, id);
            if (entidad == null) {
                throw new IllegalArgumentException("No se encontró " + nombreEntidad + " que se quiere eliminar.");
            }
            session.remove(entidad);
        });
    }

    // ------------------------------------------------- Ayudas para los DAO

    /** Ejecuta una escritura dentro de una transacción y devuelve un resultado. */
    protected <R> R enTransaccion(String accion, Function<Session, R> trabajo) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaccion = session.beginTransaction();
            try {
                R resultado = trabajo.apply(session);
                transaccion.commit();
                return resultado;
            } catch (RuntimeException e) {
                if (transaccion.isActive()) {
                    transaccion.rollback();
                }
                throw traducir(accion, e);
            }
        }
    }

    /** Ejecuta una escritura dentro de una transacción sin devolver nada. */
    protected void ejecutar(String accion, Consumer<Session> trabajo) {
        enTransaccion(accion, session -> {
            trabajo.accept(session);
            return null;
        });
    }

    /** Ejecuta una consulta de solo lectura (no necesita transacción). */
    protected <R> R consultar(String accion, Function<Session, R> trabajo) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return trabajo.apply(session);
        } catch (RuntimeException e) {
            throw traducir(accion, e);
        }
    }

    /**
     * Las reglas del negocio (IllegalArgumentException / IllegalStateException) se dejan pasar
     * tal cual porque su mensaje ya es para el usuario; los errores técnicos se envuelven.
     */
    private RuntimeException traducir(String accion, RuntimeException e) {
        if (e instanceof IllegalArgumentException || e instanceof IllegalStateException
                || e instanceof PersistenciaException) {
            return e;
        }
        for (Throwable causa = e; causa != null; causa = causa.getCause()) {
            if (causa instanceof ConstraintViolationException || causa instanceof PropertyValueException) {
                return new PersistenciaException("No se pudo " + accion
                        + ": hay un dato repetido o falta un campo obligatorio.", e);
            }
            if (causa instanceof JDBCConnectionException) {
                return new PersistenciaException("No se pudo " + accion
                        + ": no hay conexión con la base de datos PostgreSQL.", e);
            }
        }
        return new PersistenciaException("No se pudo " + accion + ". Inténtalo de nuevo.", e);
    }
}

package ni.edu.uam.proyectofinal.util;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
public class HibernateUtil {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            // Crea la SessionFactory a partir de hibernate.cfg.xml
            return new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            // Maneja la excepción si ocurre un error al crear la SessionFactory
            System.err.println("Error al crear la SessionFactory: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
    public static void shutdown() {
        // Cierra la SessionFactory
        getSessionFactory().close();
    }
}

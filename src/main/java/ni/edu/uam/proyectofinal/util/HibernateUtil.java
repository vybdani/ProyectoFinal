package ni.edu.uam.proyectofinal.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            // Crea la SessionFactory a partir de hibernate.cfg.xml
            Configuration configuration = new Configuration().configure();

            // Las credenciales reales se leen de variables de entorno para no subirlas a GitHub.
            // Si no están definidas, se usan los valores de hibernate.cfg.xml.
            aplicarVariable(configuration, "DB_URL", "hibernate.connection.url");
            aplicarVariable(configuration, "DB_USER", "hibernate.connection.username");
            aplicarVariable(configuration, "DB_PASSWORD", "hibernate.connection.password");

            return configuration.buildSessionFactory();
        } catch (Throwable ex) {
            // Maneja la excepción si ocurre un error al crear la SessionFactory
            System.err.println("Error al crear la SessionFactory: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    private static void aplicarVariable(Configuration configuration, String variable, String propiedad) {
        String valor = System.getenv(variable);
        if (valor != null && !valor.isBlank()) {
            configuration.setProperty(propiedad, valor);
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

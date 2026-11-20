package ni.edu.uam.proyectofinal.util;

import ni.edu.uam.proyectofinal.dao.PersistenciaException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.net.ConnectException;
import java.sql.SQLException;

/**
 * Única SessionFactory de la aplicación (se crea una sola vez y se comparte).
 *
 * <ul>
 *   <li>Se crea la primera vez que se necesita. Si PostgreSQL no está disponible, lanza una
 *       {@link PersistenciaException} con el motivo, y el siguiente intento vuelve a probar
 *       (así la app se recupera en cuanto se enciende el servidor, sin reiniciarla).</li>
 *   <li>Las credenciales reales se leen de las variables de entorno DB_URL, DB_USER y DB_PASSWORD
 *       para no subirlas a GitHub; si no existen se usan los valores de hibernate.cfg.xml.</li>
 * </ul>
 */
public final class HibernateUtil {

    private static volatile SessionFactory sessionFactory;

    private HibernateUtil() {
    }

    public static SessionFactory getSessionFactory() {
        SessionFactory actual = sessionFactory;
        if (actual == null) {
            synchronized (HibernateUtil.class) {
                actual = sessionFactory;
                if (actual == null) {
                    actual = construir();
                    sessionFactory = actual;
                }
            }
        }
        return actual;
    }

    /** Intenta conectarse; devuelve null si todo está bien o el motivo del fallo para mostrarlo. */
    public static String verificarConexion() {
        try {
            getSessionFactory();
            return null;
        } catch (PersistenciaException e) {
            return e.getMessage();
        }
    }

    public static synchronized void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
            sessionFactory = null;
        }
    }

    private static SessionFactory construir() {
        Configuration configuracion = new Configuration().configure();
        aplicarVariable(configuracion, "DB_URL", "hibernate.connection.url");
        aplicarVariable(configuracion, "DB_USER", "hibernate.connection.username");
        aplicarVariable(configuracion, "DB_PASSWORD", "hibernate.connection.password");
        String url = configuracion.getProperty("hibernate.connection.url");
        if (url == null) {
            url = configuracion.getProperty("connection.url");
        }
        try {
            return configuracion.buildSessionFactory();
        } catch (RuntimeException e) {
            System.err.println("No se pudo crear la SessionFactory: " + e);
            throw new PersistenciaException(diagnosticar(e, url), e);
        }
    }

    /** Traduce el error de conexión a un mensaje que diga qué hacer. */
    static String diagnosticar(Throwable error, String url) {
        String destino = url != null ? url.replace("jdbc:postgresql://", "") : "la base de datos";
        for (Throwable causa = error; causa != null; causa = causa.getCause()) {
            if (causa instanceof SQLException sql && sql.getSQLState() != null) {
                switch (sql.getSQLState()) {
                    case "28P01", "28000":
                        return "PostgreSQL rechazó el usuario o la contraseña. Define la variable de entorno "
                                + "DB_PASSWORD con tu contraseña de PostgreSQL (ver README) y vuelve a intentarlo.";
                    case "3D000":
                        return "La base de datos no existe (" + destino + "). Créala en pgAdmin o psql con: "
                                + "CREATE DATABASE solea_db;";
                    case "08004":
                        String detalle = sql.getMessage() == null ? "" : sql.getMessage().toLowerCase();
                        if (detalle.contains("password") || detalle.contains("authentication")) {
                            return "PostgreSQL pide una contraseña y no se envió ninguna. Define la variable de "
                                    + "entorno DB_PASSWORD con tu contraseña de PostgreSQL (ver README).";
                        }
                        return "PostgreSQL rechazó la conexión a " + destino + ". Revisa usuario, contraseña y puerto.";
                    case "08001", "08006":
                        return "No hay un servidor PostgreSQL respondiendo en " + destino
                                + ". Revisa que el servicio de PostgreSQL esté encendido y el puerto sea el correcto.";
                    default:
                        break;
                }
            }
            if (causa instanceof ConnectException) {
                return "No hay un servidor PostgreSQL respondiendo en " + destino
                        + ". Revisa que el servicio de PostgreSQL esté encendido y el puerto sea el correcto.";
            }
        }
        return "No se pudo conectar con la base de datos (" + destino + "). Revisa la configuración del README.";
    }

    private static void aplicarVariable(Configuration configuracion, String variable, String propiedad) {
        String valor = System.getenv(variable);
        if (valor != null && !valor.isBlank()) {
            configuracion.setProperty(propiedad, valor);
        }
    }
}

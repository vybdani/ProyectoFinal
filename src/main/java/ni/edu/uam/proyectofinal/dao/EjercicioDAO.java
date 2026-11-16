package ni.edu.uam.proyectofinal.dao;

import ni.edu.uam.proyectofinal.model.Ejercicio;
import ni.edu.uam.proyectofinal.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class EjercicioDAO {

    public void guardar(Ejercicio ejercicio) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(ejercicio);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al guardar el ejercicio", e);
        }
    }

    public void actualizar(Ejercicio ejercicio) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(ejercicio);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al actualizar el ejercicio", e);
        }
    }

    public void eliminar(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long usos = session.createQuery(
                            "SELECT COUNT(re) FROM RutinaEjercicio re WHERE re.ejercicio.id = :id", Long.class)
                    .setParameter("id", id)
                    .uniqueResult();
            if (usos != null && usos > 0) {
                // Integridad referencial: no se borra un ejercicio que forma parte de una rutina
                throw new IllegalStateException("El ejercicio está en " + usos
                        + " rutina(s) y no se puede eliminar. Puedes editarlo en su lugar.");
            }

            transaction = session.beginTransaction();
            Ejercicio ejercicio = session.get(Ejercicio.class, id);
            if (ejercicio != null) {
                session.remove(ejercicio);
            }
            transaction.commit();
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al eliminar el ejercicio", e);
        }
    }

    /** HQL con JOIN: ejercicios con su grupo muscular, ordenados por grupo. */
    public List<Ejercicio> listarTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = """
                SELECT e FROM Ejercicio e
                JOIN FETCH e.grupoMuscular g
                ORDER BY g.nombre, e.nombre
                """;
            return session.createQuery(hql, Ejercicio.class).list();
        }
    }

    /** HQL con JOIN: ejercicios de un grupo muscular por su nombre. */
    public List<Ejercicio> listarPorGrupo(String nombreGrupo) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = """
                SELECT e FROM Ejercicio e
                JOIN FETCH e.grupoMuscular g
                WHERE g.nombre = :grupo
                ORDER BY e.id
                """;
            return session.createQuery(hql, Ejercicio.class)
                    .setParameter("grupo", nombreGrupo)
                    .list();
        }
    }
}

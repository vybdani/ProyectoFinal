package ni.edu.uam.proyectofinal.dao;

import ni.edu.uam.proyectofinal.model.Rutina;
import ni.edu.uam.proyectofinal.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class RutinaDAO {

    public void guardar(Rutina rutina) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(rutina);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al guardar la rutina", e);
        }
    }

    public void actualizarNombre(Long rutinaId, String nombre) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Rutina rutina = session.get(Rutina.class, rutinaId);
            if (rutina != null) {
                rutina.setNombre(nombre);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al actualizar la rutina", e);
        }
    }

    public void eliminar(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Rutina rutina = session.get(Rutina.class, id);
            if (rutina != null) {
                session.remove(rutina); // cascade borra también sus RutinaEjercicio
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al eliminar la rutina", e);
        }
    }

    /**
     * HQL con JOIN FETCH: la rutina más reciente del usuario con sus ejercicios
     * y el grupo muscular de cada uno.
     */
    public Rutina buscarUltimaPorUsuario(Long usuarioId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = """
                SELECT DISTINCT r FROM Rutina r
                LEFT JOIN FETCH r.ejercicios re
                LEFT JOIN FETCH re.ejercicio e
                LEFT JOIN FETCH e.grupoMuscular
                WHERE r.usuario.id = :usuarioId
                ORDER BY r.fechaCreacion DESC, r.id DESC
                """;
            List<Rutina> rutinas = session.createQuery(hql, Rutina.class)
                    .setParameter("usuarioId", usuarioId)
                    .list();
            return rutinas.isEmpty() ? null : rutinas.get(0);
        }
    }

    /**
     * HQL de resumen: cuántos ejercicios y series semanales tiene cada grupo muscular
     * en una rutina (RutinaEjercicio → Ejercicio → GrupoMuscular).
     */
    public List<Object[]> resumenPorGrupoMuscular(Long rutinaId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = """
                SELECT g.nombre, COUNT(re), SUM(re.series)
                FROM RutinaEjercicio re
                JOIN re.ejercicio e
                JOIN e.grupoMuscular g
                WHERE re.rutina.id = :rutinaId
                GROUP BY g.nombre
                ORDER BY SUM(re.series) DESC
                """;
            return session.createQuery(hql, Object[].class)
                    .setParameter("rutinaId", rutinaId)
                    .list();
        }
    }
}

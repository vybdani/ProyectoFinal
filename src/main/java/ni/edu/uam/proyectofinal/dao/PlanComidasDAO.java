package ni.edu.uam.proyectofinal.dao;

import ni.edu.uam.proyectofinal.model.PlanComidas;
import ni.edu.uam.proyectofinal.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class PlanComidasDAO {

    public void guardar(PlanComidas plan) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(plan);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al guardar el plan de comidas", e);
        }
    }

    public PlanComidas buscarUltimoPorUsuario(Long usuarioId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = """
                SELECT DISTINCT p FROM PlanComidas p
                LEFT JOIN FETCH p.comidas c
                LEFT JOIN FETCH c.receta
                WHERE p.usuario.id = :usuarioId
                ORDER BY p.fechaInicio DESC, p.id DESC
                """;
            List<PlanComidas> planes = session.createQuery(hql, PlanComidas.class)
                    .setParameter("usuarioId", usuarioId)
                    .list();
            return planes.isEmpty() ? null : planes.get(0);
        }
    }

    public void eliminar(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            PlanComidas plan = session.get(PlanComidas.class, id);
            if (plan != null) {
                session.remove(plan);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al eliminar el plan de comidas", e);
        }
    }
}
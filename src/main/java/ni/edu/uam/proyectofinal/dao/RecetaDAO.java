package ni.edu.uam.proyectofinal.dao;

import ni.edu.uam.proyectofinal.model.Receta;
import ni.edu.uam.proyectofinal.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class RecetaDAO {

    public void guardar(Receta receta) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(receta);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al guardar la receta", e);
        }
    }

    public void actualizar(Receta receta) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(receta);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al actualizar la receta", e);
        }
    }

    public void eliminar(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Receta receta = session.get(Receta.class, id);
            if (receta != null) {
                session.remove(receta);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al eliminar la receta", e);
        }
    }

    // HQL 1: Recupera las recetas con sus ingredientes y alimentos asociados
    public List<Receta> listarTodas() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = """
                SELECT DISTINCT r FROM Receta r
                LEFT JOIN FETCH r.ingredientes ri
                LEFT JOIN FETCH ri.alimento
                ORDER BY r.nombre ASC
                """;
            return session.createQuery(hql, Receta.class).list();
        }
    }

    // HQL 2 (Rúbrica): Calcula las kcal y macros totales por receta desde sus ingredientes
    public List<Object[]> obtenerResumenCaloricoRecetas() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = """
                SELECT r.id, r.nombre, r.tipoComida,
                       COALESCE(SUM((ri.cantidadGramos / a.cantidadBase) * a.calorias), 0.0),
                       COALESCE(SUM((ri.cantidadGramos / a.cantidadBase) * a.proteinas), 0.0),
                       COALESCE(SUM((ri.cantidadGramos / a.cantidadBase) * a.carbohidratos), 0.0),
                       COALESCE(SUM((ri.cantidadGramos / a.cantidadBase) * a.grasas), 0.0)
                FROM Receta r
                LEFT JOIN r.ingredientes ri
                LEFT JOIN ri.alimento a
                GROUP BY r.id, r.nombre, r.tipoComida
                ORDER BY r.nombre ASC
                """;
            return session.createQuery(hql, Object[].class).list();
        }
    }
}

package ni.edu.uam.nutrifit.dao;

import ni.edu.uam.nutrifit.entity.RegistroConsumo;
import ni.edu.uam.nutrifit.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDate;
import java.util.List;

public class RegistroConsumoDAO {

    public void guardar(RegistroConsumo registro) {

        Transaction transaction = null;

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            transaction = session.beginTransaction();

            session.persist(registro);

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Error al registrar consumo", e);
        }
    }

    // Listar alimentos consumidos en el día
    public List<RegistroConsumo> listarConsumoDiario(
            Long usuarioId, LocalDate fecha) {

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            String hql = """
                FROM RegistroConsumo r
                WHERE r.usuario.id = :usuarioId
                AND r.fecha = :fecha
                ORDER BY r.hora DESC
                """;

            return session.createQuery(
                            hql, RegistroConsumo.class)
                    .setParameter("usuarioId", usuarioId)
                    .setParameter("fecha", fecha)
                    .list();
        }
    }

    // Obtener suma de calorías y macronutrientes
    public Object[] obtenerTotales(Long usuarioId, LocalDate fecha) {

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            String hql = """
                SELECT
                    COALESCE(SUM(r.caloriasConsumidas), 0.0),
                    COALESCE(SUM(r.proteinasConsumidas), 0.0),
                    COALESCE(SUM(r.carbohidratosConsumidos), 0.0),
                    COALESCE(SUM(r.grasasConsumidas), 0.0)
                FROM RegistroConsumo r
                WHERE r.usuario.id = :usuarioId
                AND r.fecha = :fecha
                """;

            return session.createQuery(hql, Object[].class)
                    .setParameter("usuarioId", usuarioId)
                    .setParameter("fecha", fecha)
                    .uniqueResult();
        }
    }

    public void eliminar(Long id) {

        Transaction transaction = null;

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            transaction = session.beginTransaction();

            RegistroConsumo registro =
                    session.get(RegistroConsumo.class, id);

            if (registro != null) {
                session.remove(registro);
            }

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Error al eliminar consumo", e);
        }
    }
}


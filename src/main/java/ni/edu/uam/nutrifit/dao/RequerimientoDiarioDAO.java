package ni.edu.uam.nutrifit.dao;

import ni.edu.uam.nutrifit.entity.RequerimientoDiario;
import ni.edu.uam.nutrifit.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDate;

public class RequerimientoDiarioDAO {

    public void guardar(RequerimientoDiario requerimiento) {

        Transaction transaction = null;

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            transaction = session.beginTransaction();

            session.persist(requerimiento);

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Error al guardar requerimiento", e);
        }
    }

    public RequerimientoDiario buscarPorUsuario(Long usuarioId) {

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            String hql = """
                FROM RequerimientoDiario r
                WHERE r.usuario.id = :usuarioId
                ORDER BY r.fecha DESC, r.id DESC
                """;

            return session.createQuery(
                            hql, RequerimientoDiario.class)
                    .setParameter("usuarioId", usuarioId)
                    .setMaxResults(1)
                    .uniqueResult();
        }
    }

    public RequerimientoDiario buscarDelDia(Long usuarioId) {

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            String hql = """
                FROM RequerimientoDiario r
                WHERE r.usuario.id = :usuarioId
                AND r.fecha = :fecha
                ORDER BY r.id DESC
                """;

            return session.createQuery(
                            hql, RequerimientoDiario.class)
                    .setParameter("usuarioId", usuarioId)
                    .setParameter("fecha", LocalDate.now())
                    .setMaxResults(1)
                    .uniqueResult();
        }
    }
}


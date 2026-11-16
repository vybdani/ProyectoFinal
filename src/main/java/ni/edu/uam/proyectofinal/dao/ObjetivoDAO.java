package ni.edu.uam.proyectofinal.dao;

import ni.edu.uam.proyectofinal.model.Objetivo;
import ni.edu.uam.proyectofinal.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class ObjetivoDAO {

    /** Inserta el objetivo si es nuevo o lo actualiza si ya existe. */
    public Objetivo guardar(Objetivo objetivo) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Objetivo guardado;
            if (objetivo.getId() == null) {
                session.persist(objetivo);
                guardado = objetivo;
            } else {
                guardado = session.merge(objetivo);
            }
            transaction.commit();
            return guardado;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al guardar el objetivo", e);
        }
    }

    /** Objetivo vigente del usuario para un modo (NUTRICION o GIMNASIO). */
    public Objetivo buscarPorUsuarioYModo(Long usuarioId, String modo) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = """
                FROM Objetivo o
                WHERE o.usuario.id = :usuarioId
                AND o.modo = :modo
                ORDER BY o.id DESC
                """;
            return session.createQuery(hql, Objetivo.class)
                    .setParameter("usuarioId", usuarioId)
                    .setParameter("modo", modo)
                    .setMaxResults(1)
                    .uniqueResult();
        }
    }

    /** HQL con JOIN: objetivos de un usuario junto con su nombre, para mostrar el historial. */
    public List<Object[]> listarObjetivosConUsuario(Long usuarioId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = """
                SELECT u.nombre, o.modo, o.tipo, o.caloriasDiarias
                FROM Objetivo o
                JOIN o.usuario u
                WHERE u.id = :usuarioId
                ORDER BY o.id DESC
                """;
            return session.createQuery(hql, Object[].class)
                    .setParameter("usuarioId", usuarioId)
                    .list();
        }
    }
}

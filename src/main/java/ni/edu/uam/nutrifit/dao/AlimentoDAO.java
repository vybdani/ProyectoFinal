package ni.edu.uam.nutrifit.dao;

import ni.edu.uam.nutrifit.entity.Alimento;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class AlimentoDAO {

    private ni.edu.uam.proyectofinal.util.HibernateUtil HibernateUtil;

    public void guardar(Alimento alimento) {

        Transaction transaction = null;

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            transaction = session.beginTransaction();

            session.persist(alimento);

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }

            throw new RuntimeException("Error al guardar alimento", e);
        }
    }

    public List<Alimento> listarTodos() {

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            String hql = """
                FROM Alimento a
                ORDER BY a.nombre ASC
                """;

            return session.createQuery(hql, Alimento.class).list();
        }
    }

    public List<Alimento> buscarPorNombre(String nombre) {

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            String hql = """
                FROM Alimento a
                WHERE LOWER(a.nombre) LIKE LOWER(:nombre)
                """;

            return session.createQuery(hql, Alimento.class)
                    .setParameter("nombre", "%" + nombre + "%")
                    .list();
        }
    }

    public List<Alimento> buscarPorCategoria(String categoria) {

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            String hql = """
                FROM Alimento a
                WHERE a.categoria = :categoria
                """;

            return session.createQuery(hql, Alimento.class)
                    .setParameter("categoria", categoria)
                    .list();
        }
    }

    public Alimento buscarPorId(Long id) {

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            return session.get(Alimento.class, id);
        }
    }
}


package ni.edu.uam.proyectofinal.dao;

import ni.edu.uam.proyectofinal.model.ItemListaSuper;
import ni.edu.uam.proyectofinal.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class ItemListaSuperDAO {

    public void guardar(ItemListaSuper item) {
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(item);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al guardar producto", e);
        }
    }

    public List<ItemListaSuper> listarPorUsuario(Long usuarioId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = """
                FROM ItemListaSuper i
                WHERE i.usuario.id = :usuarioId
                ORDER BY i.comprado ASC, i.nombreProducto ASC
                """;
            return session.createQuery(hql, ItemListaSuper.class)
                    .setParameter("usuarioId", usuarioId)
                    .list();
        }
    }

    public void actualizar(ItemListaSuper item) {
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(item);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al actualizar producto", e);
        }
    }

    public void eliminar(Long id) {
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            ItemListaSuper item = session.get(ItemListaSuper.class, id);
            if (item != null) {
                session.remove(item);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al eliminar producto", e);
        }
    }
}
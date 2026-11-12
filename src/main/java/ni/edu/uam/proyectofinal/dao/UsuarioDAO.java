package ni.edu.uam.proyectofinal.dao;

import ni.edu.uam.proyectofinal.model.Usuario;
import ni.edu.uam.proyectofinal.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

public class UsuarioDAO {

    public void guardar(Usuario usuario) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(usuario);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al guardar el usuario", e);
        }
    }

    public void actualizar(Usuario usuario) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(usuario);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error al actualizar el usuario", e);
        }
    }

    public Usuario autenticar(String correo, String password) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = """
                SELECT u FROM Usuario u
                LEFT JOIN FETCH u.perfilFisico
                WHERE u.correo = :correo AND u.password = :password
                """;
            Query<Usuario> query = session.createQuery(hql, Usuario.class);
            query.setParameter("correo", correo);
            query.setParameter("password", password);
            return query.uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException("Error al autenticar usuario", e);
        }
    }

    public Usuario obtenerUsuarioConPerfil(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT u FROM Usuario u LEFT JOIN FETCH u.perfilFisico WHERE u.id = :id";
            Query<Usuario> query = session.createQuery(hql, Usuario.class);
            query.setParameter("id", id);
            return query.uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener usuario con perfil", e);
        }
    }
}
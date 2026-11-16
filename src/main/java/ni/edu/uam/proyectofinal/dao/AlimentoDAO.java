package ni.edu.uam.proyectofinal.dao;

import ni.edu.uam.proyectofinal.model.Alimento;
import ni.edu.uam.proyectofinal.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class AlimentoDAO {

    public void guardar(Alimento alimento) {
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
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

    /** Si la tabla de alimentos está vacía, carga una lista básica (valores por 100 g). */
    public void asegurarAlimentosIniciales() {
        if (!listarTodos().isEmpty()) {
            return;
        }
        guardar(new Alimento("Pechuga de Pollo", "Proteína", 165.0, 31.0, 0.0, 3.6));
        guardar(new Alimento("Huevo Entero", "Proteína", 155.0, 13.0, 1.1, 11.0));
        guardar(new Alimento("Atún en Agua", "Proteína", 116.0, 26.0, 0.0, 1.0));
        guardar(new Alimento("Carne de Res Magra", "Proteína", 217.0, 26.0, 0.0, 12.0));
        guardar(new Alimento("Frijoles Rojos Cocidos", "Legumbre", 127.0, 8.7, 22.8, 0.5));
        guardar(new Alimento("Arroz Blanco Cocido", "Carbohidrato", 130.0, 2.7, 28.0, 0.3));
        guardar(new Alimento("Avena en Hojuelas", "Carbohidrato", 389.0, 16.9, 66.3, 6.9));
        guardar(new Alimento("Pan Integral", "Carbohidrato", 247.0, 13.0, 41.0, 3.4));
        guardar(new Alimento("Papa Cocida", "Carbohidrato", 87.0, 1.9, 20.1, 0.1));
        guardar(new Alimento("Tortilla de Maíz", "Carbohidrato", 218.0, 5.7, 44.6, 2.9));
        guardar(new Alimento("Leche Descremada", "Lácteo", 34.0, 3.4, 5.0, 0.1));
        guardar(new Alimento("Yogur Griego Natural", "Lácteo", 59.0, 10.0, 3.6, 0.4));
        guardar(new Alimento("Queso Fresco", "Lácteo", 264.0, 18.0, 3.0, 20.0));
        guardar(new Alimento("Aguacate", "Grasa", 160.0, 2.0, 8.5, 14.7));
        guardar(new Alimento("Maní", "Grasa", 567.0, 25.8, 16.1, 49.2));
        guardar(new Alimento("Aceite de Oliva", "Grasa", 884.0, 0.0, 0.0, 100.0));
        guardar(new Alimento("Banano", "Fruta", 89.0, 1.1, 22.8, 0.3));
        guardar(new Alimento("Manzana", "Fruta", 52.0, 0.3, 13.8, 0.2));
        guardar(new Alimento("Brócoli", "Verdura", 34.0, 2.8, 6.6, 0.4));
        guardar(new Alimento("Tomate", "Verdura", 18.0, 0.9, 3.9, 0.2));
    }

    public List<Alimento> listarTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = """
                FROM Alimento a
                ORDER BY a.nombre ASC
                """;
            return session.createQuery(hql, Alimento.class).list();
        }
    }

    public List<Alimento> buscarPorNombre(String nombre) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
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
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
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
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Alimento.class, id);
        }
    }
}
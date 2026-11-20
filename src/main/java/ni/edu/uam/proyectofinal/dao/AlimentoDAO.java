package ni.edu.uam.proyectofinal.dao;

import ni.edu.uam.proyectofinal.model.Alimento;

import java.util.List;

public class AlimentoDAO extends GenericDAO<Alimento> {

    public AlimentoDAO() {
        super(Alimento.class, "el alimento");
    }

    /** Si la tabla de alimentos está vacía, carga una lista básica (valores por 100 g) en una sola transacción. */
    public void asegurarAlimentosIniciales() {
        Long cantidad = consultar("contar los alimentos", session ->
                session.createQuery("SELECT COUNT(a) FROM Alimento a", Long.class).uniqueResult());
        if (cantidad != null && cantidad > 0) {
            return;
        }
        ejecutar("cargar los alimentos iniciales", session -> {
            session.persist(new Alimento("Pechuga de Pollo", "Proteína", 165.0, 31.0, 0.0, 3.6));
            session.persist(new Alimento("Huevo Entero", "Proteína", 155.0, 13.0, 1.1, 11.0));
            session.persist(new Alimento("Atún en Agua", "Proteína", 116.0, 26.0, 0.0, 1.0));
            session.persist(new Alimento("Carne de Res Magra", "Proteína", 217.0, 26.0, 0.0, 12.0));
            session.persist(new Alimento("Frijoles Rojos Cocidos", "Legumbre", 127.0, 8.7, 22.8, 0.5));
            session.persist(new Alimento("Arroz Blanco Cocido", "Carbohidrato", 130.0, 2.7, 28.0, 0.3));
            session.persist(new Alimento("Avena en Hojuelas", "Carbohidrato", 389.0, 16.9, 66.3, 6.9));
            session.persist(new Alimento("Pan Integral", "Carbohidrato", 247.0, 13.0, 41.0, 3.4));
            session.persist(new Alimento("Papa Cocida", "Carbohidrato", 87.0, 1.9, 20.1, 0.1));
            session.persist(new Alimento("Tortilla de Maíz", "Carbohidrato", 218.0, 5.7, 44.6, 2.9));
            session.persist(new Alimento("Leche Descremada", "Lácteo", 34.0, 3.4, 5.0, 0.1));
            session.persist(new Alimento("Yogur Griego Natural", "Lácteo", 59.0, 10.0, 3.6, 0.4));
            session.persist(new Alimento("Queso Fresco", "Lácteo", 264.0, 18.0, 3.0, 20.0));
            session.persist(new Alimento("Aguacate", "Grasa", 160.0, 2.0, 8.5, 14.7));
            session.persist(new Alimento("Maní", "Grasa", 567.0, 25.8, 16.1, 49.2));
            session.persist(new Alimento("Aceite de Oliva", "Grasa", 884.0, 0.0, 0.0, 100.0));
            session.persist(new Alimento("Banano", "Fruta", 89.0, 1.1, 22.8, 0.3));
            session.persist(new Alimento("Manzana", "Fruta", 52.0, 0.3, 13.8, 0.2));
            session.persist(new Alimento("Brócoli", "Verdura", 34.0, 2.8, 6.6, 0.4));
            session.persist(new Alimento("Tomate", "Verdura", 18.0, 0.9, 3.9, 0.2));
        });
    }

    /** Todos los alimentos ordenados por nombre. */
    @Override
    public List<Alimento> listarTodos() {
        return consultar("listar los alimentos", session ->
                session.createQuery("FROM Alimento a ORDER BY a.nombre ASC", Alimento.class).list());
    }

    public List<Alimento> buscarPorNombre(String nombre) {
        return consultar("buscar alimentos", session -> session.createQuery("""
                        FROM Alimento a
                        WHERE LOWER(a.nombre) LIKE LOWER(:nombre)
                        ORDER BY a.nombre ASC
                        """, Alimento.class)
                .setParameter("nombre", "%" + nombre + "%")
                .list());
    }

    /** Alimento con exactamente ese nombre (sin distinguir mayúsculas), o null. */
    public Alimento buscarPorNombreExacto(String nombre) {
        return consultar("buscar el alimento", session -> session.createQuery(
                        "FROM Alimento a WHERE lower(a.nombre) = lower(:nombre)", Alimento.class)
                .setParameter("nombre", nombre.trim())
                .setMaxResults(1)
                .uniqueResult());
    }

    public List<Alimento> buscarPorCategoria(String categoria) {
        return consultar("buscar alimentos", session -> session.createQuery("""
                        FROM Alimento a
                        WHERE a.categoria = :categoria
                        ORDER BY a.nombre ASC
                        """, Alimento.class)
                .setParameter("categoria", categoria)
                .list());
    }

    /** No se borra un alimento que ya se usa en recetas o en el registro de consumo. */
    @Override
    public void eliminar(Long id) {
        Long usos = consultar("verificar el alimento", session -> session.createQuery("""
                        SELECT (SELECT COUNT(ri) FROM RecetaIngrediente ri WHERE ri.alimento.id = :id)
                             + (SELECT COUNT(rc) FROM RegistroConsumo rc WHERE rc.alimento.id = :id)
                        FROM Alimento a WHERE a.id = :id
                        """, Long.class)
                .setParameter("id", id)
                .uniqueResult());
        if (usos != null && usos > 0) {
            throw new IllegalStateException("El alimento se usa en " + usos
                    + " receta(s) o registro(s) de consumo y no se puede eliminar.");
        }
        super.eliminar(id);
    }
}

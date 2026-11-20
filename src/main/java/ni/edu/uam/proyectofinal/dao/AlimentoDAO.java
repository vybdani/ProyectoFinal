package ni.edu.uam.proyectofinal.dao;

import ni.edu.uam.proyectofinal.model.Alimento;

import java.util.List;

public class AlimentoDAO extends GenericDAO<Alimento> {

    public AlimentoDAO() {
        super(Alimento.class, "el alimento");
    }

    /**
     * Carga los alimentos comunes de {@link DatosIniciales} que todavía no existan (comparando por
     * nombre, sin distinguir mayúsculas), todo en una sola transacción. Devuelve cuántos agregó.
     */
    public int asegurarAlimentosIniciales() {
        return enTransaccion("cargar los alimentos iniciales", session -> {
            java.util.Set<String> existentes = new java.util.HashSet<>(session.createQuery(
                    "SELECT lower(a.nombre) FROM Alimento a", String.class).list());
            int agregados = 0;
            for (DatosIniciales.AlimentoBase a : DatosIniciales.ALIMENTOS) {
                if (existentes.add(a.nombre().toLowerCase())) {
                    session.persist(new Alimento(a.nombre(), a.categoria(), a.calorias(),
                            a.proteinas(), a.carbohidratos(), a.grasas()));
                    agregados++;
                }
            }
            return agregados;
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

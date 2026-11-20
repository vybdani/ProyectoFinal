package ni.edu.uam.proyectofinal.dao;

import ni.edu.uam.proyectofinal.model.Receta;

import java.util.List;

public class RecetaDAO extends GenericDAO<Receta> {

    public RecetaDAO() {
        super(Receta.class, "la receta");
    }

    /** HQL con JOIN FETCH: recetas con sus ingredientes y el alimento de cada uno. */
    public List<Receta> listarTodas() {
        return consultar("listar las recetas", session -> session.createQuery("""
                        SELECT DISTINCT r FROM Receta r
                        LEFT JOIN FETCH r.ingredientes ri
                        LEFT JOIN FETCH ri.alimento
                        ORDER BY r.nombre ASC
                        """, Receta.class)
                .list());
    }

    /**
     * Carga las recetas de {@link DatosIniciales} que todavía no existan (por nombre) en una sola
     * transacción. Usa los alimentos ya guardados; una receta cuyo alimento falte se omite.
     * Devuelve cuántas recetas agregó.
     */
    public int asegurarRecetasIniciales() {
        return enTransaccion("cargar las recetas iniciales", session -> {
            java.util.Set<String> existentes = new java.util.HashSet<>(session.createQuery(
                    "SELECT lower(r.nombre) FROM Receta r", String.class).list());
            java.util.Map<String, ni.edu.uam.proyectofinal.model.Alimento> alimentos = new java.util.HashMap<>();
            session.createQuery("FROM Alimento a", ni.edu.uam.proyectofinal.model.Alimento.class).list()
                    .forEach(a -> alimentos.put(a.getNombre().toLowerCase(), a));

            int agregadas = 0;
            for (DatosIniciales.RecetaBase base : DatosIniciales.RECETAS) {
                if (existentes.contains(base.nombre().toLowerCase())) {
                    continue;
                }
                Receta receta = new Receta(base.nombre(), base.tipoComida(), base.descripcion(), base.minutos());
                boolean completa = true;
                for (DatosIniciales.IngredienteBase ing : base.ingredientes()) {
                    ni.edu.uam.proyectofinal.model.Alimento alimento = alimentos.get(ing.alimento().toLowerCase());
                    if (alimento == null) {
                        completa = false;
                        break;
                    }
                    receta.agregarIngrediente(new ni.edu.uam.proyectofinal.model.RecetaIngrediente(alimento, ing.gramos()));
                }
                if (completa) {
                    session.persist(receta); // cascade guarda también los ingredientes
                    existentes.add(base.nombre().toLowerCase());
                    agregadas++;
                }
            }
            return agregadas;
        });
    }

    /** Las recetas siempre se listan con sus ingredientes (la colección es LAZY). */
    @Override
    public List<Receta> listarTodos() {
        return listarTodas();
    }

    /** HQL de agregación: kcal y macros totales de cada receta calculados desde sus ingredientes. */
    public List<Object[]> obtenerResumenCaloricoRecetas() {
        return consultar("calcular el resumen de recetas", session -> session.createQuery("""
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
                        """, Object[].class)
                .list());
    }

    /** No se borra una receta que forma parte de un plan semanal. */
    @Override
    public void eliminar(Long id) {
        Long usos = consultar("verificar la receta", session -> session.createQuery(
                        "SELECT COUNT(c) FROM ComidaPlan c WHERE c.receta.id = :id", Long.class)
                .setParameter("id", id)
                .uniqueResult());
        if (usos != null && usos > 0) {
            throw new IllegalStateException("La receta está en " + usos
                    + " comida(s) de un plan semanal y no se puede eliminar.");
        }
        super.eliminar(id);
    }
}

package ni.edu.uam.proyectofinal.dao;

import ni.edu.uam.proyectofinal.model.Ejercicio;

import java.util.List;

public class EjercicioDAO extends GenericDAO<Ejercicio> {

    public EjercicioDAO() {
        super(Ejercicio.class, "el ejercicio");
    }

    /** Integridad referencial: no se borra un ejercicio que forma parte de una rutina. */
    @Override
    public void eliminar(Long id) {
        ejecutar("eliminar el ejercicio", session -> {
            Long usos = session.createQuery(
                            "SELECT COUNT(re) FROM RutinaEjercicio re WHERE re.ejercicio.id = :id", Long.class)
                    .setParameter("id", id)
                    .uniqueResult();
            if (usos != null && usos > 0) {
                throw new IllegalStateException("El ejercicio está en " + usos
                        + " rutina(s) y no se puede eliminar. Puedes editarlo en su lugar.");
            }
            Ejercicio ejercicio = session.get(Ejercicio.class, id);
            if (ejercicio == null) {
                throw new IllegalArgumentException("El ejercicio que se quiere eliminar no existe.");
            }
            session.remove(ejercicio);
        });
    }

    /** HQL con JOIN FETCH: ejercicios con su grupo muscular, ordenados por grupo. */
    @Override
    public List<Ejercicio> listarTodos() {
        return consultar("listar los ejercicios", session -> session.createQuery("""
                        SELECT e FROM Ejercicio e
                        JOIN FETCH e.grupoMuscular g
                        ORDER BY g.nombre, e.nombre
                        """, Ejercicio.class)
                .list());
    }

    /** HQL con JOIN FETCH: ejercicios de un grupo muscular por su nombre. */
    public List<Ejercicio> listarPorGrupo(String nombreGrupo) {
        return consultar("listar los ejercicios del grupo", session -> session.createQuery("""
                        SELECT e FROM Ejercicio e
                        JOIN FETCH e.grupoMuscular g
                        WHERE g.nombre = :grupo
                        ORDER BY e.id
                        """, Ejercicio.class)
                .setParameter("grupo", nombreGrupo)
                .list());
    }
}

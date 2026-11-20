package ni.edu.uam.proyectofinal.dao;

import ni.edu.uam.proyectofinal.model.Rutina;

import java.util.List;

public class RutinaDAO extends GenericDAO<Rutina> {

    public RutinaDAO() {
        super(Rutina.class, "la rutina");
    }

    /** UPDATE de un solo campo: cambia el nombre de la rutina. */
    public void actualizarNombre(Long rutinaId, String nombre) {
        ejecutar("renombrar la rutina", session -> {
            Rutina rutina = session.get(Rutina.class, rutinaId);
            if (rutina == null) {
                throw new IllegalArgumentException("La rutina que se quiere renombrar no existe.");
            }
            rutina.setNombre(nombre);
        });
    }

    /**
     * HQL con JOIN FETCH: la rutina más reciente del usuario con sus ejercicios
     * y el grupo muscular de cada uno.
     */
    public Rutina buscarUltimaPorUsuario(Long usuarioId) {
        return consultar("cargar la rutina", session -> {
            List<Rutina> rutinas = session.createQuery("""
                            SELECT DISTINCT r FROM Rutina r
                            LEFT JOIN FETCH r.ejercicios re
                            LEFT JOIN FETCH re.ejercicio e
                            LEFT JOIN FETCH e.grupoMuscular
                            WHERE r.usuario.id = :usuarioId
                            ORDER BY r.fechaCreacion DESC, r.id DESC
                            """, Rutina.class)
                    .setParameter("usuarioId", usuarioId)
                    .list();
            return rutinas.isEmpty() ? null : rutinas.get(0);
        });
    }

    /**
     * HQL de agregación: cuántos ejercicios y series semanales tiene cada grupo muscular
     * en una rutina (RutinaEjercicio → Ejercicio → GrupoMuscular).
     */
    public List<Object[]> resumenPorGrupoMuscular(Long rutinaId) {
        return consultar("calcular el resumen de la rutina", session -> session.createQuery("""
                        SELECT g.nombre, COUNT(re), SUM(re.series)
                        FROM RutinaEjercicio re
                        JOIN re.ejercicio e
                        JOIN e.grupoMuscular g
                        WHERE re.rutina.id = :rutinaId
                        GROUP BY g.nombre
                        ORDER BY SUM(re.series) DESC
                        """, Object[].class)
                .setParameter("rutinaId", rutinaId)
                .list());
    }
}

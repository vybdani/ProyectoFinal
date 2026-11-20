package ni.edu.uam.proyectofinal.dao;

import ni.edu.uam.proyectofinal.model.PlanComidas;
import ni.edu.uam.proyectofinal.model.Receta;

import java.util.List;

public class PlanComidasDAO extends GenericDAO<PlanComidas> {

    public PlanComidasDAO() {
        super(PlanComidas.class, "el plan de comidas");
    }

    /** HQL con JOIN FETCH: el plan más reciente del usuario con sus comidas, recetas e ingredientes. */
    public PlanComidas buscarUltimoPorUsuario(Long usuarioId) {
        return consultar("cargar el plan semanal", session -> {
            List<PlanComidas> planes = session.createQuery("""
                            SELECT DISTINCT p FROM PlanComidas p
                            LEFT JOIN FETCH p.comidas c
                            LEFT JOIN FETCH c.receta
                            WHERE p.usuario.id = :usuarioId
                            ORDER BY p.fechaInicio DESC, p.id DESC
                            """, PlanComidas.class)
                    .setParameter("usuarioId", usuarioId)
                    .list();
            if (planes.isEmpty()) {
                return null;
            }
            PlanComidas plan = planes.get(0);

            // Segunda consulta en la misma sesión: carga los ingredientes de las recetas del plan.
            // Se hace aparte porque Hibernate no permite JOIN FETCH de dos listas a la vez.
            session.createQuery("""
                            SELECT DISTINCT r FROM Receta r
                            LEFT JOIN FETCH r.ingredientes ri
                            LEFT JOIN FETCH ri.alimento
                            WHERE r IN (SELECT c.receta FROM ComidaPlan c WHERE c.planComidas.id = :planId)
                            """, Receta.class)
                    .setParameter("planId", plan.getId())
                    .list();
            return plan;
        });
    }
}

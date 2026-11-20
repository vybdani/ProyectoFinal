package ni.edu.uam.proyectofinal.dao;

import ni.edu.uam.proyectofinal.model.RequerimientoDiario;

import java.time.LocalDate;

public class RequerimientoDiarioDAO extends GenericDAO<RequerimientoDiario> {

    public RequerimientoDiarioDAO() {
        super(RequerimientoDiario.class, "el requerimiento diario");
    }

    /** Último requerimiento calculado por el usuario. */
    public RequerimientoDiario buscarPorUsuario(Long usuarioId) {
        return consultar("cargar el requerimiento", session -> session.createQuery("""
                        FROM RequerimientoDiario r
                        WHERE r.usuario.id = :usuarioId
                        ORDER BY r.fecha DESC, r.id DESC
                        """, RequerimientoDiario.class)
                .setParameter("usuarioId", usuarioId)
                .setMaxResults(1)
                .uniqueResult());
    }

    /** Requerimiento calculado hoy, si existe. */
    public RequerimientoDiario buscarDelDia(Long usuarioId) {
        return consultar("cargar el requerimiento", session -> session.createQuery("""
                        FROM RequerimientoDiario r
                        WHERE r.usuario.id = :usuarioId
                        AND r.fecha = :fecha
                        ORDER BY r.id DESC
                        """, RequerimientoDiario.class)
                .setParameter("usuarioId", usuarioId)
                .setParameter("fecha", LocalDate.now())
                .setMaxResults(1)
                .uniqueResult());
    }

    /** Un plan de comidas puede apuntar al requerimiento: se desvincula antes de borrarlo. */
    @Override
    public void eliminar(Long id) {
        ejecutar("eliminar el requerimiento diario", session -> {
            RequerimientoDiario requerimiento = session.get(RequerimientoDiario.class, id);
            if (requerimiento == null) {
                throw new IllegalArgumentException("El requerimiento que se quiere eliminar no existe.");
            }
            session.createMutationQuery(
                            "UPDATE PlanComidas p SET p.requerimientoDiario = null WHERE p.requerimientoDiario.id = :id")
                    .setParameter("id", id)
                    .executeUpdate();
            session.remove(requerimiento);
        });
    }
}

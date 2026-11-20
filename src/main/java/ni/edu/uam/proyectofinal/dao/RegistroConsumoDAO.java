package ni.edu.uam.proyectofinal.dao;

import ni.edu.uam.proyectofinal.model.RegistroConsumo;

import java.time.LocalDate;
import java.util.List;

public class RegistroConsumoDAO extends GenericDAO<RegistroConsumo> {

    public RegistroConsumoDAO() {
        super(RegistroConsumo.class, "el registro de consumo");
    }

    /** Lo que comió el usuario en una fecha (el alimento viene cargado por ser EAGER). */
    public List<RegistroConsumo> listarConsumoDiario(Long usuarioId, LocalDate fecha) {
        return consultar("cargar el consumo del día", session -> session.createQuery("""
                        FROM RegistroConsumo r
                        WHERE r.usuario.id = :usuarioId
                        AND r.fecha = :fecha
                        ORDER BY r.hora DESC
                        """, RegistroConsumo.class)
                .setParameter("usuarioId", usuarioId)
                .setParameter("fecha", fecha)
                .list());
    }

    /** HQL de agregación: kcal, proteínas, carbohidratos y grasas consumidos en el día. */
    public Object[] obtenerTotales(Long usuarioId, LocalDate fecha) {
        return consultar("calcular los totales del día", session -> session.createQuery("""
                        SELECT
                            COALESCE(SUM(r.caloriasConsumidas), 0.0),
                            COALESCE(SUM(r.proteinasConsumidas), 0.0),
                            COALESCE(SUM(r.carbohidratosConsumidos), 0.0),
                            COALESCE(SUM(r.grasasConsumidas), 0.0)
                        FROM RegistroConsumo r
                        WHERE r.usuario.id = :usuarioId
                        AND r.fecha = :fecha
                        """, Object[].class)
                .setParameter("usuarioId", usuarioId)
                .setParameter("fecha", fecha)
                .uniqueResult());
    }
}

package ni.edu.uam.proyectofinal.dao;

import ni.edu.uam.proyectofinal.model.ItemListaSuper;

import java.util.List;

public class ItemListaSuperDAO extends GenericDAO<ItemListaSuper> {

    public ItemListaSuperDAO() {
        super(ItemListaSuper.class, "el producto");
    }

    /**
     * Guarda varios productos en una sola transacción: o se guardan todos o ninguno.
     * Si ya hay un producto pendiente con el mismo nombre y unidad, se le suma la cantidad.
     */
    public int guardarTodos(Long usuarioId, List<ItemListaSuper> nuevos) {
        return enTransaccion("enviar los productos a la lista del súper", session -> {
            List<ItemListaSuper> pendientes = session.createQuery("""
                            FROM ItemListaSuper i
                            WHERE i.usuario.id = :usuarioId AND i.comprado = false
                            """, ItemListaSuper.class)
                    .setParameter("usuarioId", usuarioId)
                    .list();
            int agregados = 0;
            for (ItemListaSuper nuevo : nuevos) {
                ItemListaSuper existente = pendientes.stream()
                        .filter(p -> p.getNombreProducto().equalsIgnoreCase(nuevo.getNombreProducto())
                                && p.getUnidad().equalsIgnoreCase(nuevo.getUnidad()))
                        .findFirst()
                        .orElse(null);
                if (existente != null) {
                    existente.setCantidad(existente.getCantidad() + nuevo.getCantidad());
                } else {
                    session.persist(nuevo);
                    agregados++;
                }
            }
            return agregados;
        });
    }

    /** Lista del súper del usuario: primero lo pendiente y luego por nombre. */
    public List<ItemListaSuper> listarPorUsuario(Long usuarioId) {
        return consultar("cargar la lista del súper", session -> session.createQuery("""
                        FROM ItemListaSuper i
                        WHERE i.usuario.id = :usuarioId
                        ORDER BY i.comprado ASC, i.nombreProducto ASC
                        """, ItemListaSuper.class)
                .setParameter("usuarioId", usuarioId)
                .list());
    }
}

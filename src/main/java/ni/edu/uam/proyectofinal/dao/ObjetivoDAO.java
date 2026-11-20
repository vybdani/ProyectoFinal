package ni.edu.uam.proyectofinal.dao;

import ni.edu.uam.proyectofinal.model.Objetivo;

import java.util.List;

public class ObjetivoDAO extends GenericDAO<Objetivo> {

    public ObjetivoDAO() {
        super(Objetivo.class, "el objetivo");
    }

    /** Inserta el objetivo si es nuevo o lo actualiza si ya existe. */
    @Override
    public Objetivo guardar(Objetivo objetivo) {
        return objetivo.getId() == null ? super.guardar(objetivo) : actualizar(objetivo);
    }

    /** Objetivo vigente del usuario para un modo (NUTRICION o GIMNASIO). */
    public Objetivo buscarPorUsuarioYModo(Long usuarioId, String modo) {
        return consultar("cargar el objetivo", session -> session.createQuery("""
                        FROM Objetivo o
                        WHERE o.usuario.id = :usuarioId
                        AND o.modo = :modo
                        ORDER BY o.id DESC
                        """, Objetivo.class)
                .setParameter("usuarioId", usuarioId)
                .setParameter("modo", modo)
                .setMaxResults(1)
                .uniqueResult());
    }

    /** Último objetivo que guardó el usuario, en cualquier modo. */
    public Objetivo buscarUltimoPorUsuario(Long usuarioId) {
        return consultar("cargar el objetivo", session -> session.createQuery(
                        "FROM Objetivo o WHERE o.usuario.id = :usuarioId ORDER BY o.id DESC", Objetivo.class)
                .setParameter("usuarioId", usuarioId)
                .setMaxResults(1)
                .uniqueResult());
    }

    /** HQL con JOIN: objetivos de un usuario junto con su nombre, para mostrar el historial. */
    public List<Object[]> listarObjetivosConUsuario(Long usuarioId) {
        return consultar("cargar el historial de objetivos", session -> session.createQuery("""
                        SELECT u.nombre, o.modo, o.tipo, o.caloriasDiarias
                        FROM Objetivo o
                        JOIN o.usuario u
                        WHERE u.id = :usuarioId
                        ORDER BY o.id DESC
                        """, Object[].class)
                .setParameter("usuarioId", usuarioId)
                .list());
    }
}

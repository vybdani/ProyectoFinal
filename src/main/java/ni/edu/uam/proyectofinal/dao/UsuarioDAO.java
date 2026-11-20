package ni.edu.uam.proyectofinal.dao;

import ni.edu.uam.proyectofinal.model.Usuario;
import ni.edu.uam.proyectofinal.util.Contrasenas;

public class UsuarioDAO extends GenericDAO<Usuario> {

    public UsuarioDAO() {
        super(Usuario.class, "el usuario");
    }

    /** Guarda el usuario nuevo con la contraseña convertida en hash (nunca en texto plano). */
    @Override
    public Usuario guardar(Usuario usuario) {
        if (!Contrasenas.estaHasheada(usuario.getPassword())) {
            usuario.setPassword(Contrasenas.hashear(usuario.getPassword()));
        }
        return super.guardar(usuario);
    }

    /**
     * HQL con JOIN FETCH: busca al usuario por correo junto con su perfil físico
     * y comprueba la contraseña contra el hash guardado. Devuelve null si no coinciden.
     */
    public Usuario autenticar(String correo, String password) {
        Usuario usuario = consultar("iniciar sesión", session -> session.createQuery("""
                        SELECT u FROM Usuario u
                        LEFT JOIN FETCH u.perfilFisico
                        WHERE lower(u.correo) = lower(:correo)
                        """, Usuario.class)
                .setParameter("correo", correo)
                .uniqueResult());
        return usuario != null && Contrasenas.verificar(password, usuario.getPassword()) ? usuario : null;
    }

    /** HQL de agregación: ¿ya hay una cuenta con ese correo? (sin distinguir mayúsculas). */
    public boolean existeCorreo(String correo) {
        Long cantidad = consultar("verificar el correo", session -> session.createQuery(
                        "SELECT COUNT(u) FROM Usuario u WHERE lower(u.correo) = lower(:correo)", Long.class)
                .setParameter("correo", correo)
                .uniqueResult());
        return cantidad != null && cantidad > 0;
    }

    /** HQL con JOIN FETCH: recarga el usuario con su perfil físico. */
    public Usuario obtenerUsuarioConPerfil(Long id) {
        return consultar("cargar el usuario", session -> session.createQuery(
                        "SELECT u FROM Usuario u LEFT JOIN FETCH u.perfilFisico WHERE u.id = :id", Usuario.class)
                .setParameter("id", id)
                .uniqueResult());
    }

    /**
     * Borra la cuenta y todos sus datos en una sola transacción.
     * Primero se borran las tablas que apuntan al usuario (respetando las llaves foráneas)
     * y al final el usuario; su perfil físico se borra por cascade.
     */
    @Override
    public void eliminar(Long id) {
        ejecutar("eliminar la cuenta", session -> {
            Usuario usuario = session.get(Usuario.class, id);
            if (usuario == null) {
                throw new IllegalArgumentException("La cuenta que se quiere eliminar no existe.");
            }
            String[] borrados = {
                    "DELETE FROM ComidaPlan c WHERE c.planComidas.usuario.id = :id",
                    "DELETE FROM PlanComidas p WHERE p.usuario.id = :id",
                    "DELETE FROM RequerimientoDiario r WHERE r.usuario.id = :id",
                    "DELETE FROM RutinaEjercicio re WHERE re.rutina.usuario.id = :id",
                    "DELETE FROM Rutina r WHERE r.usuario.id = :id",
                    "DELETE FROM RegistroConsumo r WHERE r.usuario.id = :id",
                    "DELETE FROM ItemListaSuper i WHERE i.usuario.id = :id",
                    "DELETE FROM Objetivo o WHERE o.usuario.id = :id"
            };
            for (String hql : borrados) {
                session.createMutationQuery(hql).setParameter("id", id).executeUpdate();
            }
            session.remove(usuario);
        });
    }
}

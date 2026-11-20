package ni.edu.uam.proyectofinal.dao;

import ni.edu.uam.proyectofinal.model.Usuario;
import ni.edu.uam.proyectofinal.util.Contrasenas;

public class UsuarioDAO extends GenericDAO<Usuario> {

    public UsuarioDAO() {
        super(Usuario.class, "el usuario");
    }

    /** Guarda el usuario nuevo con la contraseña y la respuesta de seguridad convertidas en hash. */
    @Override
    public Usuario guardar(Usuario usuario) {
        if (!Contrasenas.estaHasheada(usuario.getPassword())) {
            usuario.setPassword(Contrasenas.hashear(usuario.getPassword()));
        }
        String respuesta = usuario.getRespuestaSeguridad();
        if (respuesta != null && !respuesta.isBlank() && !Contrasenas.estaHasheada(respuesta)) {
            usuario.setRespuestaSeguridad(Contrasenas.hashear(normalizarRespuesta(respuesta)));
        }
        return super.guardar(usuario);
    }

    // --------------------------------------------- Recuperar la cuenta

    /** Pregunta de seguridad de la cuenta con ese correo; null si no existe o no tiene pregunta. */
    public String buscarPreguntaSeguridad(String correo) {
        return consultar("buscar la cuenta", session -> session.createQuery(
                        "SELECT u.preguntaSeguridad FROM Usuario u WHERE lower(u.correo) = lower(:correo)", String.class)
                .setParameter("correo", correo.trim())
                .uniqueResult());
    }

    /**
     * Cambia la contraseña si la respuesta de seguridad es correcta.
     * Devuelve false si la respuesta no coincide (no cambia nada).
     */
    public boolean restablecerContrasena(String correo, String respuesta, String nuevaContrasena) {
        return enTransaccion("cambiar la contraseña", session -> {
            Usuario usuario = session.createQuery(
                            "FROM Usuario u WHERE lower(u.correo) = lower(:correo)", Usuario.class)
                    .setParameter("correo", correo.trim())
                    .uniqueResult();
            if (usuario == null || usuario.getRespuestaSeguridad() == null) {
                throw new IllegalStateException("Esa cuenta no tiene una pregunta de seguridad configurada.");
            }
            if (!Contrasenas.verificar(normalizarRespuesta(respuesta), usuario.getRespuestaSeguridad())) {
                return false;
            }
            usuario.setPassword(Contrasenas.hashear(nuevaContrasena)); // se guarda al hacer commit
            return true;
        });
    }

    /** Guarda o cambia la pregunta de seguridad de una cuenta (la respuesta se guarda como hash). */
    public void configurarPreguntaSeguridad(Long usuarioId, String pregunta, String respuesta) {
        ejecutar("guardar la pregunta de seguridad", session -> {
            Usuario usuario = session.get(Usuario.class, usuarioId);
            if (usuario == null) {
                throw new IllegalArgumentException("La cuenta no existe.");
            }
            usuario.setPreguntaSeguridad(pregunta.trim());
            usuario.setRespuestaSeguridad(Contrasenas.hashear(normalizarRespuesta(respuesta)));
        });
    }

    /** Marca que el usuario ya vio el tutorial de bienvenida, para no volver a mostrarlo. */
    public void marcarTutorialVisto(Long usuarioId) {
        ejecutar("guardar el tutorial", session -> {
            Usuario usuario = session.get(Usuario.class, usuarioId);
            if (usuario != null) {
                usuario.setTutorialVisto(true);
            }
        });
    }

    /**
     * La respuesta se compara sin importar mayúsculas, tildes ni espacios extra:
     * "  Firulais " y "firulais" cuentan como la misma respuesta.
     */
    static String normalizarRespuesta(String respuesta) {
        return java.text.Normalizer.normalize(respuesta == null ? "" : respuesta.trim(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("\\s+", " ")
                .toLowerCase(java.util.Locale.ROOT);
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

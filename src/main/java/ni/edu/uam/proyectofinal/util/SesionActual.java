package ni.edu.uam.proyectofinal.util;

import ni.edu.uam.proyectofinal.model.Usuario;

public class SesionActual {

    private static Usuario usuarioActivo;

    private SesionActual() {
    }

    public static Usuario getUsuarioActivo() {
        return usuarioActivo;
    }

    public static void setUsuarioActivo(Usuario usuario) {
        usuarioActivo = usuario;
    }

    public static void cerrarSesion() {
        usuarioActivo = null;
    }
}
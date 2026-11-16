package ni.edu.uam.proyectofinal.util;

import ni.edu.uam.proyectofinal.model.Usuario;

/**
 * Guarda el usuario que inició sesión y el modo que eligió en el menú
 * (NUTRICION o GIMNASIO) para que todas las pantallas lo puedan usar.
 */
public class SesionActual {

    public static final String MODO_NUTRICION = "NUTRICION";
    public static final String MODO_GIMNASIO = "GIMNASIO";

    private static Usuario usuarioActivo;
    private static String modoActual;

    private SesionActual() {
    }

    public static Usuario getUsuarioActivo() {
        return usuarioActivo;
    }

    public static void setUsuarioActivo(Usuario usuario) {
        usuarioActivo = usuario;
    }

    public static String getModoActual() {
        return modoActual;
    }

    public static void setModoActual(String modo) {
        modoActual = modo;
    }

    public static void cerrarSesion() {
        usuarioActivo = null;
        modoActual = null;
    }
}

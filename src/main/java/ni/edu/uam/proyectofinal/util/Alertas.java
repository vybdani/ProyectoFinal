package ni.edu.uam.proyectofinal.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;
import ni.edu.uam.proyectofinal.dao.PersistenciaException;
import ni.edu.uam.proyectofinal.reporte.ReporteException;

/**
 * Ventanas de mensaje de toda la aplicación, siempre con el mismo formato.
 * Se abren encima de la ventana activa y bloquean la pantalla hasta que se cierran.
 */
public final class Alertas {

    private Alertas() {
    }

    /** Operación terminada con éxito. */
    public static void info(String titulo, String mensaje) {
        mostrar(Alert.AlertType.INFORMATION, titulo, mensaje);
    }

    /** Dato inválido o acción que no se puede hacer: el usuario puede corregirlo. */
    public static void aviso(String titulo, String mensaje) {
        mostrar(Alert.AlertType.WARNING, titulo, mensaje);
    }

    /** Error al trabajar con la base de datos u otro fallo. */
    public static void error(String titulo, String mensaje) {
        mostrar(Alert.AlertType.ERROR, titulo, mensaje);
    }

    /**
     * Error a partir de una excepción. Las reglas del negocio y los errores de persistencia
     * ya traen un mensaje claro; cualquier otro error se registra en consola y se muestra genérico.
     */
    public static void error(String titulo, Throwable e) {
        error(titulo, mensajeDe(e));
    }

    /** Pregunta Sí / No. Devuelve true solo si el usuario elige "Sí". */
    public static boolean confirmar(String titulo, String pregunta) {
        ButtonType si = new ButtonType("Sí", ButtonBar.ButtonData.YES);
        ButtonType no = new ButtonType("No", ButtonBar.ButtonData.NO);
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, pregunta, si, no);
        preparar(alerta, titulo);
        return alerta.showAndWait().orElse(no) == si;
    }

    public static void mostrar(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setContentText(mensaje);
        preparar(alerta, titulo);
        alerta.showAndWait();
    }

    /** Mensaje que se le puede mostrar al usuario para una excepción. */
    public static String mensajeDe(Throwable e) {
        if (e instanceof PersistenciaException || e instanceof ReporteException
                || e instanceof IllegalArgumentException || e instanceof IllegalStateException) {
            return e.getMessage();
        }
        e.printStackTrace();
        return "Ocurrió un error inesperado (" + e.getClass().getSimpleName() + "). Inténtalo de nuevo.";
    }

    private static void preparar(Alert alerta, String titulo) {
        alerta.setTitle("Solea - " + titulo);
        alerta.setHeaderText(titulo);
        alerta.getDialogPane().setMinWidth(420);
        Window.getWindows().stream()
                .filter(Window::isFocused)
                .findFirst()
                .ifPresent(alerta::initOwner);
    }
}

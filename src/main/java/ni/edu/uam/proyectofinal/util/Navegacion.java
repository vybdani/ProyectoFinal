package ni.edu.uam.proyectofinal.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import ni.edu.uam.proyectofinal.HelloApplication;

import java.io.IOException;

/**
 * Centraliza el cambio de pantallas para no repetir el mismo código en cada controlador.
 */
public final class Navegacion {

    private Navegacion() {
    }

    public static void ir(Node origen, String vista, String titulo, double ancho, double alto) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("views/" + vista));
            Stage stage = (Stage) origen.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), ancho, alto));
            stage.setTitle("NutriFit - " + titulo);
            stage.setResizable(true);
            stage.centerOnScreen();
        } catch (IOException | RuntimeException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de navegación");
            alert.setHeaderText(null);
            alert.setContentText("No se pudo abrir la pantalla " + vista + ": " + e.getMessage());
            alert.showAndWait();
        }
    }

    public static void irAlLogin(Node origen) {
        ir(origen, "login-view.fxml", "Inicio de Sesión", 400, 480);
    }

    public static void irAlMenuPrincipal(Node origen) {
        ir(origen, "menu-principal-view.fxml", "Menú Principal", 650, 460);
    }

    public static void irAlPerfil(Node origen) {
        ir(origen, "perfil-fisico-view.fxml", "Perfil Físico", 520, 520);
    }

    /** Vuelve al menú del modo elegido (nutrición o gimnasio); si no hay modo, al menú principal. */
    public static void volver(Node origen) {
        String modo = SesionActual.getModoActual();
        if (modo == null) {
            irAlMenuPrincipal(origen);
        } else {
            String titulo = SesionActual.MODO_GIMNASIO.equals(modo) ? "Modo Gimnasio" : "Modo Nutrición";
            ir(origen, "modo-view.fxml", titulo, 620, 560);
        }
    }
}

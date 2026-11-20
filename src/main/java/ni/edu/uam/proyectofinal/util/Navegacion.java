package ni.edu.uam.proyectofinal.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
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
            Scene escena = new Scene(loader.load(), ancho, alto);
            aplicarEstilos(escena);
            stage.setScene(escena);
            stage.setTitle("Solea - " + titulo);
            stage.setResizable(true);
            stage.centerOnScreen();
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            Alertas.error("No se pudo abrir la pantalla",
                    "Ocurrió un problema al abrir " + titulo + ". Si el problema sigue, revisa la conexión con PostgreSQL.");
        }
    }

    /** Agrega la hoja de estilos común (app.css) a una escena. */
    public static void aplicarEstilos(Scene escena) {
        var hoja = HelloApplication.class.getResource("views/app.css");
        if (hoja != null) {
            escena.getStylesheets().add(hoja.toExternalForm());
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

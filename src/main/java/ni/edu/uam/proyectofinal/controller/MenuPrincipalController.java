package ni.edu.uam.proyectofinal.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import ni.edu.uam.proyectofinal.model.ConsejoNutricional;
import ni.edu.uam.proyectofinal.model.Usuario;
import ni.edu.uam.proyectofinal.service.NutricionApiClient;
import ni.edu.uam.proyectofinal.util.Navegacion;
import ni.edu.uam.proyectofinal.util.SesionActual;

public class MenuPrincipalController {

    @FXML
    private Label lblBienvenida;

    private final NutricionApiClient apiClient = new NutricionApiClient();

    @FXML
    public void initialize() {
        Usuario usuario = SesionActual.getUsuarioActivo();
        lblBienvenida.setText(usuario != null
                ? "Bienvenido/a, " + usuario.getNombre()
                : "Bienvenido/a a NutriFit");
    }

    @FXML
    private void handleModoNutricion() {
        SesionActual.setModoActual(SesionActual.MODO_NUTRICION);
        Navegacion.volver(lblBienvenida);
    }

    @FXML
    private void handleModoGimnasio() {
        SesionActual.setModoActual(SesionActual.MODO_GIMNASIO);
        Navegacion.volver(lblBienvenida);
    }

    @FXML
    private void handleEditarPerfil() {
        Navegacion.irAlPerfil(lblBienvenida);
    }

    @FXML
    private void handleConsultarApiConsejo() {
        String modo = SesionActual.getModoActual();
        String objetivo = SesionActual.MODO_GIMNASIO.equals(modo) ? "volumen" : "mantener";

        apiClient.obtenerConsejoAsync(objetivo).thenAccept(consejo -> {
            Platform.runLater(() -> mostrarDialogoConsejo(consejo));
        });
    }

    private void mostrarDialogoConsejo(ConsejoNutricional c) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("NutriFit API - Recomendación Inteligente");
        alert.setHeaderText("💡 " + c.getTitulo());

        String mensaje = c.getConsejo() + "\n\n"
                + "📊 Distribución de macros: " + c.getDistribucionMacrosSugerida() + "\n"
                + "💧 Hidratación sugerida: " + c.getRecomendacionAgua() + "\n\n"
                + "🔗 Origen de datos: " + c.getFuente();

        alert.setContentText(mensaje);
        alert.getDialogPane().setPrefWidth(480);
        alert.showAndWait();
    }

    @FXML
    private void handleCerrarSesion() {
        SesionActual.cerrarSesion();
        Navegacion.irAlLogin(lblBienvenida);
    }
}

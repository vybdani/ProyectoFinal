package ni.edu.uam.proyectofinal.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import ni.edu.uam.proyectofinal.dao.ObjetivoDAO;
import ni.edu.uam.proyectofinal.model.ConsejoNutricional;
import ni.edu.uam.proyectofinal.model.Objetivo;
import ni.edu.uam.proyectofinal.model.Usuario;
import ni.edu.uam.proyectofinal.service.NutricionApiClient;
import ni.edu.uam.proyectofinal.util.Alertas;
import ni.edu.uam.proyectofinal.util.Navegacion;
import ni.edu.uam.proyectofinal.util.SesionActual;

public class MenuPrincipalController {

    @FXML private Label lblBienvenida;

    private final NutricionApiClient apiClient = new NutricionApiClient();

    @FXML
    public void initialize() {
        Usuario usuario = SesionActual.getUsuarioActivo();
        lblBienvenida.setText(usuario != null
                ? "Bienvenido/a, " + usuario.getNombre()
                : "Bienvenido/a a Solea");
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
        // El consejo se pide para el último objetivo que guardó el usuario (o "mantener" si no tiene)
        String objetivo = "Mantener peso";
        Usuario usuario = SesionActual.getUsuarioActivo();
        if (usuario != null) {
            try {
                Objetivo ultimo = new ObjetivoDAO().buscarUltimoPorUsuario(usuario.getId());
                if (ultimo != null) {
                    objetivo = ultimo.getTipo();
                }
            } catch (Exception e) {
                // Sin base de datos se pide el consejo general
            }
        }
        lblBienvenida.getScene().setCursor(javafx.scene.Cursor.WAIT);
        apiClient.obtenerConsejoAsync(objetivo)
                .whenComplete((consejo, error) -> Platform.runLater(() -> {
                    lblBienvenida.getScene().setCursor(javafx.scene.Cursor.DEFAULT);
                    if (error != null || consejo == null) {
                        Alertas.error("Consejo no disponible",
                                "No se pudo obtener el consejo nutricional. Inténtalo de nuevo más tarde.");
                    } else {
                        mostrarDialogoConsejo(consejo);
                    }
                }));
    }

    private void mostrarDialogoConsejo(ConsejoNutricional c) {
        String mensaje = c.getConsejo() + "\n\n"
                + "Distribución de macros: " + c.getDistribucionMacrosSugerida() + "\n"
                + "Hidratación sugerida: " + c.getRecomendacionAgua() + "\n\n"
                + "Fuente: " + c.getFuente();
        Alertas.mostrar(Alert.AlertType.INFORMATION, c.getTitulo(), mensaje);
    }

    @FXML
    private void handleCerrarSesion() {
        if (Alertas.confirmar("Cerrar sesión", "¿Quieres cerrar tu sesión?")) {
            SesionActual.cerrarSesion();
            Navegacion.irAlLogin(lblBienvenida);
        }
    }
}

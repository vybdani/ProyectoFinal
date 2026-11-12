package ni.edu.uam.proyectofinal.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import ni.edu.uam.proyectofinal.HelloApplication;

import java.io.IOException;

public class MenuPrincipalController {

    @FXML
    private void handleModoNutricion(ActionEvent event) {
        navegar(event, "views/receta-view.fxml", "NutriFit - Gestión de Recetas", 920, 640);
    }

    @FXML
    private void handleModoGimnasio(ActionEvent event) {
        navegar(event, "views/plan-semanal-view.fxml", "NutriFit - Plan Semanal de Comidas", 880, 600);
    }

    private void navegar(ActionEvent event, String fxmlPath, String titulo, int ancho, int alto) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loader.load(), ancho, alto));
            stage.setTitle(titulo);
            stage.setResizable(true);
            stage.centerOnScreen();
        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Navegación", "No se pudo cargar la vista: " + e.getMessage());
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
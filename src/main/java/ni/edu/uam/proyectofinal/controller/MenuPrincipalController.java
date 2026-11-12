package ni.edu.uam.proyectofinal.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;

public class MenuPrincipalController {

    @FXML
    private void handleModoNutricion() {
        mostrarAlerta(Alert.AlertType.INFORMATION, "Modo Nutrición", "Entrando a la sección de Planes de Comidas y Recetas...");
        // Aquí conectará el módulo de Nutrición de tus compañeros
    }

    @FXML
    private void handleModoGimnasio() {
        mostrarAlerta(Alert.AlertType.INFORMATION, "Modo Gimnasio", "Entrando a la sección de Rutinas y Ejercicios...");
        // Aquí conectará el módulo de Gimnasio de tus compañeros
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
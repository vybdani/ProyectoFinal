package ni.edu.uam.proyectofinal.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import ni.edu.uam.proyectofinal.dao.UsuarioDAO;
import ni.edu.uam.proyectofinal.model.PerfilFisico;
import ni.edu.uam.proyectofinal.model.Usuario;

public class PerfilFisicoController {

    @FXML
    private TextField txtPeso;

    @FXML
    private TextField txtAltura;

    @FXML
    private TextField txtEdad;

    @FXML
    private TextField txtPorcentajeGrasa;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private Usuario usuarioActual; // Usuario cargado en sesión

    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
    }

    @FXML
    private void handleGuardarPerfil() {
        try {
            Double peso = Double.parseDouble(txtPeso.getText().trim());
            Double altura = Double.parseDouble(txtAltura.getText().trim());
            Integer edad = Integer.parseInt(txtEdad.getText().trim());
            Double grasa = txtPorcentajeGrasa.getText().isEmpty() ? 0.0 : Double.parseDouble(txtPorcentajeGrasa.getText().trim());

            PerfilFisico perfil = new PerfilFisico(peso, altura, edad, grasa);

            if (usuarioActual != null) {
                usuarioActual.setPerfilFisico(perfil);
                usuarioDAO.guardar(usuarioActual);
                mostrarAlerta(Alert.AlertType.INFORMATION, "Perfil Guardado", "Tus datos físicos han sido actualizados.");
            } else {
                mostrarAlerta(Alert.AlertType.WARNING, "Sesión Requerida", "No hay un usuario activo en sesión.");
            }

        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Formato", "Por favor ingresa valores numéricos válidos en peso, altura y edad.");
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
package ni.edu.uam.proyectofinal.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ni.edu.uam.proyectofinal.dao.UsuarioDAO;
import ni.edu.uam.proyectofinal.model.Usuario;

public class LoginController {

    @FXML
    private TextField txtCorreo;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private TextField txtNombreRegistro;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @FXML
    private void handleLogin() {
        String correo = txtCorreo.getText().trim();
        String password = txtPassword.getText().trim();

        if (correo.isEmpty() || password.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos Vacíos", "Por favor ingresa tu correo y contraseña.");
            return;
        }

        // Lógica sencilla de verificación
        mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Inicio de sesión correcto para: " + correo);
        // Aquí se llamaría a la navegación hacia el Menú Principal
    }

    @FXML
    private void handleRegistro() {
        String nombre = txtNombreRegistro != null ? txtNombreRegistro.getText().trim() : "";
        String correo = txtCorreo.getText().trim();
        String password = txtPassword.getText().trim();

        if (correo.isEmpty() || password.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos Vacíos", "Completa todos los campos para registrarte.");
            return;
        }

        Usuario nuevoUsuario = new Usuario(nombre.isEmpty() ? "Usuario" : nombre, correo, password);
        usuarioDAO.guardar(nuevoUsuario);

        mostrarAlerta(Alert.AlertType.INFORMATION, "Registro Exitoso", "Usuario registrado correctamente en la base de datos.");
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
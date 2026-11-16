package ni.edu.uam.proyectofinal.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ni.edu.uam.proyectofinal.HelloApplication;
import ni.edu.uam.proyectofinal.dao.UsuarioDAO;
import ni.edu.uam.proyectofinal.model.Usuario;
import ni.edu.uam.proyectofinal.util.SesionActual;

import java.io.IOException;

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

        try {
            Usuario usuario = usuarioDAO.autenticar(correo, password);
            if (usuario == null) {
                mostrarAlerta(Alert.AlertType.ERROR, "Acceso Denegado", "Correo o contraseña incorrectos.");
                return;
            }

            SesionActual.setUsuarioActivo(usuario);
            cambiarVista("views/menu-principal-view.fxml", "NutriFit - Menú Principal", 650, 420);

        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Conexión", "No se pudo verificar el usuario: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegistro() {
        String nombre = txtNombreRegistro != null ? txtNombreRegistro.getText().trim() : "";
        String correo = txtCorreo.getText().trim();
        String password = txtPassword.getText().trim();

        if (correo.isEmpty() || password.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos Vacíos", "Completa correo y contraseña para registrarte.");
            return;
        }

        try {
            Usuario nuevoUsuario = new Usuario(nombre.isEmpty() ? "Usuario" : nombre, correo, password);
            usuarioDAO.guardar(nuevoUsuario);
            SesionActual.setUsuarioActivo(nuevoUsuario);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Registro Exitoso", "Cuenta creada. Completa tu perfil físico.");
            cambiarVista("views/perfil-fisico-view.fxml", "NutriFit - Perfil Físico", 520, 480);

        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Registro", "No se pudo registrar (el correo ya podría existir).");
        }
    }

    private void cambiarVista(String fxmlPath, String titulo, int ancho, int alto) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(fxmlPath));
            Stage stage = (Stage) txtCorreo.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), ancho, alto));
            stage.setTitle(titulo);
            stage.setResizable(true);
            stage.centerOnScreen();
        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Navegación", "No se pudo abrir la pantalla: " + e.getMessage());
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
package ni.edu.uam.proyectofinal.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ni.edu.uam.proyectofinal.dao.UsuarioDAO;
import ni.edu.uam.proyectofinal.model.Usuario;
import ni.edu.uam.proyectofinal.util.Navegacion;
import ni.edu.uam.proyectofinal.util.SesionActual;

public class LoginController {

    private static final String PATRON_CORREO = "^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$";

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
            if (usuario.getPerfilFisico() == null) {
                // Usuario que se registró pero no terminó su perfil
                mostrarAlerta(Alert.AlertType.INFORMATION, "Perfil incompleto", "Completa tu perfil físico para continuar.");
                Navegacion.irAlPerfil(txtCorreo);
            } else {
                Navegacion.irAlMenuPrincipal(txtCorreo);
            }

        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Conexión",
                    "No se pudo conectar con la base de datos. Revisa que PostgreSQL esté encendido y la configuración del README.\n\n"
                            + e.getMessage());
        }
    }

    @FXML
    private void handleRegistro() {
        String nombre = txtNombreRegistro != null ? txtNombreRegistro.getText().trim() : "";
        String correo = txtCorreo.getText().trim();
        String password = txtPassword.getText().trim();

        if (nombre.isEmpty() || correo.isEmpty() || password.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos Vacíos", "Completa nombre, correo y contraseña para registrarte.");
            return;
        }
        if (!correo.matches(PATRON_CORREO)) {
            mostrarAlerta(Alert.AlertType.WARNING, "Correo inválido", "Ingresa un correo con formato válido, por ejemplo ana@correo.com.");
            return;
        }
        if (password.length() < 6) {
            mostrarAlerta(Alert.AlertType.WARNING, "Contraseña débil", "La contraseña debe tener al menos 6 caracteres.");
            return;
        }

        try {
            if (usuarioDAO.existeCorreo(correo)) {
                mostrarAlerta(Alert.AlertType.WARNING, "Correo registrado", "Ya existe una cuenta con ese correo. Inicia sesión.");
                return;
            }

            Usuario nuevoUsuario = new Usuario(nombre, correo, password);
            usuarioDAO.guardar(nuevoUsuario);
            SesionActual.setUsuarioActivo(nuevoUsuario);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Registro Exitoso", "Cuenta creada. Completa tu perfil físico.");
            Navegacion.irAlPerfil(txtCorreo);

        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Registro", "No se pudo registrar el usuario: " + e.getMessage());
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

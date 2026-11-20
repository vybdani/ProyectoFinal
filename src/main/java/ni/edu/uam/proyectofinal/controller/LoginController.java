package ni.edu.uam.proyectofinal.controller;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ni.edu.uam.proyectofinal.dao.UsuarioDAO;
import ni.edu.uam.proyectofinal.model.Usuario;
import ni.edu.uam.proyectofinal.util.Alertas;
import ni.edu.uam.proyectofinal.util.CuentaDialogos;
import ni.edu.uam.proyectofinal.util.Navegacion;
import ni.edu.uam.proyectofinal.util.SesionActual;
import ni.edu.uam.proyectofinal.util.Validacion;
import ni.edu.uam.proyectofinal.util.Validacion.ValidacionException;

public class LoginController {

    private static final int MIN_CONTRASENA = 6;
    private static final int MAX_CONTRASENA = 64;

    @FXML private TextField txtCorreo;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtNombreRegistro;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @FXML
    private void handleLogin() {
        try {
            String correo = Validacion.texto(txtCorreo, "tu correo", 100);
            String password = contrasena();

            Usuario usuario = usuarioDAO.autenticar(correo, password);
            if (usuario == null) {
                txtPassword.clear();
                // Mismo mensaje para correo o contraseña incorrectos: no revela qué cuentas existen
                Alertas.aviso("Acceso denegado", "El correo o la contraseña no son correctos.");
                return;
            }
            SesionActual.setUsuarioActivo(usuario);
            // Cuentas creadas antes de existir la recuperación: se les ofrece configurarla una vez
            if (usuario.getPreguntaSeguridad() == null) {
                ofrecerPreguntaSeguridad(usuario);
            }
            if (usuario.getPerfilFisico() == null) {
                Alertas.info("Perfil incompleto", "Completa tu perfil físico para continuar.");
                Navegacion.irAlPerfil(txtCorreo);
            } else {
                Navegacion.irAlMenuPrincipal(txtCorreo);
            }
        } catch (ValidacionException e) {
            Alertas.aviso("Revisa tus datos", e.getMessage());
        } catch (Exception e) {
            Alertas.error("No se pudo iniciar sesión", e);
        }
    }

    @FXML
    private void handleRegistro() {
        try {
            String nombre = Validacion.texto(txtNombreRegistro, "tu nombre", 100);
            if (nombre.length() < 2 || !nombre.matches("[\\p{L} .'-]+")) {
                throw Validacion.fallo(txtNombreRegistro, "El nombre solo puede tener letras y espacios (mínimo 2).");
            }
            String correo = Validacion.correo(txtCorreo);
            String password = contrasena();

            if (usuarioDAO.existeCorreo(correo)) {
                throw Validacion.fallo(txtCorreo, "Ya existe una cuenta con ese correo. Inicia sesión.");
            }
            // La pregunta de seguridad permite recuperar la cuenta si se olvida la contraseña
            CuentaDialogos.PreguntaSeguridad seguridad =
                    CuentaDialogos.elegirPregunta(txtCorreo, false).orElse(null);
            if (seguridad == null) {
                Alertas.aviso("Cuenta no creada", "Elige una pregunta de seguridad para poder crear tu cuenta.");
                return;
            }
            Usuario cuenta = new Usuario(nombre, correo.toLowerCase(), password);
            cuenta.setPreguntaSeguridad(seguridad.pregunta());
            cuenta.setRespuestaSeguridad(seguridad.respuesta()); // el DAO la guarda como hash
            Usuario nuevoUsuario = usuarioDAO.guardar(cuenta);
            SesionActual.setUsuarioActivo(nuevoUsuario);
            Alertas.info("Cuenta creada", "Bienvenido/a, " + nombre + ". Ahora completa tu perfil físico.");
            Navegacion.irAlPerfil(txtCorreo);
        } catch (ValidacionException e) {
            Alertas.aviso("Revisa tus datos", e.getMessage());
        } catch (Exception e) {
            Alertas.error("No se pudo crear la cuenta", e);
        }
    }

    /** Recupera la cuenta respondiendo la pregunta de seguridad y poniendo una contraseña nueva. */
    @FXML
    private void handleOlvideContrasena() {
        CuentaDialogos.recuperarContrasena(txtCorreo, txtCorreo.getText()).ifPresent(correo -> {
            txtCorreo.setText(correo);
            txtPassword.clear();
            txtPassword.requestFocus();
            Alertas.info("Contraseña cambiada", "Listo. Ya puedes iniciar sesión con tu contraseña nueva.");
        });
    }

    private void ofrecerPreguntaSeguridad(Usuario usuario) {
        CuentaDialogos.elegirPregunta(txtCorreo, true).ifPresent(seguridad -> {
            try {
                usuarioDAO.configurarPreguntaSeguridad(usuario.getId(), seguridad.pregunta(), seguridad.respuesta());
                usuario.setPreguntaSeguridad(seguridad.pregunta());
            } catch (Exception e) {
                Alertas.error("No se pudo guardar la pregunta", e);
            }
        });
    }

    /** La contraseña no se recorta: los espacios forman parte de ella. */
    private String contrasena() {
        String password = txtPassword.getText() == null ? "" : txtPassword.getText();
        if (password.isEmpty()) {
            throw Validacion.fallo(txtPassword, "Escribe tu contraseña.");
        }
        if (password.length() < MIN_CONTRASENA || password.length() > MAX_CONTRASENA) {
            throw Validacion.fallo(txtPassword,
                    "La contraseña debe tener entre " + MIN_CONTRASENA + " y " + MAX_CONTRASENA + " caracteres.");
        }
        return password;
    }
}

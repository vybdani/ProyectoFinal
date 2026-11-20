package ni.edu.uam.proyectofinal.util;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import ni.edu.uam.proyectofinal.dao.UsuarioDAO;

import java.util.List;
import java.util.Optional;

/**
 * Ventanas de la cuenta: elegir la pregunta de seguridad y recuperar la contraseña olvidada.
 * Solea es una app de escritorio sin servidor de correo, así que la cuenta se recupera
 * respondiendo la pregunta de seguridad que el usuario eligió al registrarse.
 */
public final class CuentaDialogos {

    public static final List<String> PREGUNTAS = List.of(
            "¿Cómo se llamaba tu primera mascota?",
            "¿En qué ciudad naciste?",
            "¿Cuál es el nombre de tu mejor amigo o amiga de la infancia?",
            "¿Cuál es tu comida favorita?",
            "¿Cómo se llamaba tu escuela primaria?",
            "¿Cuál es el segundo nombre de tu mamá?");

    private static final int MAX_INTENTOS = 5;

    private CuentaDialogos() {
    }

    /** Pregunta y respuesta elegidas por el usuario. */
    public record PreguntaSeguridad(String pregunta, String respuesta) {
    }

    /**
     * Pide elegir una pregunta de seguridad y escribir la respuesta.
     *
     * @param permitirOmitir true para mostrar "Ahora no" (cuentas que ya existían)
     */
    public static Optional<PreguntaSeguridad> elegirPregunta(Node origen, boolean permitirOmitir) {
        Dialog<PreguntaSeguridad> dialogo = crear(origen, "Pregunta de seguridad",
                "Si algún día olvidas tu contraseña, podrás recuperarla respondiendo esta pregunta.");

        ComboBox<String> cmbPregunta = new ComboBox<>();
        cmbPregunta.getItems().setAll(PREGUNTAS);
        cmbPregunta.setPromptText("Elige una pregunta");
        cmbPregunta.setMaxWidth(Double.MAX_VALUE);
        TextField txtRespuesta = new TextField();
        txtRespuesta.setPromptText("Tu respuesta");
        Label lblError = error();

        dialogo.getDialogPane().setContent(contenido(
                etiqueta("Pregunta"), cmbPregunta, etiqueta("Respuesta"), txtRespuesta,
                nota("No distingue mayúsculas ni tildes. Elige algo que solo tú sepas y que no cambie con el tiempo."),
                lblError));

        ButtonType guardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().add(guardar);
        if (permitirOmitir) {
            dialogo.getDialogPane().getButtonTypes().add(new ButtonType("Ahora no", ButtonBar.ButtonData.CANCEL_CLOSE));
        } else {
            dialogo.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        }
        estiloPrimario(dialogo, guardar);

        dialogo.getDialogPane().lookupButton(guardar).addEventFilter(ActionEvent.ACTION, evento -> {
            try {
                Validacion.seleccion(cmbPregunta, "una pregunta");
                String respuesta = Validacion.texto(txtRespuesta, "la respuesta", 100);
                if (respuesta.length() < 2) {
                    throw Validacion.fallo(txtRespuesta, "La respuesta debe tener al menos 2 caracteres.");
                }
            } catch (Validacion.ValidacionException e) {
                lblError.setText(e.getMessage());
                evento.consume();
            }
        });
        dialogo.setResultConverter(boton -> boton == guardar
                ? new PreguntaSeguridad(cmbPregunta.getValue(), txtRespuesta.getText().trim())
                : null);
        Platform.runLater(cmbPregunta::requestFocus);
        return dialogo.showAndWait();
    }

    /**
     * Recuperar la contraseña: correo → pregunta de seguridad → respuesta y contraseña nueva.
     * Devuelve el correo de la cuenta recuperada (para dejarlo escrito en el login).
     */
    public static Optional<String> recuperarContrasena(Node origen, String correoEscrito) {
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        Dialog<String> dialogo = crear(origen, "Recuperar contraseña",
                "Escribe tu correo, responde tu pregunta de seguridad y elige una contraseña nueva.");

        TextField txtCorreo = new TextField(correoEscrito == null ? "" : correoEscrito.trim());
        txtCorreo.setPromptText("tu@correo.com");
        Button btnBuscar = new Button("Buscar mi pregunta");
        Label lblPregunta = new Label();
        lblPregunta.setWrapText(true);
        lblPregunta.getStyleClass().add("dato");
        TextField txtRespuesta = new TextField();
        txtRespuesta.setPromptText("Tu respuesta");
        PasswordField txtNueva = new PasswordField();
        txtNueva.setPromptText("Contraseña nueva (6 a 64 caracteres)");
        PasswordField txtConfirmar = new PasswordField();
        txtConfirmar.setPromptText("Repite la contraseña nueva");
        Label lblError = error();

        VBox paso2 = new VBox(8, etiqueta("Tu pregunta"), lblPregunta, txtRespuesta,
                etiqueta("Contraseña nueva"), txtNueva, txtConfirmar);
        paso2.setVisible(false);
        paso2.setManaged(false);

        dialogo.getDialogPane().setContent(contenido(
                etiqueta("Correo de tu cuenta"), txtCorreo, btnBuscar, paso2, lblError));

        ButtonType cambiar = new ButtonType("Cambiar contraseña", ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(cambiar, ButtonType.CANCEL);
        estiloPrimario(dialogo, cambiar);
        Node btnCambiar = dialogo.getDialogPane().lookupButton(cambiar);
        btnCambiar.setDisable(true);

        String[] correoConfirmado = new String[1];
        btnBuscar.setOnAction(e -> {
            lblError.setText("");
            try {
                String correo = Validacion.correo(txtCorreo);
                String pregunta = usuarioDAO.buscarPreguntaSeguridad(correo);
                if (pregunta == null) {
                    // Mismo mensaje si no existe la cuenta: no revela qué correos están registrados
                    lblError.setText("No encontramos una cuenta con pregunta de seguridad para ese correo.");
                    return;
                }
                correoConfirmado[0] = correo;
                lblPregunta.setText(pregunta);
                paso2.setVisible(true);
                paso2.setManaged(true);
                txtCorreo.setDisable(true);
                btnBuscar.setDisable(true);
                btnCambiar.setDisable(false);
                dialogo.getDialogPane().getScene().getWindow().sizeToScene();
                txtRespuesta.requestFocus();
            } catch (Validacion.ValidacionException ex) {
                lblError.setText(ex.getMessage());
            } catch (Exception ex) {
                lblError.setText(Alertas.mensajeDe(ex));
            }
        });
        txtCorreo.setOnAction(e -> btnBuscar.fire());

        int[] intentos = {0};
        btnCambiar.addEventFilter(ActionEvent.ACTION, evento -> {
            lblError.setText("");
            try {
                String respuesta = Validacion.texto(txtRespuesta, "la respuesta", 100);
                String nueva = txtNueva.getText() == null ? "" : txtNueva.getText();
                if (nueva.length() < 6 || nueva.length() > 64) {
                    throw Validacion.fallo(txtNueva, "La contraseña nueva debe tener entre 6 y 64 caracteres.");
                }
                if (!nueva.equals(txtConfirmar.getText())) {
                    throw Validacion.fallo(txtConfirmar, "Las dos contraseñas no coinciden.");
                }
                if (!usuarioDAO.restablecerContrasena(correoConfirmado[0], respuesta, nueva)) {
                    intentos[0]++;
                    if (intentos[0] >= MAX_INTENTOS) {
                        Alertas.aviso("Demasiados intentos", "La respuesta no coincidió varias veces. Inténtalo más tarde.");
                        dialogo.setResult(null);
                        dialogo.close();
                        return;
                    }
                    throw Validacion.fallo(txtRespuesta, "La respuesta no coincide. Te quedan "
                            + (MAX_INTENTOS - intentos[0]) + " intento(s).");
                }
            } catch (Validacion.ValidacionException ex) {
                lblError.setText(ex.getMessage());
                evento.consume();
            } catch (Exception ex) {
                lblError.setText(Alertas.mensajeDe(ex));
                evento.consume();
            }
        });
        dialogo.setResultConverter(boton -> boton == cambiar ? correoConfirmado[0] : null);
        Platform.runLater(() -> (txtCorreo.getText().isBlank() ? txtCorreo : btnBuscar).requestFocus());
        return dialogo.showAndWait();
    }

    // ------------------------------------------------------------- Interno

    private static <T> Dialog<T> crear(Node origen, String titulo, String explicacion) {
        Dialog<T> dialogo = new Dialog<>();
        dialogo.setTitle("Solea - " + titulo);
        dialogo.setHeaderText(titulo);
        dialogo.setContentText(explicacion);
        if (origen != null && origen.getScene() != null) {
            dialogo.initOwner(origen.getScene().getWindow());
            dialogo.getDialogPane().getStylesheets().addAll(origen.getScene().getStylesheets());
        }
        dialogo.getDialogPane().setMinWidth(460);
        dialogo.getDialogPane().getStyleClass().add(
                SesionActual.MODO_GIMNASIO.equals(SesionActual.getModoActual()) ? "modo-gimnasio" : "modo-nutricion");
        return dialogo;
    }

    private static VBox contenido(Node... nodos) {
        VBox caja = new VBox(8, nodos);
        caja.setPadding(new Insets(4, 0, 0, 0));
        caja.setPrefWidth(420);
        return caja;
    }

    private static Label etiqueta(String texto) {
        Label etiqueta = new Label(texto);
        etiqueta.getStyleClass().add("etiqueta");
        return etiqueta;
    }

    private static Label nota(String texto) {
        Label nota = new Label(texto);
        nota.setWrapText(true);
        nota.getStyleClass().add("nota");
        return nota;
    }

    private static Label error() {
        Label error = new Label();
        error.setWrapText(true);
        error.setStyle("-fx-text-fill: #B3261E;");
        return error;
    }

    private static void estiloPrimario(Dialog<?> dialogo, ButtonType boton) {
        Node nodo = dialogo.getDialogPane().lookupButton(boton);
        if (nodo != null) {
            nodo.getStyleClass().add("primario");
        }
    }
}

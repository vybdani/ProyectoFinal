package ni.edu.uam.proyectofinal.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ni.edu.uam.proyectofinal.HelloApplication;
import ni.edu.uam.proyectofinal.dao.UsuarioDAO;
import ni.edu.uam.proyectofinal.model.PerfilFisico;
import ni.edu.uam.proyectofinal.model.Usuario;
import ni.edu.uam.proyectofinal.util.SesionActual;

import java.io.IOException;

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
    private Usuario usuarioActual;

    @FXML
    public void initialize() {
        usuarioActual = SesionActual.getUsuarioActivo();
        if (usuarioActual != null && usuarioActual.getPerfilFisico() != null) {
            PerfilFisico p = usuarioActual.getPerfilFisico();
            if (p.getPeso() != null) txtPeso.setText(String.valueOf(p.getPeso()));
            if (p.getAltura() != null) txtAltura.setText(String.valueOf(p.getAltura()));
            if (p.getEdad() != null) txtEdad.setText(String.valueOf(p.getEdad()));
            if (p.getPorcentajeGrasa() != null) txtPorcentajeGrasa.setText(String.valueOf(p.getPorcentajeGrasa()));
        }
    }

    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
    }

    @FXML
    private void handleGuardarPerfil() {
        if (usuarioActual == null) {
            usuarioActual = SesionActual.getUsuarioActivo();
        }
        if (usuarioActual == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sesión Requerida", "No hay un usuario activo en sesión.");
            return;
        }

        try {
            double peso = Double.parseDouble(txtPeso.getText().trim());
            double altura = Double.parseDouble(txtAltura.getText().trim());
            int edad = Integer.parseInt(txtEdad.getText().trim());
            double grasa = txtPorcentajeGrasa.getText().trim().isEmpty()
                    ? 0.0
                    : Double.parseDouble(txtPorcentajeGrasa.getText().trim());

            if (peso <= 0 || altura <= 0 || edad <= 0) {
                mostrarAlerta(Alert.AlertType.WARNING, "Datos Inválidos", "Peso, altura y edad deben ser mayores a 0.");
                return;
            }

            PerfilFisico perfil = usuarioActual.getPerfilFisico();
            if (perfil == null) {
                perfil = new PerfilFisico(peso, altura, edad, grasa, "Masculino", "Moderado");
                usuarioActual.setPerfilFisico(perfil);
            } else {
                perfil.setPeso(peso);
                perfil.setAltura(altura);
                perfil.setEdad(edad);
                perfil.setPorcentajeGrasa(grasa);
                if (perfil.getSexo() == null) perfil.setSexo("Masculino");
                if (perfil.getNivelActividad() == null) perfil.setNivelActividad("Moderado");
            }

            usuarioDAO.actualizar(usuarioActual);
            SesionActual.setUsuarioActivo(usuarioActual);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Perfil Guardado", "Tus datos físicos han sido guardados.");
            irAlMenuPrincipal();

        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Formato", "Ingresa valores numéricos válidos en peso, altura y edad.");
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Persistencia", "No se pudo guardar el perfil: " + e.getMessage());
        }
    }

    private void irAlMenuPrincipal() {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("views/menu-principal-view.fxml"));
            Stage stage = (Stage) txtPeso.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 650, 420));
            stage.setTitle("NutriFit - Menú Principal");
            stage.centerOnScreen();
        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Navegación", e.getMessage());
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
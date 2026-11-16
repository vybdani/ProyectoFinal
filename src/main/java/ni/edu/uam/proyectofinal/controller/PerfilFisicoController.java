package ni.edu.uam.proyectofinal.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import ni.edu.uam.proyectofinal.dao.UsuarioDAO;
import ni.edu.uam.proyectofinal.model.PerfilFisico;
import ni.edu.uam.proyectofinal.model.Usuario;
import ni.edu.uam.proyectofinal.util.Navegacion;
import ni.edu.uam.proyectofinal.util.SesionActual;

public class PerfilFisicoController {

    @FXML
    private TextField txtPeso;

    @FXML
    private TextField txtAltura;

    @FXML
    private TextField txtEdad;

    @FXML
    private TextField txtPorcentajeGrasa;

    @FXML
    private ComboBox<String> cmbSexo;

    @FXML
    private ComboBox<String> cmbNivelActividad;

    @FXML
    private Button btnVolver;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private Usuario usuarioActual;

    @FXML
    public void initialize() {
        cmbSexo.setItems(FXCollections.observableArrayList("Masculino", "Femenino"));
        // Los valores deben coincidir con los que entiende CalculoNutricionalService
        cmbNivelActividad.setItems(FXCollections.observableArrayList("Sedentario", "Ligero", "Moderado", "Alto"));

        usuarioActual = SesionActual.getUsuarioActivo();
        PerfilFisico p = usuarioActual != null ? usuarioActual.getPerfilFisico() : null;

        // Si todavía no tiene perfil (recién registrado) no puede volver al menú sin guardarlo
        btnVolver.setVisible(p != null);
        btnVolver.setManaged(p != null);

        if (p != null) {
            if (p.getPeso() != null) txtPeso.setText(String.valueOf(p.getPeso()));
            if (p.getAltura() != null) txtAltura.setText(String.valueOf(p.getAltura()));
            if (p.getEdad() != null) txtEdad.setText(String.valueOf(p.getEdad()));
            if (p.getPorcentajeGrasa() != null && p.getPorcentajeGrasa() > 0) {
                txtPorcentajeGrasa.setText(String.valueOf(p.getPorcentajeGrasa()));
            }
            cmbSexo.setValue(p.getSexo());
            cmbNivelActividad.setValue(p.getNivelActividad());
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
            String sexo = cmbSexo.getValue();
            String nivel = cmbNivelActividad.getValue();

            if (peso < 30 || peso > 300) {
                mostrarAlerta(Alert.AlertType.WARNING, "Datos Inválidos", "El peso debe estar entre 30 y 300 kg.");
                return;
            }
            if (altura < 120 || altura > 230) {
                mostrarAlerta(Alert.AlertType.WARNING, "Datos Inválidos", "La altura debe estar entre 120 y 230 cm.");
                return;
            }
            if (edad < 18 || edad > 100) {
                mostrarAlerta(Alert.AlertType.WARNING, "Datos Inválidos",
                        "La edad debe estar entre 18 y 100 años. Los menores de edad necesitan evaluación profesional.");
                return;
            }
            if (grasa < 0 || grasa > 60) {
                mostrarAlerta(Alert.AlertType.WARNING, "Datos Inválidos", "El % de grasa debe estar entre 0 y 60.");
                return;
            }
            if (sexo == null || nivel == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Datos Incompletos", "Selecciona tu sexo y tu nivel de actividad.");
                return;
            }

            PerfilFisico perfil = usuarioActual.getPerfilFisico();
            if (perfil == null) {
                perfil = new PerfilFisico(peso, altura, edad, grasa, sexo, nivel);
                usuarioActual.setPerfilFisico(perfil);
            } else {
                perfil.setPeso(peso);
                perfil.setAltura(altura);
                perfil.setEdad(edad);
                perfil.setPorcentajeGrasa(grasa);
                perfil.setSexo(sexo);
                perfil.setNivelActividad(nivel);
            }

            usuarioDAO.actualizar(usuarioActual);
            // Se recarga desde la BD para tener los ids generados y evitar duplicar el perfil
            Usuario recargado = usuarioDAO.obtenerUsuarioConPerfil(usuarioActual.getId());
            SesionActual.setUsuarioActivo(recargado != null ? recargado : usuarioActual);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Perfil Guardado", "Tus datos físicos han sido guardados.");
            Navegacion.irAlMenuPrincipal(txtPeso);

        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Formato", "Ingresa valores numéricos válidos en peso, altura, edad y % de grasa.");
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Persistencia", "No se pudo guardar el perfil: " + e.getMessage());
        }
    }

    @FXML
    private void handleVolver() {
        Navegacion.irAlMenuPrincipal(txtPeso);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

package ni.edu.uam.proyectofinal.controller;

import ni.edu.uam.proyectofinal.dao.RequerimientoDiarioDAO;
import ni.edu.uam.proyectofinal.model.RequerimientoDiario;
import ni.edu.uam.nutrifit.entity.Usuario;
import ni.edu.uam.proyectofinal.service.CalculoNutricionalService;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

public class CalculoController {

    @FXML
    private Label lblTMB;

    @FXML
    private Label lblCalorias;

    @FXML
    private Label lblProteinas;

    @FXML
    private Label lblCarbohidratos;

    @FXML
    private Label lblGrasas;

    private Usuario usuario;

    private final CalculoNutricionalService servicio =
            new CalculoNutricionalService();

    private final RequerimientoDiarioDAO requerimientoDAO =
            new RequerimientoDiarioDAO();

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    @FXML
    public void calcular() {

        if (usuario == null) {
            mostrarAlerta("Error", "No se ha seleccionado un usuario.");
            return;
        }

        try {
            RequerimientoDiario resultado =
                    servicio.calcular(usuario);

            requerimientoDAO.guardar(resultado);

            lblTMB.setText(
                    String.format("%.2f", resultado.getTmb())
            );

            lblCalorias.setText(
                    String.format("%.2f", resultado.getCaloriasDiarias())
            );

            lblProteinas.setText(
                    String.format("%.2f g", resultado.getProteinas())
            );

            lblCarbohidratos.setText(
                    String.format("%.2f g", resultado.getCarbohidratos())
            );

            lblGrasas.setText(
                    String.format("%.2f g", resultado.getGrasas())
            );

            mostrarAlerta(
                    "Resultado",
                    "El cálculo se realizó correctamente."
            );

        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage());
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);

        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }
}

package ni.edu.uam.proyectofinal.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import ni.edu.uam.proyectofinal.dao.ObjetivoDAO;
import ni.edu.uam.proyectofinal.dao.RequerimientoDiarioDAO;
import ni.edu.uam.proyectofinal.model.Objetivo;
import ni.edu.uam.proyectofinal.model.RequerimientoDiario;
import ni.edu.uam.proyectofinal.model.Usuario;
import ni.edu.uam.proyectofinal.service.CalculoNutricionalService;
import ni.edu.uam.proyectofinal.util.Navegacion;
import ni.edu.uam.proyectofinal.util.SesionActual;

public class CalculoController {

    @FXML
    private Label lblObjetivo;

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

    private final CalculoNutricionalService servicio = new CalculoNutricionalService();

    private final RequerimientoDiarioDAO requerimientoDAO = new RequerimientoDiarioDAO();

    private final ObjetivoDAO objetivoDAO = new ObjetivoDAO();

    @FXML
    public void initialize() {
        usuario = SesionActual.getUsuarioActivo();
        Objetivo objetivo = buscarObjetivo();
        lblObjetivo.setText(objetivo != null
                ? "Objetivo: " + objetivo.getTipo()
                : "Sin objetivo elegido: se calculará para mantener el peso.");

        // Muestra el último cálculo guardado, si existe
        if (usuario != null) {
            try {
                RequerimientoDiario ultimo = requerimientoDAO.buscarPorUsuario(usuario.getId());
                if (ultimo != null) {
                    mostrarResultado(ultimo);
                }
            } catch (Exception e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo cargar el último cálculo: " + e.getMessage());
            }
        }
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    @FXML
    public void calcular() {
        if (usuario == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Error", "No hay un usuario activo en sesión.");
            return;
        }

        try {
            Objetivo objetivo = buscarObjetivo();
            RequerimientoDiario resultado = servicio.calcular(usuario, objetivo);

            requerimientoDAO.guardar(resultado);

            if (objetivo != null) {
                objetivo.setCaloriasDiarias((int) Math.round(resultado.getCaloriasDiarias()));
                objetivo.setUsuario(usuario);
                objetivoDAO.guardar(objetivo);
            }

            mostrarResultado(resultado);
            mostrarAlerta(Alert.AlertType.INFORMATION, "Resultado", "El cálculo se realizó y se guardó correctamente.");

        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void handleVolver() {
        Navegacion.volver(lblTMB);
    }

    private Objetivo buscarObjetivo() {
        if (usuario == null || SesionActual.getModoActual() == null) {
            return null;
        }
        try {
            return objetivoDAO.buscarPorUsuarioYModo(usuario.getId(), SesionActual.getModoActual());
        } catch (Exception e) {
            return null;
        }
    }

    private void mostrarResultado(RequerimientoDiario resultado) {
        lblTMB.setText(String.format("%.0f kcal", resultado.getTmb()));
        lblCalorias.setText(String.format("%.0f kcal", resultado.getCaloriasDiarias()));
        lblProteinas.setText(String.format("%.0f g", resultado.getProteinas()));
        lblCarbohidratos.setText(String.format("%.0f g", resultado.getCarbohidratos()));
        lblGrasas.setText(String.format("%.0f g", resultado.getGrasas()));
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}

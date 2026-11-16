package ni.edu.uam.proyectofinal.controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ni.edu.uam.proyectofinal.HelloApplication;
import ni.edu.uam.proyectofinal.dao.ItemListaSuperDAO;
import ni.edu.uam.proyectofinal.dao.PlanComidasDAO;
import ni.edu.uam.proyectofinal.model.*;
import ni.edu.uam.proyectofinal.service.GeneradorPlanService;
import ni.edu.uam.proyectofinal.util.SesionActual;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlanSemanalController {

    @FXML private Label lblInfoPlan;
    @FXML private ComboBox<String> cmbFiltroDia;
    @FXML private TableView<ComidaPlan> tblPlanSemanal;
    @FXML private TableColumn<ComidaPlan, String> colDia;
    @FXML private TableColumn<ComidaPlan, String> colTiempo;
    @FXML private TableColumn<ComidaPlan, String> colReceta;
    @FXML private TableColumn<ComidaPlan, String> colCalorias;
    @FXML private TableColumn<ComidaPlan, String> colMacros;

    private final GeneradorPlanService generadorService = new GeneradorPlanService();
    private final PlanComidasDAO planDAO = new PlanComidasDAO();
    private final ItemListaSuperDAO listaSuperDAO = new ItemListaSuperDAO();

    private final ObservableList<ComidaPlan> comidasMostradas = FXCollections.observableArrayList();
    private PlanComidas planActual;

    @FXML
    public void initialize() {
        cmbFiltroDia.setItems(FXCollections.observableArrayList(
                "Todos los días", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"
        ));
        cmbFiltroDia.setValue("Todos los días");

        colDia.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getDiaSemana()));
        colTiempo.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getTiempoComida()));
        colReceta.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getReceta().getNombre()));
        colCalorias.setCellValueFactory(d -> new ReadOnlyStringWrapper(
                String.format("%.0f kcal", d.getValue().getCaloriasEstimadas())
        ));
        colMacros.setCellValueFactory(d -> {
            Receta r = d.getValue().getReceta();
            return new ReadOnlyStringWrapper(String.format("P: %.0fg | C: %.0fg | G: %.0fg",
                    r.getProteinasTotales(), r.getCarbohidratosTotales(), r.getGrasasTotales()));
        });

        tblPlanSemanal.setItems(comidasMostradas);
        cargarUltimoPlan();
    }

    private void cargarUltimoPlan() {
        Usuario u = SesionActual.getUsuarioActivo();
        if (u != null) {
            planActual = planDAO.buscarUltimoPorUsuario(u.getId());
            actualizarVistaPlan();
        }
    }

    @FXML
    private void handleGenerarPlan() {
        Usuario u = SesionActual.getUsuarioActivo();
        if (u == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sesión", "No hay un usuario activo en sesión.");
            return;
        }
        try {
            planActual = generadorService.generarPlanSemanal(u);
            actualizarVistaPlan();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Plan Generado",
                    "Se ha generado y guardado tu plan semanal ajustado a " + String.format("%.0f", planActual.getCaloriasObjetivoDiarias()) + " kcal/día.");
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void handleFiltrarDia() {
        actualizarVistaPlan();
    }

    @FXML
    private void handleEnviarListaSuper() {
        Usuario u = SesionActual.getUsuarioActivo();
        if (u == null || planActual == null || planActual.getComidas().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Aviso", "Primero genera un plan semanal.");
            return;
        }

        Map<String, Double> acumuladoGramos = new HashMap<>();
        for (ComidaPlan comida : planActual.getComidas()) {
            for (RecetaIngrediente ing : comida.getReceta().getIngredientes()) {
                String nombreAlimento = ing.getAlimento().getNombre();
                acumuladoGramos.merge(nombreAlimento, ing.getCantidadGramos(), Double::sum);
            }
        }

        for (Map.Entry<String, Double> entry : acumuladoGramos.entrySet()) {
            listaSuperDAO.guardar(new ItemListaSuper(u, entry.getKey(), entry.getValue(), "Gramos"));
        }

        mostrarAlerta(Alert.AlertType.INFORMATION, "Lista del Súper",
                "Se enviaron " + acumuladoGramos.size() + " ingredientes consolidados a la lista del súper.");
    }

    @FXML
    private void handleVolverMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("views/menu-principal-view.fxml"));
            Stage stage = (Stage) tblPlanSemanal.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 650, 420));
            stage.setTitle("NutriFit - Menú Principal");
            stage.centerOnScreen();
        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    private void actualizarVistaPlan() {
        if (planActual == null) {
            lblInfoPlan.setText("Aún no tienes un plan semanal generado.");
            comidasMostradas.clear();
            return;
        }

        String filtro = cmbFiltroDia.getValue();
        List<ComidaPlan> filtradas = planActual.getComidas().stream()
                .filter(c -> filtro == null || filtro.equals("Todos los días") || c.getDiaSemana().equalsIgnoreCase(filtro))
                .toList();

        comidasMostradas.setAll(filtradas);
        double totalMostrado = filtradas.stream().mapToDouble(ComidaPlan::getCaloriasEstimadas).sum();
        lblInfoPlan.setText(String.format("%s | Meta diaria: %.0f kcal | Total mostrado: %.0f kcal",
                planActual.getNombrePlan(), planActual.getCaloriasObjetivoDiarias(), totalMostrado));
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
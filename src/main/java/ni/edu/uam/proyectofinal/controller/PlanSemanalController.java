package ni.edu.uam.proyectofinal.controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ni.edu.uam.proyectofinal.dao.ItemListaSuperDAO;
import ni.edu.uam.proyectofinal.dao.PlanComidasDAO;
import ni.edu.uam.proyectofinal.model.ComidaPlan;
import ni.edu.uam.proyectofinal.model.ItemListaSuper;
import ni.edu.uam.proyectofinal.model.PlanComidas;
import ni.edu.uam.proyectofinal.model.Receta;
import ni.edu.uam.proyectofinal.model.RecetaIngrediente;
import ni.edu.uam.proyectofinal.model.Usuario;
import ni.edu.uam.proyectofinal.service.GeneradorPlanService;
import ni.edu.uam.proyectofinal.util.Alertas;
import ni.edu.uam.proyectofinal.util.Navegacion;
import ni.edu.uam.proyectofinal.util.SesionActual;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PlanSemanalController {

    private static final String TODOS = "Todos los días";

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
                TODOS, "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"));
        cmbFiltroDia.setValue(TODOS);

        colDia.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getDiaSemana()));
        colTiempo.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getTiempoComida()));
        colReceta.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getReceta().getNombre()));
        colCalorias.setCellValueFactory(d -> new ReadOnlyStringWrapper(
                String.format("%.0f kcal", d.getValue().getCaloriasEstimadas())));
        colMacros.setCellValueFactory(d -> {
            Receta r = d.getValue().getReceta();
            return new ReadOnlyStringWrapper(String.format("P: %.0fg | C: %.0fg | G: %.0fg",
                    r.getProteinasTotales(), r.getCarbohidratosTotales(), r.getGrasasTotales()));
        });
        tblPlanSemanal.setItems(comidasMostradas);

        Usuario u = SesionActual.getUsuarioActivo();
        if (u != null) {
            try {
                planActual = planDAO.buscarUltimoPorUsuario(u.getId());
            } catch (Exception e) {
                Alertas.error("No se pudo cargar tu plan", e);
            }
        }
        actualizarVistaPlan();
    }

    @FXML
    private void handleGenerarPlan() {
        Usuario u = SesionActual.getUsuarioActivo();
        if (u == null) {
            Alertas.aviso("Sesión requerida", "Inicia sesión para generar tu plan semanal.");
            return;
        }
        if (planActual != null && !Alertas.confirmar("Generar plan nuevo",
                "Ya tienes un plan semanal. ¿Quieres generar uno nuevo? El anterior se conserva en el historial.")) {
            return;
        }
        try {
            generadorService.generarPlanSemanal(u);
            // Se recarga desde la BD para tener las recetas con sus ingredientes
            planActual = planDAO.buscarUltimoPorUsuario(u.getId());
            cmbFiltroDia.setValue(TODOS);
            actualizarVistaPlan();
            Alertas.info("Plan generado", String.format(
                    "Tu plan semanal se ajustó a %.0f kcal por día.", planActual.getCaloriasObjetivoDiarias()));
        } catch (Exception e) {
            // Por ejemplo: no hay recetas registradas (el servicio explica el motivo)
            Alertas.error("No se pudo generar el plan", e);
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
            Alertas.aviso("Sin plan", "Primero genera tu plan semanal.");
            return;
        }

        // Suma los gramos de cada alimento en todas las comidas de la semana
        Map<String, Double> gramosPorAlimento = new LinkedHashMap<>();
        for (ComidaPlan comida : planActual.getComidas()) {
            for (RecetaIngrediente ing : comida.getReceta().getIngredientes()) {
                gramosPorAlimento.merge(ing.getAlimento().getNombre(), ing.getCantidadGramos(), Double::sum);
            }
        }
        if (!Alertas.confirmar("Enviar a la lista del súper", "Se agregarán " + gramosPorAlimento.size()
                + " ingredientes de tu plan a la lista del súper. ¿Continuar?")) {
            return;
        }

        List<ItemListaSuper> items = new ArrayList<>();
        gramosPorAlimento.forEach((nombre, gramos) ->
                items.add(new ItemListaSuper(u, nombre, Math.round(gramos * 10) / 10.0, "Gramos")));
        try {
            // Una sola transacción: si algo falla no queda la lista a medias
            int nuevos = listaSuperDAO.guardarTodos(u.getId(), items);
            int sumados = items.size() - nuevos;
            Alertas.info("Lista del súper actualizada", nuevos + " producto(s) nuevos"
                    + (sumados > 0 ? " y " + sumados + " sumados a productos que ya tenías pendientes." : "."));
        } catch (Exception e) {
            Alertas.error("No se pudo enviar a la lista del súper", e);
        }
    }

    @FXML
    private void handleVolverMenu() {
        Navegacion.volver(tblPlanSemanal);
    }

    private void actualizarVistaPlan() {
        if (planActual == null) {
            lblInfoPlan.setText("Aún no tienes un plan semanal. Pulsa «Generar plan» para crear uno.");
            comidasMostradas.clear();
            return;
        }
        String filtro = cmbFiltroDia.getValue();
        List<ComidaPlan> filtradas = planActual.getComidas().stream()
                .filter(c -> filtro == null || TODOS.equals(filtro) || c.getDiaSemana().equalsIgnoreCase(filtro))
                .toList();
        comidasMostradas.setAll(filtradas);

        double totalMostrado = filtradas.stream().mapToDouble(ComidaPlan::getCaloriasEstimadas).sum();
        lblInfoPlan.setText(String.format("%s | Meta diaria: %.0f kcal | Total mostrado: %.0f kcal",
                planActual.getNombrePlan(), planActual.getCaloriasObjetivoDiarias(), totalMostrado));
    }
}

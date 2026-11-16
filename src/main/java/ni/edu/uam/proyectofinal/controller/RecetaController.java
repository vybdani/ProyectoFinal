package ni.edu.uam.proyectofinal.controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ni.edu.uam.proyectofinal.HelloApplication;
import ni.edu.uam.proyectofinal.dao.AlimentoDAO;
import ni.edu.uam.proyectofinal.dao.RecetaDAO;
import ni.edu.uam.proyectofinal.model.Alimento;
import ni.edu.uam.proyectofinal.model.Receta;
import ni.edu.uam.proyectofinal.model.RecetaIngrediente;

import java.io.IOException;
import java.util.List;

public class RecetaController {

    @FXML private TextField txtNombreReceta;
    @FXML private ComboBox<String> cmbTipoComida;
    @FXML private TextField txtTiempoPrep;
    @FXML private TextArea txtDescripcion;

    @FXML private ComboBox<Alimento> cmbAlimento;
    @FXML private TextField txtGramos;
    @FXML private Label lblTotalesReceta;

    @FXML private TableView<RecetaIngrediente> tblIngredientes;
    @FXML private TableColumn<RecetaIngrediente, String> colIngAlimento;
    @FXML private TableColumn<RecetaIngrediente, Double> colIngGramos;
    @FXML private TableColumn<RecetaIngrediente, String> colIngKcal;

    @FXML private TableView<Receta> tblRecetas;
    @FXML private TableColumn<Receta, String> colRecNombre;
    @FXML private TableColumn<Receta, String> colRecTipo;
    @FXML private TableColumn<Receta, String> colRecKcal;
    @FXML private TableColumn<Receta, String> colRecMacros;

    private final RecetaDAO recetaDAO = new RecetaDAO();
    private final AlimentoDAO alimentoDAO = new AlimentoDAO();

    private final ObservableList<RecetaIngrediente> ingredientesActuales = FXCollections.observableArrayList();
    private final ObservableList<Receta> listaRecetas = FXCollections.observableArrayList();
    private Receta recetaSeleccionada;

    @FXML
    public void initialize() {
        cmbTipoComida.setItems(FXCollections.observableArrayList("Desayuno", "Almuerzo", "Cena", "Merienda"));

        colIngAlimento.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getAlimento().getNombre()));
        colIngGramos.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getCantidadGramos()));
        colIngKcal.setCellValueFactory(d -> new ReadOnlyStringWrapper(String.format("%.1f kcal", d.getValue().getCalorias())));
        tblIngredientes.setItems(ingredientesActuales);

        colRecNombre.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getNombre()));
        colRecTipo.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getTipoComida()));
        colRecKcal.setCellValueFactory(d -> new ReadOnlyStringWrapper(String.format("%.0f kcal", d.getValue().getCaloriasTotales())));
        colRecMacros.setCellValueFactory(d -> new ReadOnlyStringWrapper(
                String.format("P: %.0fg | C: %.0fg | G: %.0fg",
                        d.getValue().getProteinasTotales(),
                        d.getValue().getCarbohidratosTotales(),
                        d.getValue().getGrasasTotales())
        ));
        tblRecetas.setItems(listaRecetas);

        tblRecetas.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, nueva) -> {
            if (nueva != null) {
                cargarRecetaEnFormulario(nueva);
            }
        });

        cargarAlimentos();
        cargarRecetas();
    }

    private void cargarAlimentos() {
        List<Alimento> alimentos = alimentoDAO.listarTodos();
        if (alimentos.isEmpty()) {
            alimentoDAO.guardar(new Alimento("Pechuga de Pollo", "Proteína", 165.0, 31.0, 0.0, 3.6));
            alimentoDAO.guardar(new Alimento("Arroz Blanco Cocido", "Carbohidrato", 130.0, 2.7, 28.0, 0.3));
            alimentoDAO.guardar(new Alimento("Huevo Entero", "Proteína", 155.0, 13.0, 1.1, 11.0));
            alimentoDAO.guardar(new Alimento("Avena en Hojuelas", "Carbohidrato", 389.0, 16.9, 66.3, 6.9));
            alimentoDAO.guardar(new Alimento("Aguacate", "Grasa", 160.0, 2.0, 8.5, 14.7));
            alimentoDAO.guardar(new Alimento("Banano", "Fruta", 89.0, 1.1, 22.8, 0.3));
            alimentos = alimentoDAO.listarTodos();
        }
        cmbAlimento.setItems(FXCollections.observableArrayList(alimentos));
    }

    private void cargarRecetas() {
        listaRecetas.setAll(recetaDAO.listarTodas());
    }

    @FXML
    private void handleAgregarIngrediente() {
        Alimento alimento = cmbAlimento.getValue();
        if (alimento == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", "Selecciona un alimento.");
            return;
        }
        try {
            double gramos = Double.parseDouble(txtGramos.getText().trim());
            if (gramos <= 0) {
                mostrarAlerta(Alert.AlertType.WARNING, "Validación", "Los gramos deben ser mayores a 0.");
                return;
            }
            ingredientesActuales.add(new RecetaIngrediente(alimento, gramos));
            txtGramos.clear();
            actualizarTotalesEtiqueta();
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Ingresa una cantidad numérica válida en gramos.");
        }
    }

    @FXML
    private void handleQuitarIngrediente() {
        RecetaIngrediente sel = tblIngredientes.getSelectionModel().getSelectedItem();
        if (sel != null) {
            ingredientesActuales.remove(sel);
            actualizarTotalesEtiqueta();
        }
    }

    @FXML
    private void handleGuardarReceta() {
        String nombre = txtNombreReceta.getText().trim();
        String tipo = cmbTipoComida.getValue();

        if (nombre.isEmpty() || tipo == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos obligatorios", "Ingresa el nombre de la receta y el tipo de comida.");
            return;
        }
        if (ingredientesActuales.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin ingredientes", "Agrega al menos un ingrediente a la receta.");
            return;
        }

        try {
            int tiempo = txtTiempoPrep.getText().trim().isEmpty() ? 15 : Integer.parseInt(txtTiempoPrep.getText().trim());

            if (recetaSeleccionada == null) {
                Receta nueva = new Receta(nombre, tipo, txtDescripcion.getText().trim(), tiempo);
                for (RecetaIngrediente ing : ingredientesActuales) {
                    nueva.agregarIngrediente(new RecetaIngrediente(ing.getAlimento(), ing.getCantidadGramos()));
                }
                recetaDAO.guardar(nueva);
                mostrarAlerta(Alert.AlertType.INFORMATION, "Guardado", "Receta registrada correctamente.");
            } else {
                recetaSeleccionada.setNombre(nombre);
                recetaSeleccionada.setTipoComida(tipo);
                recetaSeleccionada.setDescripcion(txtDescripcion.getText().trim());
                recetaSeleccionada.setTiempoPreparacionMin(tiempo);
                recetaSeleccionada.getIngredientes().clear();
                for (RecetaIngrediente ing : ingredientesActuales) {
                    recetaSeleccionada.agregarIngrediente(new RecetaIngrediente(ing.getAlimento(), ing.getCantidadGramos()));
                }
                recetaDAO.actualizar(recetaSeleccionada);
                mostrarAlerta(Alert.AlertType.INFORMATION, "Actualizado", "Receta actualizada correctamente.");
            }

            limpiarFormulario();
            cargarRecetas();
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Formato inválido", "El tiempo de preparación debe ser un número entero.");
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Persistencia", e.getMessage());
        }
    }

    @FXML
    private void handleEliminarReceta() {
        Receta sel = tblRecetas.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección", "Selecciona una receta de la tabla para eliminar.");
            return;
        }
        try {
            recetaDAO.eliminar(sel.getId());
            limpiarFormulario();
            cargarRecetas();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Eliminada", "Receta eliminada correctamente.");
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error al eliminar", "No se puede eliminar la receta porque está asignada a un plan de comidas.");
        }
    }

    @FXML
    private void handleVerResumenHQL() {
        List<Object[]> resumen = recetaDAO.obtenerResumenCaloricoRecetas();
        if (resumen.isEmpty()) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Consulta HQL", "No hay recetas registradas todavía.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Object[] fila : resumen) {
            sb.append(String.format("• %s [%s]: %.0f kcal (P: %.1fg | C: %.1fg | G: %.1fg)\n",
                    fila[1], fila[2], (Double) fila[3], (Double) fila[4], (Double) fila[5], (Double) fila[6]));
        }
        mostrarAlerta(Alert.AlertType.INFORMATION, "Resumen Calórico por Receta (HQL)", sb.toString());
    }

    @FXML
    private void handleLimpiar() {
        limpiarFormulario();
    }

    @FXML
    private void handleVolverMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("views/menu-principal-view.fxml"));
            Stage stage = (Stage) txtNombreReceta.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 650, 420));
            stage.setTitle("NutriFit - Menú Principal");
            stage.centerOnScreen();
        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    private void cargarRecetaEnFormulario(Receta r) {
        recetaSeleccionada = r;
        txtNombreReceta.setText(r.getNombre());
        cmbTipoComida.setValue(r.getTipoComida());
        txtTiempoPrep.setText(r.getTiempoPreparacionMin() != null ? String.valueOf(r.getTiempoPreparacionMin()) : "");
        txtDescripcion.setText(r.getDescripcion() != null ? r.getDescripcion() : "");
        ingredientesActuales.setAll(r.getIngredientes());
        actualizarTotalesEtiqueta();
    }

    private void limpiarFormulario() {
        recetaSeleccionada = null;
        txtNombreReceta.clear();
        cmbTipoComida.setValue(null);
        txtTiempoPrep.clear();
        txtDescripcion.clear();
        txtGramos.clear();
        ingredientesActuales.clear();
        tblRecetas.getSelectionModel().clearSelection();
        actualizarTotalesEtiqueta();
    }

    private void actualizarTotalesEtiqueta() {
        double kcal = ingredientesActuales.stream().mapToDouble(RecetaIngrediente::getCalorias).sum();
        double prot = ingredientesActuales.stream().mapToDouble(RecetaIngrediente::getProteinas).sum();
        double carb = ingredientesActuales.stream().mapToDouble(RecetaIngrediente::getCarbohidratos).sum();
        double gras = ingredientesActuales.stream().mapToDouble(RecetaIngrediente::getGrasas).sum();
        lblTotalesReceta.setText(String.format("Total Receta: %.0f kcal | Proteína: %.1fg | Carbs: %.1fg | Grasas: %.1fg", kcal, prot, carb, gras));
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
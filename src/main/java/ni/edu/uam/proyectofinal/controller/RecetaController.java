package ni.edu.uam.proyectofinal.controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import ni.edu.uam.proyectofinal.dao.AlimentoDAO;
import ni.edu.uam.proyectofinal.dao.RecetaDAO;
import ni.edu.uam.proyectofinal.model.Alimento;
import ni.edu.uam.proyectofinal.model.Receta;
import ni.edu.uam.proyectofinal.model.RecetaIngrediente;
import ni.edu.uam.proyectofinal.util.Alertas;
import ni.edu.uam.proyectofinal.util.ComboBuscable;
import ni.edu.uam.proyectofinal.util.BuscadorAlimentosDialog;
import ni.edu.uam.proyectofinal.util.Navegacion;
import ni.edu.uam.proyectofinal.util.Validacion;
import ni.edu.uam.proyectofinal.util.Validacion.ValidacionException;

import java.util.List;

public class RecetaController {

    private static final int MAX_INGREDIENTES = 20;

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
        // Se puede escribir para buscar el alimento (sin importar tildes ni mayúsculas)
        ComboBuscable.activar(cmbAlimento, a -> a.getNombre() + "  ·  " + Math.round(a.getCalorias()) + " kcal / 100 g");
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
                        d.getValue().getGrasasTotales())));
        tblRecetas.setItems(listaRecetas);

        tblRecetas.getSelectionModel().selectedItemProperty().addListener((obs, anterior, nueva) -> {
            if (nueva != null) {
                cargarRecetaEnFormulario(nueva);
            }
        });

        try {
            new ni.edu.uam.proyectofinal.service.DatosInicialesService().asegurar();
            ComboBuscable.cargar(cmbAlimento, alimentoDAO.listarTodos());
            cargarRecetas();
        } catch (Exception e) {
            Alertas.error("No se pudieron cargar las recetas", e);
        }
        actualizarTotalesEtiqueta();
    }

    private void cargarRecetas() {
        listaRecetas.setAll(recetaDAO.listarTodas());
    }

    @FXML
    private void handleAgregarIngrediente() {
        try {
            Alimento alimento = Validacion.seleccion(cmbAlimento, "un alimento");
            double gramos = Validacion.decimal(txtGramos, "los gramos", 1, 2000);

            // Si el alimento ya está en la receta, se suman los gramos en vez de repetirlo
            RecetaIngrediente existente = ingredientesActuales.stream()
                    .filter(i -> i.getAlimento().getId().equals(alimento.getId()))
                    .findFirst()
                    .orElse(null);
            if (existente != null) {
                double total = existente.getCantidadGramos() + gramos;
                if (total > 2000) {
                    throw Validacion.fallo(txtGramos, "Con esa cantidad, " + alimento.getNombre()
                            + " pasaría de 2000 g en la receta.");
                }
                ingredientesActuales.set(ingredientesActuales.indexOf(existente), new RecetaIngrediente(alimento, total));
            } else {
                if (ingredientesActuales.size() >= MAX_INGREDIENTES) {
                    throw Validacion.fallo(cmbAlimento, "Una receta admite como máximo " + MAX_INGREDIENTES + " ingredientes.");
                }
                ingredientesActuales.add(new RecetaIngrediente(alimento, gramos));
            }
            txtGramos.clear();
            cmbAlimento.setValue(null);
            actualizarTotalesEtiqueta();
        } catch (ValidacionException e) {
            Alertas.aviso("Revisa el ingrediente", e.getMessage());
        }
    }

    /** Busca un alimento en internet (API de Solea → Open Food Facts) para usarlo como ingrediente. */
    @FXML
    private void handleBuscarEnInternet() {
        BuscadorAlimentosDialog.mostrar(txtNombreReceta).ifPresent(alimento -> {
            try {
                ComboBuscable.cargar(cmbAlimento, alimentoDAO.listarTodos());
                cmbAlimento.getItems().stream()
                        .filter(a -> a.getId().equals(alimento.getId()))
                        .findFirst()
                        .ifPresent(cmbAlimento::setValue);
                txtGramos.requestFocus();
            } catch (Exception e) {
                Alertas.error("No se pudieron recargar los alimentos", e);
            }
        });
    }

    @FXML
    private void handleQuitarIngrediente() {
        RecetaIngrediente sel = tblIngredientes.getSelectionModel().getSelectedItem();
        if (sel == null) {
            Alertas.aviso("Nada seleccionado", "Selecciona en la tabla el ingrediente que quieres quitar.");
            return;
        }
        ingredientesActuales.remove(sel);
        actualizarTotalesEtiqueta();
    }

    @FXML
    private void handleGuardarReceta() {
        try {
            String nombre = Validacion.texto(txtNombreReceta, "el nombre de la receta", 120);
            String tipo = Validacion.seleccion(cmbTipoComida, "el tipo de comida");
            Integer tiempoEscrito = Validacion.enteroOpcional(txtTiempoPrep, "el tiempo de preparación (min)", 1, 600);
            int tiempo = tiempoEscrito != null ? tiempoEscrito : 15;
            String descripcion = Validacion.textoOpcional(txtDescripcion, "la descripción", 500);
            if (ingredientesActuales.isEmpty()) {
                throw Validacion.fallo(cmbAlimento, "Agrega al menos un ingrediente a la receta.");
            }
            boolean nombreRepetido = listaRecetas.stream()
                    .anyMatch(r -> r.getNombre().equalsIgnoreCase(nombre)
                            && (recetaSeleccionada == null || !r.getId().equals(recetaSeleccionada.getId())));
            if (nombreRepetido) {
                throw Validacion.fallo(txtNombreReceta, "Ya existe una receta llamada \"" + nombre + "\".");
            }

            if (recetaSeleccionada == null) {
                Receta nueva = new Receta(nombre, tipo, descripcion, tiempo);
                ingredientesActuales.forEach(i ->
                        nueva.agregarIngrediente(new RecetaIngrediente(i.getAlimento(), i.getCantidadGramos())));
                recetaDAO.guardar(nueva);
                Alertas.info("Receta guardada", "\"" + nombre + "\" se agregó a tus recetas.");
            } else {
                recetaSeleccionada.setNombre(nombre);
                recetaSeleccionada.setTipoComida(tipo);
                recetaSeleccionada.setDescripcion(descripcion);
                recetaSeleccionada.setTiempoPreparacionMin(tiempo);
                // orphanRemoval borra en la BD los ingredientes que se quitan de la lista
                recetaSeleccionada.getIngredientes().clear();
                ingredientesActuales.forEach(i ->
                        recetaSeleccionada.agregarIngrediente(new RecetaIngrediente(i.getAlimento(), i.getCantidadGramos())));
                recetaDAO.actualizar(recetaSeleccionada);
                Alertas.info("Receta actualizada", "Se guardaron los cambios de \"" + nombre + "\".");
            }
            limpiarFormulario();
            cargarRecetas();
        } catch (ValidacionException e) {
            Alertas.aviso("Revisa la receta", e.getMessage());
        } catch (Exception e) {
            Alertas.error("No se pudo guardar la receta", e);
            recargarSinPerderFormulario();
        }
    }

    @FXML
    private void handleEliminarReceta() {
        Receta sel = tblRecetas.getSelectionModel().getSelectedItem();
        if (sel == null) {
            Alertas.aviso("Nada seleccionado", "Selecciona en la tabla la receta que quieres eliminar.");
            return;
        }
        if (!Alertas.confirmar("Eliminar receta", "¿Eliminar la receta \"" + sel.getNombre() + "\"?")) {
            return;
        }
        try {
            recetaDAO.eliminar(sel.getId());
            limpiarFormulario();
            cargarRecetas();
        } catch (Exception e) {
            // Por ejemplo: la receta está en un plan semanal (el DAO explica el motivo)
            Alertas.error("No se pudo eliminar la receta", e);
        }
    }

    @FXML
    private void handleVerResumenHQL() {
        try {
            List<Object[]> resumen = recetaDAO.obtenerResumenCaloricoRecetas();
            if (resumen.isEmpty()) {
                Alertas.info("Resumen de recetas", "Todavía no hay recetas registradas.");
                return;
            }
            StringBuilder sb = new StringBuilder();
            for (Object[] fila : resumen) {
                sb.append(String.format("• %s [%s]: %.0f kcal (P: %.1fg | C: %.1fg | G: %.1fg)%n",
                        fila[1], fila[2], numero(fila[3]), numero(fila[4]), numero(fila[5]), numero(fila[6])));
            }
            Alertas.info("Resumen calórico por receta", sb.toString());
        } catch (Exception e) {
            Alertas.error("No se pudo calcular el resumen", e);
        }
    }

    @FXML
    private void handleLimpiar() {
        limpiarFormulario();
    }

    @FXML
    private void handleVolverMenu() {
        Navegacion.volver(txtNombreReceta);
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
        cmbAlimento.setValue(null);
        ingredientesActuales.clear();
        tblRecetas.getSelectionModel().clearSelection();
        actualizarTotalesEtiqueta();
    }

    /** Tras un error al guardar, recarga la tabla para que no quede una receta a medio editar. */
    private void recargarSinPerderFormulario() {
        try {
            recetaSeleccionada = null;
            cargarRecetas();
        } catch (Exception ignorada) {
            // El error principal ya se mostró
        }
    }

    private void actualizarTotalesEtiqueta() {
        double kcal = ingredientesActuales.stream().mapToDouble(RecetaIngrediente::getCalorias).sum();
        double prot = ingredientesActuales.stream().mapToDouble(RecetaIngrediente::getProteinas).sum();
        double carb = ingredientesActuales.stream().mapToDouble(RecetaIngrediente::getCarbohidratos).sum();
        double gras = ingredientesActuales.stream().mapToDouble(RecetaIngrediente::getGrasas).sum();
        lblTotalesReceta.setText(String.format(
                "Total receta: %.0f kcal | Proteína: %.1f g | Carbohidratos: %.1f g | Grasas: %.1f g", kcal, prot, carb, gras));
    }

    private static double numero(Object valor) {
        return valor instanceof Number n ? n.doubleValue() : 0;
    }
}

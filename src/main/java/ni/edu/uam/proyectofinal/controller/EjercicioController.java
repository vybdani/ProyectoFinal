package ni.edu.uam.proyectofinal.controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ni.edu.uam.proyectofinal.dao.EjercicioDAO;
import ni.edu.uam.proyectofinal.dao.GrupoMuscularDAO;
import ni.edu.uam.proyectofinal.model.Ejercicio;
import ni.edu.uam.proyectofinal.model.GrupoMuscular;
import ni.edu.uam.proyectofinal.util.Navegacion;

import java.util.List;

/** CRUD del catálogo de ejercicios del modo gimnasio. */
public class EjercicioController {

    private static final String TODOS = "Todos";

    @FXML private TextField txtNombre;
    @FXML private ComboBox<GrupoMuscular> cmbGrupo;
    @FXML private TextField txtEquipo;
    @FXML private TextArea txtDescripcion;
    @FXML private ComboBox<String> cmbFiltroGrupo;
    @FXML private Label lblModo;

    @FXML private TableView<Ejercicio> tblEjercicios;
    @FXML private TableColumn<Ejercicio, String> colNombre;
    @FXML private TableColumn<Ejercicio, String> colGrupo;
    @FXML private TableColumn<Ejercicio, String> colEquipo;

    private final EjercicioDAO ejercicioDAO = new EjercicioDAO();
    private final GrupoMuscularDAO grupoDAO = new GrupoMuscularDAO();

    private final ObservableList<Ejercicio> ejercicios = FXCollections.observableArrayList();
    private List<Ejercicio> todos = List.of();
    private Ejercicio seleccionado;

    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getNombre()));
        colGrupo.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getGrupoMuscular().getNombre()));
        colEquipo.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getEquipo()));
        tblEjercicios.setItems(ejercicios);

        tblEjercicios.getSelectionModel().selectedItemProperty().addListener((obs, anterior, sel) -> {
            if (sel != null) {
                cargarEnFormulario(sel);
            }
        });
        cmbFiltroGrupo.valueProperty().addListener((obs, anterior, nuevo) -> filtrar());

        try {
            grupoDAO.asegurarDatosIniciales();
            List<GrupoMuscular> grupos = grupoDAO.listarTodos();
            cmbGrupo.setItems(FXCollections.observableArrayList(grupos));

            ObservableList<String> filtros = FXCollections.observableArrayList(TODOS);
            grupos.forEach(g -> filtros.add(g.getNombre()));
            cmbFiltroGrupo.setItems(filtros);
            cmbFiltroGrupo.setValue(TODOS);

            cargarEjercicios();
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudieron cargar los ejercicios: " + e.getMessage());
        }
        limpiarFormulario();
    }

    @FXML
    private void handleGuardar() {
        String nombre = txtNombre.getText().trim();
        GrupoMuscular grupo = cmbGrupo.getValue();
        String equipo = txtEquipo.getText().trim();
        String descripcion = txtDescripcion.getText().trim();

        if (nombre.isEmpty() || nombre.length() > 100) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", "El nombre es obligatorio (máximo 100 caracteres).");
            return;
        }
        if (grupo == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", "Selecciona el grupo muscular.");
            return;
        }
        if (equipo.length() > 60 || descripcion.length() > 300) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación",
                    "El equipo admite 60 caracteres y la descripción 300.");
            return;
        }

        try {
            if (seleccionado == null) {
                ejercicioDAO.guardar(new Ejercicio(nombre, equipo, descripcion, grupo));
            } else {
                seleccionado.setNombre(nombre);
                seleccionado.setGrupoMuscular(grupo);
                seleccionado.setEquipo(equipo);
                seleccionado.setDescripcion(descripcion);
                ejercicioDAO.actualizar(seleccionado);
            }
            cargarEjercicios();
            limpiarFormulario();
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void handleEliminar() {
        Ejercicio ejercicio = tblEjercicios.getSelectionModel().getSelectedItem();
        if (ejercicio == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Aviso", "Selecciona un ejercicio de la tabla.");
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar \"" + ejercicio.getNombre() + "\"?", ButtonType.YES, ButtonType.NO);
        confirmacion.setHeaderText(null);
        if (confirmacion.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
            return;
        }
        try {
            ejercicioDAO.eliminar(ejercicio.getId());
            cargarEjercicios();
            limpiarFormulario();
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo eliminar", e.getMessage());
        }
    }

    @FXML
    private void handleNuevo() {
        tblEjercicios.getSelectionModel().clearSelection();
        limpiarFormulario();
    }

    @FXML
    private void handleVolver() {
        Navegacion.ir(tblEjercicios, "rutina-view.fxml", "Rutina de Gimnasio", 900, 640);
    }

    private void cargarEjercicios() {
        todos = ejercicioDAO.listarTodos();
        filtrar();
    }

    private void filtrar() {
        String filtro = cmbFiltroGrupo.getValue();
        ejercicios.setAll(todos.stream()
                .filter(e -> filtro == null || TODOS.equals(filtro) || e.getGrupoMuscular().getNombre().equals(filtro))
                .toList());
    }

    private void cargarEnFormulario(Ejercicio ejercicio) {
        seleccionado = ejercicio;
        txtNombre.setText(ejercicio.getNombre());
        txtEquipo.setText(ejercicio.getEquipo() != null ? ejercicio.getEquipo() : "");
        txtDescripcion.setText(ejercicio.getDescripcion() != null ? ejercicio.getDescripcion() : "");
        cmbGrupo.getItems().stream()
                .filter(g -> g.getId().equals(ejercicio.getGrupoMuscular().getId()))
                .findFirst()
                .ifPresent(cmbGrupo::setValue);
        lblModo.setText("Editando: " + ejercicio.getNombre());
    }

    private void limpiarFormulario() {
        seleccionado = null;
        txtNombre.clear();
        txtEquipo.clear();
        txtDescripcion.clear();
        cmbGrupo.setValue(null);
        lblModo.setText("Nuevo ejercicio");
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

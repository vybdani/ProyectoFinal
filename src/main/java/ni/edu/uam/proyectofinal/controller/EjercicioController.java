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
import ni.edu.uam.proyectofinal.util.Alertas;
import ni.edu.uam.proyectofinal.util.Navegacion;
import ni.edu.uam.proyectofinal.util.Validacion;

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
        try {
            String nombre = Validacion.texto(txtNombre, "el nombre del ejercicio", 100);
            GrupoMuscular grupo = Validacion.seleccion(cmbGrupo, "el grupo muscular");
            String equipo = Validacion.textoOpcional(txtEquipo, "el equipo", 60);
            String descripcion = Validacion.textoOpcional(txtDescripcion, "la descripción", 300);
            boolean repetido = todos.stream().anyMatch(e -> e.getNombre().equalsIgnoreCase(nombre)
                    && (seleccionado == null || !e.getId().equals(seleccionado.getId())));
            if (repetido) {
                throw Validacion.fallo(txtNombre, "Ya existe un ejercicio llamado \"" + nombre + "\".");
            }

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
        } catch (Validacion.ValidacionException e) {
            Alertas.aviso("Revisa el ejercicio", e.getMessage());
        } catch (Exception e) {
            Alertas.error("No se pudo guardar el ejercicio", e);
        }
    }

    @FXML
    private void handleEliminar() {
        Ejercicio ejercicio = tblEjercicios.getSelectionModel().getSelectedItem();
        if (ejercicio == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Aviso", "Selecciona un ejercicio de la tabla.");
            return;
        }
        if (!Alertas.confirmar("Confirmar eliminación", "¿Eliminar \"" + ejercicio.getNombre() + "\"?")) {
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
        Alertas.mostrar(tipo, titulo, mensaje);
    }
}

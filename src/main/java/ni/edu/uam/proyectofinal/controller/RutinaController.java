package ni.edu.uam.proyectofinal.controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ni.edu.uam.proyectofinal.dao.ObjetivoDAO;
import ni.edu.uam.proyectofinal.dao.RutinaDAO;
import ni.edu.uam.proyectofinal.model.Objetivo;
import ni.edu.uam.proyectofinal.model.Rutina;
import ni.edu.uam.proyectofinal.model.RutinaEjercicio;
import ni.edu.uam.proyectofinal.model.Usuario;
import ni.edu.uam.proyectofinal.service.GeneradorRutinaService;
import ni.edu.uam.proyectofinal.util.Alertas;
import ni.edu.uam.proyectofinal.reporte.ReporteService;
import ni.edu.uam.proyectofinal.util.ExportadorPdf;
import ni.edu.uam.proyectofinal.util.Navegacion;
import ni.edu.uam.proyectofinal.util.Validacion;
import ni.edu.uam.proyectofinal.util.SesionActual;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class RutinaController {

    private static final String TODOS = "Todos los días";

    @FXML private Label lblObjetivo;
    @FXML private Label lblInfoRutina;
    @FXML private Label lblResumen;
    @FXML private Label lblDetalle;
    @FXML private ComboBox<Integer> cmbDias;
    @FXML private ComboBox<String> cmbFiltroDia;
    @FXML private TextField txtNombreRutina;

    @FXML private TableView<RutinaEjercicio> tblRutina;
    @FXML private TableColumn<RutinaEjercicio, String> colDia;
    @FXML private TableColumn<RutinaEjercicio, String> colEjercicio;
    @FXML private TableColumn<RutinaEjercicio, String> colGrupo;
    @FXML private TableColumn<RutinaEjercicio, Integer> colSeries;
    @FXML private TableColumn<RutinaEjercicio, String> colRepeticiones;
    @FXML private TableColumn<RutinaEjercicio, String> colDescanso;

    private final GeneradorRutinaService generador = new GeneradorRutinaService();
    private final RutinaDAO rutinaDAO = new RutinaDAO();
    private final ObjetivoDAO objetivoDAO = new ObjetivoDAO();

    private final ObservableList<RutinaEjercicio> filas = FXCollections.observableArrayList();
    private Usuario usuario;
    private Objetivo objetivo;
    private Rutina rutinaActual;

    @FXML
    public void initialize() {
        cmbDias.setItems(FXCollections.observableArrayList(GeneradorRutinaService.DIAS_DISPONIBLES));
        cmbDias.setValue(3);

        colDia.setCellValueFactory(d -> new ReadOnlyStringWrapper(
                "Día " + d.getValue().getDia() + " - " + d.getValue().getNombreDia()));
        colEjercicio.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getEjercicio().getNombre()));
        colGrupo.setCellValueFactory(d -> new ReadOnlyStringWrapper(
                d.getValue().getEjercicio().getGrupoMuscular().getNombre()));
        colSeries.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getSeries()));
        colRepeticiones.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getRepeticiones()));
        colDescanso.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getDescansoSegundos() + " s"));
        tblRutina.setItems(filas);

        tblRutina.getSelectionModel().selectedItemProperty().addListener((obs, anterior, sel) ->
                lblDetalle.setText(sel == null ? "" : sel.getEjercicio().getNombre() + " ("
                        + sel.getEjercicio().getEquipo() + "): " + sel.getEjercicio().getDescripcion()));

        cmbFiltroDia.valueProperty().addListener((obs, anterior, nuevo) -> mostrarFilas());

        usuario = SesionActual.getUsuarioActivo();
        if (usuario == null) {
            lblObjetivo.setText("No hay un usuario activo en sesión.");
            return;
        }

        try {
            objetivo = objetivoDAO.buscarPorUsuarioYModo(usuario.getId(), SesionActual.MODO_GIMNASIO);
            lblObjetivo.setText(objetivo != null
                    ? "Objetivo: " + objetivo.getTipo()
                    : "Primero elige y guarda tu objetivo en el menú del Modo Gimnasio.");
            rutinaActual = rutinaDAO.buscarUltimaPorUsuario(usuario.getId());
            actualizarVista();
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo cargar la rutina: " + e.getMessage());
        }
    }

    @FXML
    private void handleGenerar() {
        if (usuario == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sesión", "No hay un usuario activo en sesión.");
            return;
        }
        if (objetivo == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Objetivo",
                    "Primero elige y guarda tu objetivo de gimnasio en el menú del Modo Gimnasio.");
            return;
        }
        if (cmbDias.getValue() == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", "Selecciona cuántos días puedes entrenar.");
            return;
        }

        try {
            generador.generar(usuario, objetivo.getTipo(), cmbDias.getValue());
            rutinaActual = rutinaDAO.buscarUltimaPorUsuario(usuario.getId());
            actualizarVista();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Rutina generada",
                    "Se generó tu rutina de " + cmbDias.getValue() + " días para " + objetivo.getTipo() + ".");
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void handleRenombrar() {
        if (rutinaActual == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Aviso", "Primero genera una rutina.");
            return;
        }
        String nombre;
        try {
            nombre = Validacion.texto(txtNombreRutina, "el nombre de la rutina", 120);
        } catch (Validacion.ValidacionException e) {
            Alertas.aviso("Revisa el nombre", e.getMessage());
            return;
        }
        try {
            rutinaDAO.actualizarNombre(rutinaActual.getId(), nombre);
            rutinaActual.setNombre(nombre);
            actualizarInfo();
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void handleEliminar() {
        if (rutinaActual == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Aviso", "No hay una rutina para eliminar.");
            return;
        }
        if (!Alertas.confirmar("Confirmar eliminación", "¿Eliminar la rutina \"" + rutinaActual.getNombre() + "\"?")) {
            return;
        }
        try {
            rutinaDAO.eliminar(rutinaActual.getId());
            rutinaActual = rutinaDAO.buscarUltimaPorUsuario(usuario.getId());
            actualizarVista();
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void handleEjercicios() {
        Navegacion.ir(tblRutina, "ejercicio-view.fxml", "Catálogo de Ejercicios", 860, 600);
    }

    /** Exporta la rutina que se está viendo a PDF (JasperReports). */
    @FXML
    private void handleExportarPdf() {
        if (rutinaActual == null) {
            Alertas.aviso("Sin rutina", "Genera una rutina antes de exportarla.");
            return;
        }
        Rutina rutina = rutinaActual;
        ExportadorPdf.exportar(tblRutina, "Rutina", usuario,
                destino -> new ReporteService().exportarRutina(usuario, rutina, destino));
    }

    @FXML
    private void handleVolver() {
        Navegacion.volver(tblRutina);
    }

    private void actualizarVista() {
        if (rutinaActual == null) {
            filas.clear();
            cmbFiltroDia.setItems(FXCollections.observableArrayList(TODOS));
            cmbFiltroDia.setValue(TODOS);
            txtNombreRutina.clear();
            lblInfoRutina.setText("Aún no tienes una rutina. Elige los días y pulsa \"Generar rutina\".");
            lblResumen.setText("");
            return;
        }

        Set<String> dias = new LinkedHashSet<>();
        dias.add(TODOS);
        ordenadas().forEach(re -> dias.add("Día " + re.getDia() + " - " + re.getNombreDia()));
        cmbFiltroDia.setItems(FXCollections.observableArrayList(dias));
        cmbFiltroDia.setValue(TODOS);
        txtNombreRutina.setText(rutinaActual.getNombre());

        actualizarInfo();
        mostrarFilas();
        actualizarResumen();
    }

    private void actualizarInfo() {
        lblInfoRutina.setText(String.format("%s | %d días por semana | creada el %s",
                rutinaActual.getNombre(), rutinaActual.getDiasPorSemana(), rutinaActual.getFechaCreacion()));
    }

    private void mostrarFilas() {
        if (rutinaActual == null) {
            return;
        }
        String filtro = cmbFiltroDia.getValue();
        filas.setAll(ordenadas().stream()
                .filter(re -> filtro == null || TODOS.equals(filtro)
                        || filtro.equals("Día " + re.getDia() + " - " + re.getNombreDia()))
                .toList());
    }

    private List<RutinaEjercicio> ordenadas() {
        return rutinaActual.getEjercicios().stream()
                .sorted(Comparator.comparing(RutinaEjercicio::getDia).thenComparing(RutinaEjercicio::getOrden))
                .toList();
    }

    /** Usa la consulta HQL de resumen por grupo muscular. */
    private void actualizarResumen() {
        try {
            List<Object[]> resumen = rutinaDAO.resumenPorGrupoMuscular(rutinaActual.getId());
            StringBuilder texto = new StringBuilder("Series por semana: ");
            for (int i = 0; i < resumen.size(); i++) {
                Object[] fila = resumen.get(i);
                if (i > 0) {
                    texto.append(" | ");
                }
                texto.append(fila[0]).append(" ").append(fila[2]).append(" (").append(fila[1]).append(" ej.)");
            }
            lblResumen.setText(texto.toString());
        } catch (Exception e) {
            lblResumen.setText("No se pudo calcular el resumen: " + e.getMessage());
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alertas.mostrar(tipo, titulo, mensaje);
    }
}

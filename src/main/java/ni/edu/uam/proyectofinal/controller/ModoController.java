package ni.edu.uam.proyectofinal.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import ni.edu.uam.proyectofinal.dao.ObjetivoDAO;
import ni.edu.uam.proyectofinal.model.Objetivo;
import ni.edu.uam.proyectofinal.model.Usuario;
import ni.edu.uam.proyectofinal.service.CalculoNutricionalService;
import ni.edu.uam.proyectofinal.util.Alertas;
import ni.edu.uam.proyectofinal.util.Navegacion;
import ni.edu.uam.proyectofinal.util.SesionActual;

/**
 * Menú de un modo (nutrición o gimnasio): el usuario elige su objetivo
 * y desde aquí abre el resto de pantallas.
 */
public class ModoController {

    @FXML private Label lblTitulo;
    @FXML private Label lblObjetivoActual;
    @FXML private Label lblDescripcion;
    @FXML private ComboBox<String> cmbObjetivo;

    private final ObjetivoDAO objetivoDAO = new ObjetivoDAO();
    private final CalculoNutricionalService calculoService = new CalculoNutricionalService();

    private Objetivo objetivoActual;

    @FXML
    public void initialize() {
        boolean gimnasio = SesionActual.MODO_GIMNASIO.equals(SesionActual.getModoActual());

        lblTitulo.setText(gimnasio
                ? "Modo Gimnasio: volumen muscular o definición. Cambia de modo en el menú lateral."
                : "Modo Nutrición: bajar, mantener o subir de peso. Cambia de modo en el menú lateral.");
        cmbObjetivo.setItems(FXCollections.observableArrayList(
                gimnasio ? CalculoNutricionalService.OBJETIVOS_GIMNASIO : CalculoNutricionalService.OBJETIVOS_NUTRICION
        ));
        cmbObjetivo.valueProperty().addListener((obs, anterior, nuevo) ->
                lblDescripcion.setText(calculoService.descripcion(nuevo)));

        cargarObjetivo();
    }

    private void cargarObjetivo() {
        Usuario usuario = SesionActual.getUsuarioActivo();
        if (usuario == null) {
            return;
        }
        try {
            objetivoActual = objetivoDAO.buscarPorUsuarioYModo(usuario.getId(), SesionActual.getModoActual());
            if (objetivoActual != null) {
                cmbObjetivo.setValue(objetivoActual.getTipo());
                lblObjetivoActual.setText("Objetivo actual: " + objetivoActual.getTipo());
            } else {
                lblObjetivoActual.setText("Aún no has elegido un objetivo para este modo.");
            }
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo cargar tu objetivo: " + e.getMessage());
        }
    }

    @FXML
    private void handleGuardarObjetivo() {
        Usuario usuario = SesionActual.getUsuarioActivo();
        String tipo = cmbObjetivo.getValue();
        if (usuario == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sesión", "No hay un usuario activo en sesión.");
            return;
        }
        if (tipo == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", "Selecciona un objetivo.");
            return;
        }

        try {
            if (objetivoActual == null) {
                objetivoActual = new Objetivo(SesionActual.getModoActual(), tipo, null, usuario);
            } else {
                objetivoActual.setTipo(tipo);
                objetivoActual.setUsuario(usuario);
            }
            objetivoActual = objetivoDAO.guardar(objetivoActual);
            lblObjetivoActual.setText("Objetivo actual: " + tipo);
            mostrarAlerta(Alert.AlertType.INFORMATION, "Objetivo guardado",
                    "Tu objetivo es: " + tipo + ".\nAhora calcula tu requerimiento diario para actualizar tus calorías.");
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo guardar el objetivo: " + e.getMessage());
        }
    }

    @FXML
    private void handleCalculo() {
        Navegacion.ir(lblTitulo, "calculo-view.fxml", "Cálculo Nutricional", 560, 520);
    }

    @FXML
    private void handleConsumo() {
        Navegacion.ir(lblTitulo, "consumo-view.fxml", "Registro de Consumo", 820, 600);
    }

    @FXML
    private void handleRecetas() {
        Navegacion.ir(lblTitulo, "receta-view.fxml", "Gestión de Recetas", 920, 640);
    }

    @FXML
    private void handlePlanSemanal() {
        Navegacion.ir(lblTitulo, "plan-semanal-view.fxml", "Plan Semanal de Comidas", 880, 600);
    }

    @FXML
    private void handleListaSuper() {
        Navegacion.ir(lblTitulo, "lista-super-view.fxml", "Lista del Súper", 720, 560);
    }

    @FXML
    private void handleRutina() {
        Navegacion.ir(lblTitulo, "rutina-view.fxml", "Rutina de Gimnasio", 900, 640);
    }

    @FXML
    private void handleVolverMenu() {
        SesionActual.setModoActual(null);
        Navegacion.irAlMenuPrincipal(lblTitulo);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alertas.mostrar(tipo, titulo, mensaje);
    }
}

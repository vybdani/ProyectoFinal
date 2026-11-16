package ni.edu.uam.proyectofinal.controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ni.edu.uam.proyectofinal.dao.AlimentoDAO;
import ni.edu.uam.proyectofinal.dao.RegistroConsumoDAO;
import ni.edu.uam.proyectofinal.dao.RequerimientoDiarioDAO;
import ni.edu.uam.proyectofinal.model.Alimento;
import ni.edu.uam.proyectofinal.model.RegistroConsumo;
import ni.edu.uam.proyectofinal.model.RequerimientoDiario;
import ni.edu.uam.proyectofinal.model.Usuario;
import ni.edu.uam.proyectofinal.util.Navegacion;
import ni.edu.uam.proyectofinal.util.SesionActual;

import java.time.LocalDate;

public class ConsumoController {

    @FXML
    private ComboBox<Alimento> cmbAlimento;

    @FXML
    private TextField txtCantidad;

    @FXML
    private ComboBox<String> cmbTipoComida;

    @FXML
    private DatePicker dpFecha;

    @FXML
    private TableView<RegistroConsumo> tblConsumo;

    @FXML
    private TableColumn<RegistroConsumo, String> colAlimento;

    @FXML
    private TableColumn<RegistroConsumo, Double> colCantidad;

    @FXML
    private TableColumn<RegistroConsumo, Double> colCalorias;

    @FXML
    private TableColumn<RegistroConsumo, String> colComida;

    @FXML
    private Label lblResumen;

    private Usuario usuario;

    private final AlimentoDAO alimentoDAO = new AlimentoDAO();

    private final RegistroConsumoDAO consumoDAO = new RegistroConsumoDAO();

    private final RequerimientoDiarioDAO requerimientoDAO = new RequerimientoDiarioDAO();

    private final ObservableList<RegistroConsumo> listaConsumo = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        cmbTipoComida.setItems(
                FXCollections.observableArrayList(
                        "Desayuno",
                        "Almuerzo",
                        "Cena",
                        "Merienda"
                )
        );

        dpFecha.setValue(LocalDate.now());
        dpFecha.valueProperty().addListener((obs, anterior, nueva) -> listarConsumo());

        configurarTabla();

        try {
            cargarAlimentos();
            usuario = SesionActual.getUsuarioActivo();
            listarConsumo();
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudieron cargar los datos: " + e.getMessage());
        }
    }

    private void configurarTabla() {
        colAlimento.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(datos.getValue().getAlimento().getNombre())
        );

        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colCalorias.setCellValueFactory(datos -> new ReadOnlyObjectWrapper<>(
                Math.round(datos.getValue().getCaloriasConsumidas() * 10) / 10.0));
        colComida.setCellValueFactory(new PropertyValueFactory<>("tipoComida"));

        tblConsumo.setItems(listaConsumo);
    }

    private void cargarAlimentos() {
        alimentoDAO.asegurarAlimentosIniciales();
        cmbAlimento.setItems(
                FXCollections.observableArrayList(alimentoDAO.listarTodos())
        );
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        listarConsumo();
    }

    @FXML
    public void registrarConsumo() {
        try {
            if (usuario == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Error", "No hay un usuario activo en sesión.");
                return;
            }

            Alimento alimento = cmbAlimento.getValue();
            if (alimento == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Validación", "Seleccione un alimento.");
                return;
            }

            if (txtCantidad.getText().trim().isEmpty()) {
                mostrarAlerta(Alert.AlertType.WARNING, "Validación", "Ingrese una cantidad en gramos.");
                return;
            }

            double cantidad = Double.parseDouble(txtCantidad.getText().trim());
            if (cantidad <= 0 || cantidad > 3000) {
                mostrarAlerta(Alert.AlertType.WARNING, "Validación", "La cantidad debe estar entre 1 y 3000 gramos.");
                return;
            }

            if (dpFecha.getValue() == null || cmbTipoComida.getValue() == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Validación", "Seleccione la fecha y el tipo de comida.");
                return;
            }

            if (dpFecha.getValue().isAfter(LocalDate.now())) {
                mostrarAlerta(Alert.AlertType.WARNING, "Validación", "No se puede registrar consumo en una fecha futura.");
                return;
            }

            RegistroConsumo registro = new RegistroConsumo(
                    usuario,
                    alimento,
                    cantidad,
                    cmbTipoComida.getValue()
            );

            registro.setFecha(dpFecha.getValue());

            consumoDAO.guardar(registro);

            txtCantidad.clear();
            listarConsumo();

            mostrarAlerta(Alert.AlertType.INFORMATION, "Correcto", "El consumo se registró correctamente.");

        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Ingrese una cantidad numérica válida.");
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    public void eliminarConsumo() {
        RegistroConsumo seleccionado = tblConsumo.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Aviso", "Seleccione un registro de la tabla.");
            return;
        }
        try {
            consumoDAO.eliminar(seleccionado.getId());
            listarConsumo();
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    public void listarConsumo() {
        if (usuario == null || dpFecha.getValue() == null) {
            return;
        }

        try {
            listaConsumo.setAll(
                    consumoDAO.listarConsumoDiario(usuario.getId(), dpFecha.getValue())
            );
            actualizarResumen();
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo cargar el consumo: " + e.getMessage());
        }
    }

    /** Compara lo consumido en el día con el requerimiento calculado. */
    private void actualizarResumen() {
        Object[] totales = consumoDAO.obtenerTotales(usuario.getId(), dpFecha.getValue());
        double calorias = totales != null && totales[0] != null ? ((Number) totales[0]).doubleValue() : 0;
        double proteinas = totales != null && totales[1] != null ? ((Number) totales[1]).doubleValue() : 0;
        double carbohidratos = totales != null && totales[2] != null ? ((Number) totales[2]).doubleValue() : 0;
        double grasas = totales != null && totales[3] != null ? ((Number) totales[3]).doubleValue() : 0;

        RequerimientoDiario req = requerimientoDAO.buscarPorUsuario(usuario.getId());
        if (req == null) {
            lblResumen.setText(String.format(
                    "Consumido: %.0f kcal | P: %.0f g | C: %.0f g | G: %.0f g   (calcula tu requerimiento para ver tu meta)",
                    calorias, proteinas, carbohidratos, grasas));
        } else {
            double restantes = req.getCaloriasDiarias() - calorias;
            lblResumen.setText(String.format(
                    "Consumido: %.0f / %.0f kcal (%s %.0f) | P: %.0f/%.0f g | C: %.0f/%.0f g | G: %.0f/%.0f g",
                    calorias, req.getCaloriasDiarias(),
                    restantes >= 0 ? "faltan" : "te pasaste por", Math.abs(restantes),
                    proteinas, req.getProteinas(),
                    carbohidratos, req.getCarbohidratos(),
                    grasas, req.getGrasas()));
        }
    }

    @FXML
    private void handleVolver() {
        Navegacion.volver(tblConsumo);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}


package ni.edu.uam.proyectofinal.controller;

import ni.edu.uam.proyectofinal.dao.AlimentoDAO;
import ni.edu.uam.proyectofinal.dao.RegistroConsumoDAO;
import ni.edu.uam.proyectofinal.model.Alimento;
import ni.edu.uam.proyectofinal.model.RegistroConsumo;
import ni.edu.uam.nutrifit.entity.Usuario;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;

import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.cell.PropertyValueFactory;

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

    private Usuario usuario;

    private final AlimentoDAO alimentoDAO = new AlimentoDAO();

    private final RegistroConsumoDAO consumoDAO =
            new RegistroConsumoDAO();

    private final ObservableList<RegistroConsumo> listaConsumo =
            FXCollections.observableArrayList();

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

        configurarTabla();
        cargarAlimentos();
    }

    private void configurarTabla() {

        colAlimento.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().getAlimento().getNombre()
                )
        );

        colCantidad.setCellValueFactory(
                new PropertyValueFactory<>("cantidad")
        );

        colCalorias.setCellValueFactory(
                new PropertyValueFactory<>("caloriasConsumidas")
        );

        colComida.setCellValueFactory(
                new PropertyValueFactory<>("tipoComida")
        );

        tblConsumo.setItems(listaConsumo);
    }

    private void cargarAlimentos() {

        cmbAlimento.setItems(
                FXCollections.observableArrayList(
                        alimentoDAO.listarTodos()
                )
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
                mostrarAlerta("Error", "No hay un usuario seleccionado.");
                return;
            }

            Alimento alimento = cmbAlimento.getValue();

            if (alimento == null) {
                mostrarAlerta("Validación", "Seleccione un alimento.");
                return;
            }

            if (txtCantidad.getText().trim().isEmpty()) {
                mostrarAlerta("Validación", "Ingrese una cantidad.");
                return;
            }

            double cantidad = Double.parseDouble(
                    txtCantidad.getText().trim()
            );

            if (cantidad <= 0) {
                mostrarAlerta("Validación", "La cantidad debe ser positiva.");
                return;
            }

            if (dpFecha.getValue() == null ||
                    cmbTipoComida.getValue() == null) {

                mostrarAlerta(
                        "Validación",
                        "Seleccione la fecha y el tipo de comida."
                );

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

            mostrarAlerta(
                    "Correcto",
                    "El consumo se registró correctamente."
            );

        } catch (NumberFormatException e) {

            mostrarAlerta(
                    "Error",
                    "Ingrese una cantidad numérica válida."
            );

        } catch (Exception e) {

            mostrarAlerta("Error", e.getMessage());
        }
    }

    @FXML
    public void listarConsumo() {

        if (usuario == null || dpFecha.getValue() == null) {
            return;
        }

        listaConsumo.setAll(
                consumoDAO.listarConsumoDiario(
                        usuario.getId(),
                        dpFecha.getValue()
                )
        );
    }

    private void mostrarAlerta(String titulo, String mensaje) {

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);

        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }
}

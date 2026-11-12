
package ni.edu.uam.nutrifit.controller;

import ni.edu.uam.nutrifit.dao.ItemListaSuperDAO;
import ni.edu.uam.nutrifit.entity.ItemListaSuper;
import ni.edu.uam.nutrifit.entity.Usuario;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;

import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import javafx.scene.control.cell.PropertyValueFactory;

public class ListaSuperController {

    @FXML
    private TextField txtProducto;

    @FXML
    private TextField txtCantidad;

    @FXML
    private ComboBox<String> cmbUnidad;

    @FXML
    private TableView<ItemListaSuper> tblListaSuper;

    @FXML
    private TableColumn<ItemListaSuper, String> colProducto;

    @FXML
    private TableColumn<ItemListaSuper, Double> colCantidad;

    @FXML
    private TableColumn<ItemListaSuper, String> colUnidad;

    @FXML
    private TableColumn<ItemListaSuper, Boolean> colEstado;

    private Usuario usuario;

    private final ItemListaSuperDAO listaDAO =
            new ItemListaSuperDAO();

    private final ObservableList<ItemListaSuper> listaProductos =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        cmbUnidad.setItems(
                FXCollections.observableArrayList(
                        "Unidad",
                        "Kilogramos",
                        "Gramos",
                        "Litros",
                        "Mililitros",
                        "Paquetes"
                )
        );

        colProducto.setCellValueFactory(
                new PropertyValueFactory<>("nombreProducto")
        );

        colCantidad.setCellValueFactory(
                new PropertyValueFactory<>("cantidad")
        );

        colUnidad.setCellValueFactory(
                new PropertyValueFactory<>("unidad")
        );

        colEstado.setCellValueFactory(
                new PropertyValueFactory<>("comprado")
        );

        tblListaSuper.setItems(listaProductos);
    }

    public void setUsuario(Usuario usuario) {

        this.usuario = usuario;
        listarProductos();
    }

    @FXML
    public void agregarProducto() {

        try {

            if (usuario == null) {
                mostrarAlerta("Error", "No hay un usuario seleccionado.");
                return;
            }

            String nombre = txtProducto.getText().trim();

            if (nombre.isEmpty()) {
                mostrarAlerta("Validación", "Ingrese el nombre del producto.");
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

            if (cmbUnidad.getValue() == null) {
                mostrarAlerta("Validación", "Seleccione una unidad.");
                return;
            }

            ItemListaSuper item = new ItemListaSuper(
                    usuario,
                    nombre,
                    cantidad,
                    cmbUnidad.getValue()
            );

            listaDAO.guardar(item);

            txtProducto.clear();
            txtCantidad.clear();

            listarProductos();

            mostrarAlerta(
                    "Correcto",
                    "Producto agregado correctamente."
            );

        } catch (NumberFormatException e) {

            mostrarAlerta(
                    "Error",
                    "La cantidad debe ser un número válido."
            );

        } catch (Exception e) {

            mostrarAlerta("Error", e.getMessage());
        }
    }

    @FXML
    public void marcarComprado() {

        ItemListaSuper item = tblListaSuper.getSelectionModel()
                .getSelectedItem();

        if (item == null) {
            mostrarAlerta("Aviso", "Seleccione un producto.");
            return;
        }

        item.setComprado(!item.isComprado());

        listaDAO.actualizar(item);

        listarProductos();
    }

    @FXML
    public void eliminarProducto() {

        ItemListaSuper item = tblListaSuper.getSelectionModel()
                .getSelectedItem();

        if (item == null) {
            mostrarAlerta("Aviso", "Seleccione un producto.");
            return;
        }

        listaDAO.eliminar(item);

        listarProductos();

        mostrarAlerta(
                "Correcto",
                "Producto eliminado correctamente."
        );
    }

    private void listarProductos() {

        if (usuario == null) {
            return;
        }

        listaProductos.setAll(
                listaDAO.listarPorUsuario(usuario.getId())
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

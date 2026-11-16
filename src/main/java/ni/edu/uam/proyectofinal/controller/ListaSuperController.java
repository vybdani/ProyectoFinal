package ni.edu.uam.proyectofinal.controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ni.edu.uam.proyectofinal.dao.ItemListaSuperDAO;
import ni.edu.uam.proyectofinal.model.ItemListaSuper;
import ni.edu.uam.proyectofinal.model.Usuario;
import ni.edu.uam.proyectofinal.util.Navegacion;
import ni.edu.uam.proyectofinal.util.SesionActual;

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
    private TableColumn<ItemListaSuper, String> colEstado;

    private Usuario usuario;

    private final ItemListaSuperDAO listaDAO = new ItemListaSuperDAO();

    private final ObservableList<ItemListaSuper> listaProductos = FXCollections.observableArrayList();

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

        colProducto.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colUnidad.setCellValueFactory(new PropertyValueFactory<>("unidad"));
        colEstado.setCellValueFactory(datos -> new ReadOnlyStringWrapper(
                Boolean.TRUE.equals(datos.getValue().getComprado()) ? "✔ Comprado" : "Pendiente"));

        tblListaSuper.setItems(listaProductos);

        usuario = SesionActual.getUsuarioActivo();
        listarProductos();
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        listarProductos();
    }

    @FXML
    public void agregarProducto() {
        try {
            if (usuario == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Error", "No hay un usuario activo en sesión.");
                return;
            }

            String nombre = txtProducto.getText().trim();
            if (nombre.isEmpty()) {
                mostrarAlerta(Alert.AlertType.WARNING, "Validación", "Ingrese el nombre del producto.");
                return;
            }
            if (nombre.length() > 100) {
                mostrarAlerta(Alert.AlertType.WARNING, "Validación", "El nombre no puede tener más de 100 caracteres.");
                return;
            }

            if (txtCantidad.getText().trim().isEmpty()) {
                mostrarAlerta(Alert.AlertType.WARNING, "Validación", "Ingrese una cantidad.");
                return;
            }

            double cantidad = Double.parseDouble(txtCantidad.getText().trim());
            if (cantidad <= 0) {
                mostrarAlerta(Alert.AlertType.WARNING, "Validación", "La cantidad debe ser positiva.");
                return;
            }

            if (cmbUnidad.getValue() == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Validación", "Seleccione una unidad.");
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

        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "La cantidad debe ser un número válido.");
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    public void marcarComprado() {
        ItemListaSuper item = tblListaSuper.getSelectionModel().getSelectedItem();

        if (item == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Aviso", "Seleccione un producto.");
            return;
        }

        try {
            item.setComprado(!Boolean.TRUE.equals(item.getComprado()));
            listaDAO.actualizar(item);
            listarProductos();
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    public void eliminarProducto() {
        ItemListaSuper item = tblListaSuper.getSelectionModel().getSelectedItem();

        if (item == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Aviso", "Seleccione un producto.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar \"" + item.getNombreProducto() + "\" de la lista?", ButtonType.YES, ButtonType.NO);
        confirmacion.setHeaderText(null);
        if (confirmacion.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
            return;
        }

        try {
            listaDAO.eliminar(item.getId());
            listarProductos();
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void handleVolver() {
        Navegacion.volver(tblListaSuper);
    }

    private void listarProductos() {
        if (usuario == null) {
            return;
        }

        try {
            listaProductos.setAll(
                    listaDAO.listarPorUsuario(usuario.getId())
            );
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo cargar la lista: " + e.getMessage());
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}

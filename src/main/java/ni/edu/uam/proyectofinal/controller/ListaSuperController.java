package ni.edu.uam.proyectofinal.controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import ni.edu.uam.proyectofinal.dao.ItemListaSuperDAO;
import ni.edu.uam.proyectofinal.model.ItemListaSuper;
import ni.edu.uam.proyectofinal.model.Usuario;
import ni.edu.uam.proyectofinal.util.Alertas;
import ni.edu.uam.proyectofinal.reporte.ReporteService;
import ni.edu.uam.proyectofinal.util.ExportadorPdf;
import ni.edu.uam.proyectofinal.util.Navegacion;
import ni.edu.uam.proyectofinal.util.SesionActual;
import ni.edu.uam.proyectofinal.util.Validacion;
import ni.edu.uam.proyectofinal.util.Validacion.ValidacionException;

public class ListaSuperController {

    @FXML private TextField txtProducto;
    @FXML private TextField txtCantidad;
    @FXML private ComboBox<String> cmbUnidad;
    @FXML private TableView<ItemListaSuper> tblListaSuper;
    @FXML private TableColumn<ItemListaSuper, String> colProducto;
    @FXML private TableColumn<ItemListaSuper, Double> colCantidad;
    @FXML private TableColumn<ItemListaSuper, String> colUnidad;
    @FXML private TableColumn<ItemListaSuper, String> colEstado;

    private Usuario usuario;
    private final ItemListaSuperDAO listaDAO = new ItemListaSuperDAO();
    private final ObservableList<ItemListaSuper> listaProductos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        cmbUnidad.setItems(FXCollections.observableArrayList(
                "Unidad", "Kilogramos", "Gramos", "Litros", "Mililitros", "Paquetes"));

        colProducto.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colUnidad.setCellValueFactory(new PropertyValueFactory<>("unidad"));
        colEstado.setCellValueFactory(d -> new ReadOnlyStringWrapper(
                Boolean.TRUE.equals(d.getValue().getComprado()) ? "✔ Comprado" : "Pendiente"));
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
        if (usuario == null) {
            Alertas.aviso("Sesión requerida", "Inicia sesión para usar tu lista del súper.");
            return;
        }
        try {
            String nombre = Validacion.texto(txtProducto, "el nombre del producto", 100);
            double cantidad = Validacion.decimal(txtCantidad, "la cantidad", 0.01, 100_000);
            String unidad = Validacion.seleccion(cmbUnidad, "una unidad");
            if (("Unidad".equals(unidad) || "Paquetes".equals(unidad)) && cantidad != Math.rint(cantidad)) {
                throw Validacion.fallo(txtCantidad, "Las unidades y los paquetes se cuentan en números enteros.");
            }

            listaDAO.guardar(new ItemListaSuper(usuario, nombre, cantidad, unidad));
            txtProducto.clear();
            txtCantidad.clear();
            listarProductos();
        } catch (ValidacionException e) {
            Alertas.aviso("Revisa los datos", e.getMessage());
        } catch (Exception e) {
            Alertas.error("No se pudo agregar el producto", e);
        }
    }

    @FXML
    public void marcarComprado() {
        ItemListaSuper item = tblListaSuper.getSelectionModel().getSelectedItem();
        if (item == null) {
            Alertas.aviso("Nada seleccionado", "Selecciona en la tabla el producto que quieres marcar.");
            return;
        }
        try {
            item.setComprado(!Boolean.TRUE.equals(item.getComprado()));
            listaDAO.actualizar(item);
            listarProductos();
        } catch (Exception e) {
            Alertas.error("No se pudo actualizar el producto", e);
            listarProductos();
        }
    }

    @FXML
    public void eliminarProducto() {
        ItemListaSuper item = tblListaSuper.getSelectionModel().getSelectedItem();
        if (item == null) {
            Alertas.aviso("Nada seleccionado", "Selecciona en la tabla el producto que quieres eliminar.");
            return;
        }
        if (!Alertas.confirmar("Eliminar producto", "¿Eliminar \"" + item.getNombreProducto() + "\" de la lista?")) {
            return;
        }
        try {
            listaDAO.eliminar(item.getId());
            listarProductos();
        } catch (Exception e) {
            Alertas.error("No se pudo eliminar el producto", e);
        }
    }

    /** Exporta la lista del súper a PDF (JasperReports). */
    @FXML
    private void handleExportarPdf() {
        if (listaProductos.isEmpty()) {
            Alertas.aviso("Lista vacía", "Agrega productos antes de exportar la lista.");
            return;
        }
        java.util.List<ItemListaSuper> items = java.util.List.copyOf(listaProductos);
        ExportadorPdf.exportar(tblListaSuper, "ListaSuper", usuario,
                destino -> new ReporteService().exportarListaSuper(usuario, items, destino));
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
            listaProductos.setAll(listaDAO.listarPorUsuario(usuario.getId()));
        } catch (Exception e) {
            Alertas.error("No se pudo cargar la lista", e);
        }
    }
}

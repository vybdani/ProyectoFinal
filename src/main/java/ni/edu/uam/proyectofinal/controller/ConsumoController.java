package ni.edu.uam.proyectofinal.controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import ni.edu.uam.proyectofinal.dao.AlimentoDAO;
import ni.edu.uam.proyectofinal.dao.RegistroConsumoDAO;
import ni.edu.uam.proyectofinal.dao.RequerimientoDiarioDAO;
import ni.edu.uam.proyectofinal.model.Alimento;
import ni.edu.uam.proyectofinal.model.RegistroConsumo;
import ni.edu.uam.proyectofinal.model.RequerimientoDiario;
import ni.edu.uam.proyectofinal.model.Usuario;
import ni.edu.uam.proyectofinal.util.Alertas;
import ni.edu.uam.proyectofinal.util.ComboBuscable;
import ni.edu.uam.proyectofinal.util.BuscadorAlimentosDialog;
import ni.edu.uam.proyectofinal.util.Navegacion;
import ni.edu.uam.proyectofinal.util.SesionActual;
import ni.edu.uam.proyectofinal.util.Validacion;
import ni.edu.uam.proyectofinal.util.Validacion.ValidacionException;

import java.time.LocalDate;

public class ConsumoController {

    /** Hasta cuántos días atrás se puede registrar o consultar consumo. */
    private static final int DIAS_HISTORIAL = 365;

    @FXML private ComboBox<Alimento> cmbAlimento;
    @FXML private TextField txtCantidad;
    @FXML private ComboBox<String> cmbTipoComida;
    @FXML private DatePicker dpFecha;
    @FXML private TableView<RegistroConsumo> tblConsumo;
    @FXML private TableColumn<RegistroConsumo, String> colAlimento;
    @FXML private TableColumn<RegistroConsumo, Double> colCantidad;
    @FXML private TableColumn<RegistroConsumo, Double> colCalorias;
    @FXML private TableColumn<RegistroConsumo, String> colComida;
    @FXML private Label lblResumen;

    private Usuario usuario;

    private final AlimentoDAO alimentoDAO = new AlimentoDAO();
    private final RegistroConsumoDAO consumoDAO = new RegistroConsumoDAO();
    private final RequerimientoDiarioDAO requerimientoDAO = new RequerimientoDiarioDAO();

    private final ObservableList<RegistroConsumo> listaConsumo = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Se puede escribir para buscar el alimento (sin importar tildes ni mayúsculas)
        ComboBuscable.activar(cmbAlimento, a -> a.getNombre() + "  ·  " + Math.round(a.getCalorias()) + " kcal / 100 g");
        cmbTipoComida.setItems(FXCollections.observableArrayList("Desayuno", "Almuerzo", "Cena", "Merienda"));
        dpFecha.setValue(LocalDate.now());
        // No se pueden elegir fechas futuras en el calendario
        dpFecha.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate fecha, boolean vacia) {
                super.updateItem(fecha, vacia);
                setDisable(vacia || fecha.isAfter(LocalDate.now())
                        || fecha.isBefore(LocalDate.now().minusDays(DIAS_HISTORIAL)));
            }
        });
        dpFecha.valueProperty().addListener((obs, anterior, nueva) -> listarConsumo());
        configurarTabla();

        usuario = SesionActual.getUsuarioActivo();
        try {
            alimentoDAO.asegurarAlimentosIniciales();
            ComboBuscable.cargar(cmbAlimento, alimentoDAO.listarTodos());
            listarConsumo();
        } catch (Exception e) {
            Alertas.error("No se pudieron cargar los datos", e);
        }
    }

    private void configurarTabla() {
        colAlimento.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getAlimento().getNombre()));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colCalorias.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(
                Math.round(d.getValue().getCaloriasConsumidas() * 10) / 10.0));
        colComida.setCellValueFactory(new PropertyValueFactory<>("tipoComida"));
        tblConsumo.setItems(listaConsumo);
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        listarConsumo();
    }

    @FXML
    public void registrarConsumo() {
        if (usuario == null) {
            Alertas.aviso("Sesión requerida", "Inicia sesión para registrar lo que comes.");
            return;
        }
        try {
            Alimento alimento = Validacion.seleccion(cmbAlimento, "un alimento");
            double cantidad = Validacion.decimal(txtCantidad, "la cantidad en gramos", 1, 3000);
            String tipoComida = Validacion.seleccion(cmbTipoComida, "el tipo de comida");
            LocalDate fecha = Validacion.seleccion(dpFecha, "la fecha");
            if (fecha.isAfter(LocalDate.now())) {
                throw Validacion.fallo(dpFecha, "No se puede registrar consumo en una fecha futura.");
            }
            if (fecha.isBefore(LocalDate.now().minusDays(DIAS_HISTORIAL))) {
                throw Validacion.fallo(dpFecha, "Solo se puede registrar consumo del último año.");
            }

            RegistroConsumo registro = new RegistroConsumo(usuario, alimento, cantidad, tipoComida);
            registro.setFecha(fecha);
            consumoDAO.guardar(registro);

            txtCantidad.clear();
            cmbAlimento.setValue(null);
            listarConsumo();
        } catch (ValidacionException e) {
            Alertas.aviso("Revisa los datos", e.getMessage());
        } catch (Exception e) {
            Alertas.error("No se pudo registrar el consumo", e);
        }
    }

    /** Busca un alimento en internet (API de Solea → Open Food Facts) y lo agrega a la lista. */
    @FXML
    private void handleBuscarEnInternet() {
        BuscadorAlimentosDialog.mostrar(tblConsumo).ifPresent(alimento -> {
            try {
                ComboBuscable.cargar(cmbAlimento, alimentoDAO.listarTodos());
                cmbAlimento.getItems().stream()
                        .filter(a -> a.getId().equals(alimento.getId()))
                        .findFirst()
                        .ifPresent(cmbAlimento::setValue);
                txtCantidad.requestFocus();
            } catch (Exception e) {
                Alertas.error("No se pudieron recargar los alimentos", e);
            }
        });
    }

    @FXML
    public void eliminarConsumo() {
        RegistroConsumo seleccionado = tblConsumo.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            Alertas.aviso("Nada seleccionado", "Selecciona en la tabla el registro que quieres eliminar.");
            return;
        }
        String pregunta = String.format("¿Eliminar %.0f g de %s (%s)?",
                seleccionado.getCantidad(), seleccionado.getAlimento().getNombre(), seleccionado.getTipoComida());
        if (!Alertas.confirmar("Eliminar consumo", pregunta)) {
            return;
        }
        try {
            consumoDAO.eliminar(seleccionado.getId());
            listarConsumo();
        } catch (Exception e) {
            Alertas.error("No se pudo eliminar el consumo", e);
        }
    }

    @FXML
    public void listarConsumo() {
        if (usuario == null || dpFecha.getValue() == null) {
            return;
        }
        try {
            listaConsumo.setAll(consumoDAO.listarConsumoDiario(usuario.getId(), dpFecha.getValue()));
            actualizarResumen();
        } catch (Exception e) {
            Alertas.error("No se pudo cargar el consumo", e);
        }
    }

    /** Compara lo consumido en el día con el requerimiento calculado. */
    private void actualizarResumen() {
        Object[] totales = consumoDAO.obtenerTotales(usuario.getId(), dpFecha.getValue());
        double calorias = numero(totales, 0);
        double proteinas = numero(totales, 1);
        double carbohidratos = numero(totales, 2);
        double grasas = numero(totales, 3);

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

    private static double numero(Object[] fila, int indice) {
        return fila != null && fila[indice] != null ? ((Number) fila[indice]).doubleValue() : 0;
    }

    @FXML
    private void handleVolver() {
        Navegacion.volver(tblConsumo);
    }
}

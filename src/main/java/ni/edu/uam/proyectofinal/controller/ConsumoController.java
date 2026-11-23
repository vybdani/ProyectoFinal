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
import ni.edu.uam.proyectofinal.dao.RecetaDAO;
import ni.edu.uam.proyectofinal.dao.RegistroConsumoDAO;
import ni.edu.uam.proyectofinal.dao.RequerimientoDiarioDAO;
import ni.edu.uam.proyectofinal.model.Alimento;
import ni.edu.uam.proyectofinal.model.Receta;
import ni.edu.uam.proyectofinal.model.RecetaIngrediente;
import ni.edu.uam.proyectofinal.model.RegistroConsumo;
import ni.edu.uam.proyectofinal.model.RequerimientoDiario;
import ni.edu.uam.proyectofinal.model.Usuario;
import ni.edu.uam.proyectofinal.util.Alertas;
import ni.edu.uam.proyectofinal.util.BuscadorAlimentosDialog;
import ni.edu.uam.proyectofinal.util.ComboBuscable;
import ni.edu.uam.proyectofinal.util.Navegacion;
import ni.edu.uam.proyectofinal.util.SesionActual;
import ni.edu.uam.proyectofinal.util.Validacion;
import ni.edu.uam.proyectofinal.util.Validacion.ValidacionException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ConsumoController {

    /** Hasta cuántos días atrás se puede registrar o consultar consumo. */
    private static final int DIAS_HISTORIAL = 365;
    private static final String CATEGORIA_RECETA = "Receta";

    /** Permite elegir tanto un Alimento individual como una Receta completa en el mismo buscador. */
    public static final class OpcionConsumo {
        private final Alimento alimento;
        private final Receta receta;

        private OpcionConsumo(Alimento alimento, Receta receta) {
            this.alimento = alimento;
            this.receta = receta;
        }

        public static OpcionConsumo deAlimento(Alimento alimento) {
            return new OpcionConsumo(alimento, null);
        }

        public static OpcionConsumo deReceta(Receta receta) {
            return new OpcionConsumo(null, receta);
        }

        public boolean esReceta() {
            return receta != null;
        }

        public Alimento getAlimento() {
            return alimento;
        }

        public Receta getReceta() {
            return receta;
        }

        public double getGramosReceta() {
            if (receta == null) {
                return 0.0;
            }
            return receta.getIngredientes().stream()
                    .mapToDouble(i -> i.getCantidadGramos() != null ? i.getCantidadGramos() : 0.0)
                    .sum();
        }

        public String getEtiqueta() {
            if (alimento != null) {
                return alimento.getNombre() + "  ·  " + Math.round(alimento.getCalorias()) + " kcal / 100 g";
            }
            double gramos = getGramosReceta();
            return "Receta: " + receta.getNombre() + "  ·  "
                    + Math.round(receta.getCaloriasTotales()) + " kcal (" + Math.round(gramos) + " g)";
        }
    }

    @FXML private ComboBox<OpcionConsumo> cmbAlimento;
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
    private final RecetaDAO recetaDAO = new RecetaDAO();
    private final RegistroConsumoDAO consumoDAO = new RegistroConsumoDAO();
    private final RequerimientoDiarioDAO requerimientoDAO = new RequerimientoDiarioDAO();

    private final ObservableList<RegistroConsumo> listaConsumo = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        ComboBuscable.activar(cmbAlimento, OpcionConsumo::getEtiqueta);
        cmbTipoComida.setItems(FXCollections.observableArrayList("Desayuno", "Almuerzo", "Cena", "Merienda"));
        dpFecha.setValue(LocalDate.now());

        // Al elegir una receta, sugiere automáticamente su peso total en gramos y su tiempo de comida
        cmbAlimento.valueProperty().addListener((obs, anterior, nueva) -> {
            if (nueva != null && nueva.esReceta()) {
                double gramos = nueva.getGramosReceta();
                if (gramos > 0 && (txtCantidad.getText() == null || txtCantidad.getText().isBlank())) {
                    txtCantidad.setText(String.valueOf(Math.round(gramos)));
                }
                String comidaReceta = nueva.getReceta().getTipoComida();
                if (cmbTipoComida.getValue() == null && comidaReceta != null
                        && cmbTipoComida.getItems().contains(comidaReceta)) {
                    cmbTipoComida.setValue(comidaReceta);
                }
            }
        });

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
            recetaDAO.asegurarRecetasIniciales();
            recargarOpcionesConsumo();
            listarConsumo();
        } catch (Exception e) {
            Alertas.error("No se pudieron cargar los datos", e);
        }
    }

    private void recargarOpcionesConsumo() {
        List<OpcionConsumo> opciones = new ArrayList<>();
        for (Alimento a : alimentoDAO.listarTodos()) {
            if (!CATEGORIA_RECETA.equalsIgnoreCase(a.getCategoria())) {
                opciones.add(OpcionConsumo.deAlimento(a));
            }
        }
        for (Receta r : recetaDAO.listarTodas()) {
            opciones.add(OpcionConsumo.deReceta(r));
        }
        ComboBuscable.cargar(cmbAlimento, opciones);
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
            OpcionConsumo opcion = Validacion.seleccion(cmbAlimento, "un alimento o receta");
            double cantidad = Validacion.decimal(txtCantidad, "la cantidad en gramos", 1, 3000);
            String tipoComida = Validacion.seleccion(cmbTipoComida, "el tipo de comida");
            LocalDate fecha = Validacion.seleccion(dpFecha, "la fecha");
            if (fecha.isAfter(LocalDate.now())) {
                throw Validacion.fallo(dpFecha, "No se puede registrar consumo en una fecha futura.");
            }
            if (fecha.isBefore(LocalDate.now().minusDays(DIAS_HISTORIAL))) {
                throw Validacion.fallo(dpFecha, "Solo se puede registrar consumo del último año.");
            }

            Alimento alimentoRegistrar = opcion.esReceta()
                    ? obtenerOCrearAlimentoDeReceta(opcion.getReceta())
                    : opcion.getAlimento();

            RegistroConsumo registro = new RegistroConsumo(usuario, alimentoRegistrar, cantidad, tipoComida);
            registro.setFecha(fecha);
            consumoDAO.guardar(registro);

            txtCantidad.clear();
            ComboBuscable.seleccionar(cmbAlimento, null);
            listarConsumo();
        } catch (ValidacionException e) {
            Alertas.aviso("Revisa los datos", e.getMessage());
        } catch (Exception e) {
            Alertas.error("No se pudo registrar el consumo", e);
        }
    }

    /** Convierte una Receta a su equivalente nutricional por 100 g para registrarla en el consumo diario. */
    private Alimento obtenerOCrearAlimentoDeReceta(Receta receta) {
        double gramosTotales = receta.getIngredientes().stream()
                .mapToDouble(i -> i.getCantidadGramos() != null ? i.getCantidadGramos() : 0.0)
                .sum();
        if (gramosTotales <= 0) {
            throw Validacion.fallo(cmbAlimento, "La receta seleccionada no tiene ingredientes con peso válido.");
        }

        double factor100 = 100.0 / gramosTotales;
        double kcal100 = receta.getCaloriasTotales() * factor100;
        double prot100 = receta.getProteinasTotales() * factor100;
        double carb100 = receta.getCarbohidratosTotales() * factor100;
        double gras100 = receta.getGrasasTotales() * factor100;

        String nombreBase = "Receta: " + receta.getNombre().trim();
        String nombre = nombreBase.length() > 100 ? nombreBase.substring(0, 100).trim() : nombreBase;

        Alimento existente = alimentoDAO.buscarPorNombreExacto(nombre);
        if (existente != null) {
            existente.setCategoria(CATEGORIA_RECETA);
            existente.setCalorias(kcal100);
            existente.setProteinas(prot100);
            existente.setCarbohidratos(carb100);
            existente.setGrasas(gras100);
            return alimentoDAO.actualizar(existente);
        }
        return alimentoDAO.guardar(new Alimento(nombre, CATEGORIA_RECETA, kcal100, prot100, carb100, gras100));
    }

    /** Busca un alimento en internet (API de Solea → Open Food Facts) y lo selecciona para registrar. */
    @FXML
    private void handleBuscarEnInternet() {
        BuscadorAlimentosDialog.mostrar(tblConsumo).ifPresent(alimento -> {
            try {
                recargarOpcionesConsumo();
                cmbAlimento.getItems().stream()
                        .filter(op -> !op.esReceta() && op.getAlimento().getId().equals(alimento.getId()))
                        .findFirst()
                        .ifPresent(op -> ComboBuscable.seleccionar(cmbAlimento, op));
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
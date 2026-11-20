package ni.edu.uam.proyectofinal.controller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import ni.edu.uam.proyectofinal.HelloApplication;
import ni.edu.uam.proyectofinal.model.PerfilFisico;
import ni.edu.uam.proyectofinal.model.Usuario;
import ni.edu.uam.proyectofinal.util.Alertas;
import ni.edu.uam.proyectofinal.util.Navegacion;
import ni.edu.uam.proyectofinal.util.SesionActual;

import java.util.Map;
import java.util.Set;

/**
 * Ventana principal de Solea: menú lateral fijo a la izquierda y, a la derecha,
 * el área donde se abre cada pantalla. Cambiar de pantalla no abre ventanas nuevas:
 * solo reemplaza el contenido central con una transición corta.
 */
public class ShellController {

    /** Pantallas que solo existen en el modo gimnasio. */
    private static final Set<String> VISTAS_GIMNASIO = Set.of("rutina-view.fxml", "ejercicio-view.fxml");

    /** Interpolación "ease-out" fuerte: arranca rápido y frena suave. */
    private static final Interpolator SALIDA_SUAVE = Interpolator.SPLINE(0.16, 1, 0.3, 1);

    @FXML private BorderPane raiz;
    @FXML private StackPane contenido;
    @FXML private VBox grupoGimnasio;
    @FXML private ToggleButton tgNutricion;
    @FXML private ToggleButton tgGimnasio;
    @FXML private ToggleGroup grupoNavegacion;
    @FXML private ToggleGroup grupoModo;
    @FXML private Label lblUsuario;
    @FXML private Label lblDetalleUsuario;

    @FXML private ToggleButton navInicio;
    @FXML private ToggleButton navPerfil;
    @FXML private ToggleButton navObjetivo;
    @FXML private ToggleButton navRequerimiento;
    @FXML private ToggleButton navRecetas;
    @FXML private ToggleButton navPlan;
    @FXML private ToggleButton navSuper;
    @FXML private ToggleButton navConsumo;
    @FXML private ToggleButton navRutina;
    @FXML private ToggleButton navEjercicios;

    private Map<String, ToggleButton> botonPorVista;
    private String vistaActual;

    @FXML
    public void initialize() {
        botonPorVista = Map.of(
                "menu-principal-view.fxml", navInicio,
                "perfil-fisico-view.fxml", navPerfil,
                "modo-view.fxml", navObjetivo,
                "calculo-view.fxml", navRequerimiento,
                "receta-view.fxml", navRecetas,
                "plan-semanal-view.fxml", navPlan,
                "lista-super-view.fxml", navSuper,
                "consumo-view.fxml", navConsumo,
                "rutina-view.fxml", navRutina,
                "ejercicio-view.fxml", navEjercicios);

        // Un botón del menú siempre queda marcado: no se puede "desmarcar" la pantalla actual
        grupoNavegacion.selectedToggleProperty().addListener((obs, anterior, nuevo) -> {
            if (nuevo == null && anterior != null) {
                anterior.setSelected(true);
            }
        });
        grupoModo.selectedToggleProperty().addListener((obs, anterior, nuevo) -> {
            if (nuevo == null && anterior != null) {
                anterior.setSelected(true);
            }
        });

        if (SesionActual.getModoActual() == null) {
            SesionActual.setModoActual(SesionActual.MODO_NUTRICION);
        }
        boolean gimnasio = esGimnasio();
        tgGimnasio.setSelected(gimnasio);
        tgNutricion.setSelected(!gimnasio);
        aplicarModo();
        cargarUsuario();
    }

    /** Muestra una pantalla en el área central. */
    public void mostrar(String vista, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("views/" + vista));
            Parent pantalla = loader.load();
            vistaActual = vista;
            // Arriba y al centro con ancho máximo: al maximizar no se estira ni se corre a un lado
            if (pantalla instanceof javafx.scene.layout.Region region) {
                region.setMaxWidth(Navegacion.ANCHO_MAXIMO);
                region.setMaxHeight(Double.MAX_VALUE);
            }
            StackPane.setAlignment(pantalla, javafx.geometry.Pos.TOP_CENTER);
            contenido.getChildren().setAll(pantalla);
            animarEntrada(pantalla);

            ToggleButton boton = botonPorVista.get(vista);
            if (boton != null) {
                boton.setSelected(true);
            }
            if (raiz.getScene() != null && raiz.getScene().getWindow() instanceof Stage stage) {
                stage.setTitle("Solea - " + titulo);
            }
            cargarUsuario();
        } catch (Exception e) {
            e.printStackTrace();
            Alertas.error("No se pudo abrir la pantalla",
                    "Ocurrió un problema al abrir " + titulo + ". Si el problema sigue, revisa la conexión con PostgreSQL.");
        }
    }

    private void animarEntrada(Node pantalla) {
        pantalla.setOpacity(0);
        pantalla.setTranslateY(10);
        FadeTransition aparecer = new FadeTransition(Duration.millis(240), pantalla);
        aparecer.setToValue(1);
        aparecer.setInterpolator(SALIDA_SUAVE);
        TranslateTransition subir = new TranslateTransition(Duration.millis(320), pantalla);
        subir.setToY(0);
        subir.setInterpolator(SALIDA_SUAVE);
        new ParallelTransition(aparecer, subir).play();
    }

    // ---------- Menú lateral ----------

    @FXML private void irInicio() { mostrar("menu-principal-view.fxml", "Inicio"); }
    @FXML private void irPerfil() { mostrar("perfil-fisico-view.fxml", "Perfil físico"); }
    @FXML private void irObjetivo() { mostrar("modo-view.fxml", "Objetivo"); }
    @FXML private void irRequerimiento() { mostrar("calculo-view.fxml", "Requerimiento diario"); }
    @FXML private void irRecetas() { mostrar("receta-view.fxml", "Recetas"); }
    @FXML private void irPlan() { mostrar("plan-semanal-view.fxml", "Plan semanal"); }
    @FXML private void irSuper() { mostrar("lista-super-view.fxml", "Lista del súper"); }
    @FXML private void irConsumo() { mostrar("consumo-view.fxml", "Registro de consumo"); }
    @FXML private void irRutina() { mostrar("rutina-view.fxml", "Rutina de gimnasio"); }
    @FXML private void irEjercicios() { mostrar("ejercicio-view.fxml", "Catálogo de ejercicios"); }

    @FXML
    private void handleCerrarSesion() {
        if (Alertas.confirmar("Cerrar sesión", "¿Quieres cerrar tu sesión?")) {
            SesionActual.cerrarSesion();
            Navegacion.irAlLogin(raiz);
        }
    }

    // ---------- Modo ----------

    @FXML
    private void handleModoNutricion() {
        cambiarModo(SesionActual.MODO_NUTRICION);
    }

    @FXML
    private void handleModoGimnasio() {
        cambiarModo(SesionActual.MODO_GIMNASIO);
    }

    private void cambiarModo(String modo) {
        if (modo.equals(SesionActual.getModoActual())) {
            return;
        }
        SesionActual.setModoActual(modo);
        aplicarModo();
        // Las pantallas de gimnasio no existen en nutrición; las demás se recargan con el nuevo modo
        if (vistaActual == null || (!esGimnasio() && VISTAS_GIMNASIO.contains(vistaActual))) {
            irInicio();
        } else {
            ToggleButton boton = botonPorVista.get(vistaActual);
            mostrar(vistaActual, boton != null ? boton.getText() : "Inicio");
        }
    }

    private void aplicarModo() {
        boolean gimnasio = esGimnasio();
        raiz.getStyleClass().removeAll("modo-nutricion", "modo-gimnasio");
        raiz.getStyleClass().add(gimnasio ? "modo-gimnasio" : "modo-nutricion");
        grupoGimnasio.setVisible(gimnasio);
        grupoGimnasio.setManaged(gimnasio);
    }

    private boolean esGimnasio() {
        return SesionActual.MODO_GIMNASIO.equals(SesionActual.getModoActual());
    }

    private void cargarUsuario() {
        Usuario usuario = SesionActual.getUsuarioActivo();
        if (usuario == null) {
            lblUsuario.setText("Sin sesión");
            lblDetalleUsuario.setText("");
            return;
        }
        lblUsuario.setText(usuario.getNombre());
        String modo = esGimnasio() ? "Modo Gimnasio" : "Modo Nutrición";
        try {
            PerfilFisico perfil = usuario.getPerfilFisico();
            lblDetalleUsuario.setText(perfil != null && perfil.getPeso() != null
                    ? String.format("%.1f kg · %s", perfil.getPeso(), modo)
                    : modo);
        } catch (RuntimeException e) {
            // El perfil no vino cargado con el usuario (relación LAZY): se muestra solo el modo
            lblDetalleUsuario.setText(modo);
        }
    }
}

package ni.edu.uam.proyectofinal.util;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.JasperPrint;
import ni.edu.uam.proyectofinal.model.Usuario;
import ni.edu.uam.proyectofinal.reporte.FormatoReporte;
import ni.edu.uam.proyectofinal.reporte.ReporteService;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Ventana de vista previa de un reporte: muestra las páginas tal como saldrán en el PDF y
 * permite exportarlo a PDF o Excel desde ahí, sin volver a consultar la base de datos.
 */
public final class VistaPreviaReporte {

    /** Las páginas se dibujan al doble de resolución para que se vean nítidas con zoom. */
    private static final float NITIDEZ = 2f;
    private static final double[] ZOOMS = {0.5, 0.75, 1.0, 1.25, 1.5, 2.0};
    private static final int ZOOM_INICIAL = 2; // 100 %
    /** Ancho para que la barra (título, zoom y los tres botones) quepa completa. */
    private static final double ANCHO_MINIMO = 980;

    private final Stage ventana = new Stage();
    private final List<ImageView> paginas = new ArrayList<>();
    private final Label lblZoom = new Label();
    private int zoom = ZOOM_INICIAL;

    private VistaPreviaReporte() {
    }

    /**
     * Prepara el reporte en segundo plano y abre la vista previa.
     *
     * @param origen   cualquier nodo de la pantalla (para ubicar la ventana)
     * @param titulo   título visible, por ejemplo "Rutina de gimnasio"
     * @param tipo     tipo de reporte para el nombre de archivo sugerido, por ejemplo "Rutina"
     * @param usuario  usuario en sesión
     * @param preparar llena el reporte (normalmente un método prepararXxx de ReporteService)
     */
    public static void mostrar(Node origen, String titulo, String tipo, Usuario usuario,
                               Supplier<JasperPrint> preparar) {
        if (usuario == null) {
            Alertas.aviso("Sesión requerida", "Inicia sesión para ver los reportes.");
            return;
        }

        Task<Pagina[]> tarea = new Task<>() {
            private JasperPrint impresion;

            @Override
            protected Pagina[] call() throws Exception {
                impresion = preparar.get();
                int total = impresion.getPages().size();
                Pagina[] imagenes = new Pagina[total];
                for (int i = 0; i < total; i++) {
                    imagenes[i] = new Pagina(impresion,
                            ReporteUtil.renderizarPagina(impresion, i, NITIDEZ));
                }
                return imagenes;
            }
        };
        origen.getScene().setCursor(Cursor.WAIT);
        origen.setDisable(true);

        tarea.setOnSucceeded(e -> {
            terminar(origen);
            Pagina[] imagenes = tarea.getValue();
            if (imagenes.length == 0) {
                Alertas.aviso("Reporte vacío", "El reporte no tiene páginas para mostrar.");
                return;
            }
            new VistaPreviaReporte().abrir(origen, titulo, tipo, usuario, imagenes);
        });
        tarea.setOnFailed(e -> {
            terminar(origen);
            Alertas.error("No se pudo preparar la vista previa", tarea.getException());
        });

        Thread hilo = new Thread(tarea, "vista-previa-reporte");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void abrir(Node origen, String titulo, String tipo, Usuario usuario, Pagina[] imagenes) {
        JasperPrint impresion = imagenes[0].impresion();

        // ---- Barra superior: título, zoom y exportación
        Label lblTitulo = new Label(titulo);
        lblTitulo.getStyleClass().add("seccion");
        Label lblDetalle = new Label("Vista previa · " + imagenes.length
                + (imagenes.length == 1 ? " página" : " páginas"));
        lblDetalle.getStyleClass().add("nota");
        VBox encabezado = new VBox(2, lblTitulo, lblDetalle);

        Button btnMenos = new Button("−");
        Button btnMas = new Button("+");
        btnMenos.setTooltip(new Tooltip("Alejar (Ctrl + -)"));
        btnMas.setTooltip(new Tooltip("Acercar (Ctrl + +)"));
        btnMenos.setOnAction(e -> cambiarZoom(-1));
        btnMas.setOnAction(e -> cambiarZoom(1));
        lblZoom.getStyleClass().add("vista-previa-zoom");
        HBox controlesZoom = new HBox(4, btnMenos, lblZoom, btnMas);
        controlesZoom.setAlignment(Pos.CENTER);

        Button btnPdf = new Button("Exportar PDF");
        btnPdf.getStyleClass().add("primario");
        Button btnExcel = new Button("Exportar Excel");
        Button btnCerrar = new Button("Cerrar");
        btnCerrar.getStyleClass().add("fantasma");
        btnCerrar.setCancelButton(true);
        btnCerrar.setOnAction(e -> ventana.close());

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        for (Button boton : List.of(btnMenos, btnMas, btnPdf, btnExcel, btnCerrar)) {
            boton.setMinWidth(Region.USE_PREF_SIZE); // que los textos nunca se corten con "..."
        }
        HBox acciones = new HBox(10, btnPdf, btnExcel, btnCerrar);
        acciones.setAlignment(Pos.CENTER_RIGHT);
        HBox barra = new HBox(18, encabezado, espacio, controlesZoom, acciones);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.getStyleClass().add("vista-previa-barra");

        btnPdf.setOnAction(e -> exportar(acciones, tipo, usuario, FormatoReporte.PDF, impresion));
        btnExcel.setOnAction(e -> exportar(acciones, tipo, usuario, FormatoReporte.EXCEL, impresion));

        // ---- Páginas, una debajo de otra, como hojas sobre la mesa
        VBox hojas = new VBox(24);
        hojas.setAlignment(Pos.TOP_CENTER);
        hojas.setPadding(new Insets(28));
        DropShadow sombra = new DropShadow(14, 0, 3, Color.rgb(30, 31, 27, 0.18));
        for (Pagina pagina : imagenes) {
            ImageView vista = new ImageView(aImagenFx(pagina.imagen()));
            vista.setPreserveRatio(true);
            vista.setSmooth(true);
            vista.setEffect(sombra);
            paginas.add(vista);
            hojas.getChildren().add(vista);
        }
        ScrollPane desplazamiento = new ScrollPane(hojas);
        desplazamiento.setFitToWidth(true);
        desplazamiento.setPannable(true);
        desplazamiento.getStyleClass().add("vista-previa-hojas");

        BorderPane raiz = new BorderPane(desplazamiento);
        raiz.setTop(barra);
        raiz.getStyleClass().add(
                SesionActual.MODO_GIMNASIO.equals(SesionActual.getModoActual()) ? "modo-gimnasio" : "modo-nutricion");

        // ---- Ventana del tamaño de una hoja, sin pasarse de la pantalla
        Rectangle2D pantalla = Screen.getPrimary().getVisualBounds();
        double ancho = Math.min(impresion.getPageWidth() * ZOOMS[ZOOM_INICIAL] + 160, pantalla.getWidth() - 40);
        double alto = Math.min(impresion.getPageHeight() * ZOOMS[ZOOM_INICIAL] + 140, pantalla.getHeight() - 40);
        Scene escena = new Scene(raiz, Math.max(ancho, ANCHO_MINIMO), alto);
        var hoja = VistaPreviaReporte.class.getResource("/ni/edu/uam/proyectofinal/views/app.css");
        if (hoja != null) {
            escena.getStylesheets().add(hoja.toExternalForm());
        }
        escena.getAccelerators().put(new KeyCodeCombination(KeyCode.PLUS, KeyCombination.SHORTCUT_DOWN), () -> cambiarZoom(1));
        escena.getAccelerators().put(new KeyCodeCombination(KeyCode.ADD, KeyCombination.SHORTCUT_DOWN), () -> cambiarZoom(1));
        escena.getAccelerators().put(new KeyCodeCombination(KeyCode.EQUALS, KeyCombination.SHORTCUT_DOWN), () -> cambiarZoom(1));
        escena.getAccelerators().put(new KeyCodeCombination(KeyCode.MINUS, KeyCombination.SHORTCUT_DOWN), () -> cambiarZoom(-1));
        escena.getAccelerators().put(new KeyCodeCombination(KeyCode.SUBTRACT, KeyCombination.SHORTCUT_DOWN), () -> cambiarZoom(-1));

        ventana.setTitle("Solea - Vista previa: " + titulo);
        ventana.setScene(escena);
        ventana.setMinWidth(Math.min(ANCHO_MINIMO, pantalla.getWidth()));
        ventana.setMinHeight(420);
        if (origen.getScene() != null && origen.getScene().getWindow() != null) {
            ventana.initOwner(origen.getScene().getWindow());
            ventana.initModality(Modality.WINDOW_MODAL);
            if (origen.getScene().getWindow() instanceof Stage principal) {
                ventana.getIcons().setAll(principal.getIcons());
            }
        }
        aplicarZoom();
        ventana.show();
    }

    private void exportar(Node acciones, String tipo, Usuario usuario, FormatoReporte formato, JasperPrint impresion) {
        ExportadorReporte.exportar(acciones, tipo, usuario, formato,
                destino -> new ReporteService().exportar(impresion, destino));
    }

    private void cambiarZoom(int paso) {
        zoom = Math.max(0, Math.min(ZOOMS.length - 1, zoom + paso));
        aplicarZoom();
    }

    private void aplicarZoom() {
        double factor = ZOOMS[zoom];
        for (ImageView pagina : paginas) {
            pagina.setFitWidth(pagina.getImage().getWidth() / NITIDEZ * factor);
        }
        lblZoom.setText(Math.round(factor * 100) + " %");
    }

    /** Convierte la imagen de AWT a JavaFX sin depender del módulo javafx.swing. */
    private static WritableImage aImagenFx(BufferedImage imagen) {
        int ancho = imagen.getWidth();
        int alto = imagen.getHeight();
        int[] pixeles = imagen.getRGB(0, 0, ancho, alto, null, 0, ancho);
        WritableImage fx = new WritableImage(ancho, alto);
        fx.getPixelWriter().setPixels(0, 0, ancho, alto, PixelFormat.getIntArgbInstance(), pixeles, 0, ancho);
        return fx;
    }

    private static void terminar(Node origen) {
        origen.setDisable(false);
        if (origen.getScene() != null) {
            origen.getScene().setCursor(Cursor.DEFAULT);
        }
    }

    /** Una página ya dibujada junto con el reporte al que pertenece. */
    private record Pagina(JasperPrint impresion, BufferedImage imagen) {
    }
}

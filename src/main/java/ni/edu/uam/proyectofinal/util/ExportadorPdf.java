package ni.edu.uam.proyectofinal.util;

import javafx.concurrent.Task;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import ni.edu.uam.proyectofinal.model.Usuario;
import ni.edu.uam.proyectofinal.reporte.ReporteService;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Function;

/**
 * Flujo común de "Exportar PDF" para las pantallas:
 * elegir dónde guardar → generar el PDF en segundo plano (la ventana no se congela) → ofrecer abrirlo.
 */
public final class ExportadorPdf {

    private ExportadorPdf() {
    }

    /**
     * @param origen  cualquier nodo de la pantalla (para ubicar la ventana)
     * @param tipo    tipo de reporte para el nombre sugerido, por ejemplo "Rutina"
     * @param usuario usuario en sesión
     * @param generar recibe la ruta elegida y genera el PDF (normalmente un método de ReporteService)
     */
    public static void exportar(Node origen, String tipo, Usuario usuario, Function<Path, Path> generar) {
        if (usuario == null) {
            Alertas.aviso("Sesión requerida", "Inicia sesión para exportar reportes.");
            return;
        }
        FileChooser selector = new FileChooser();
        selector.setTitle("Guardar " + tipo.toLowerCase() + " en PDF");
        selector.setInitialFileName(ReporteService.nombreSugerido(tipo, usuario));
        selector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documento PDF", "*.pdf"));
        File documentos = new File(System.getProperty("user.home"), "Documents");
        selector.setInitialDirectory(documentos.isDirectory() ? documentos : new File(System.getProperty("user.home")));

        File elegido = selector.showSaveDialog(origen.getScene().getWindow());
        if (elegido == null) {
            return; // El usuario canceló
        }

        Task<Path> tarea = new Task<>() {
            @Override
            protected Path call() {
                return generar.apply(elegido.toPath());
            }
        };
        origen.getScene().setCursor(Cursor.WAIT);
        origen.setDisable(true);

        tarea.setOnSucceeded(evento -> {
            terminar(origen);
            Path pdf = tarea.getValue();
            if (Alertas.confirmar("PDF listo", "Se guardó " + pdf.getFileName() + ".\n¿Quieres abrirlo ahora?")) {
                Thread abrir = new Thread(() -> ReporteUtil.abrirPdfConLectorDelSistema(pdf), "abrir-pdf");
                abrir.setDaemon(true);
                abrir.start();
            }
        });
        tarea.setOnFailed(evento -> {
            terminar(origen);
            Alertas.error("No se pudo exportar el PDF", tarea.getException());
        });

        Thread hilo = new Thread(tarea, "reporte-pdf");
        hilo.setDaemon(true);
        hilo.start();
    }

    private static void terminar(Node origen) {
        origen.setDisable(false);
        if (origen.getScene() != null) {
            origen.getScene().setCursor(Cursor.DEFAULT);
        }
    }
}

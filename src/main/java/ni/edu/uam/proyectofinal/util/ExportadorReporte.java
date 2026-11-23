package ni.edu.uam.proyectofinal.util;

import javafx.concurrent.Task;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import ni.edu.uam.proyectofinal.model.Usuario;
import ni.edu.uam.proyectofinal.reporte.FormatoReporte;
import ni.edu.uam.proyectofinal.reporte.ReporteService;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Function;

/**
 * Flujo común de "Exportar PDF" / "Exportar Excel" para las pantallas:
 * elegir dónde guardar → generar el archivo en segundo plano (la ventana no se congela) → ofrecer abrirlo.
 */
public final class ExportadorReporte {

    private ExportadorReporte() {
    }

    /**
     * @param origen  cualquier nodo de la pantalla (para ubicar la ventana)
     * @param tipo    tipo de reporte para el nombre sugerido, por ejemplo "Rutina"
     * @param usuario usuario en sesión
     * @param formato PDF o EXCEL
     * @param generar recibe la ruta elegida (con la extensión del formato) y genera el archivo
     *                (normalmente un método de ReporteService)
     */
    public static void exportar(Node origen, String tipo, Usuario usuario, FormatoReporte formato,
                                Function<Path, Path> generar) {
        if (usuario == null) {
            Alertas.aviso("Sesión requerida", "Inicia sesión para exportar reportes.");
            return;
        }
        FileChooser selector = new FileChooser();
        selector.setTitle("Guardar " + tipo.toLowerCase() + " en " + formato.getNombre());
        selector.setInitialFileName(ReporteService.nombreSugerido(tipo, usuario, formato));
        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(formato.getDescripcion(), "*." + formato.getExtension()));
        File documentos = new File(System.getProperty("user.home"), "Documents");
        selector.setInitialDirectory(documentos.isDirectory() ? documentos : new File(System.getProperty("user.home")));

        File elegido = selector.showSaveDialog(origen.getScene().getWindow());
        if (elegido == null) {
            return; // El usuario canceló
        }
        Path destino = formato.conExtension(elegido.toPath());

        Task<Path> tarea = new Task<>() {
            @Override
            protected Path call() {
                return generar.apply(destino);
            }
        };
        origen.getScene().setCursor(Cursor.WAIT);
        origen.setDisable(true);

        tarea.setOnSucceeded(evento -> {
            terminar(origen);
            Path archivo = tarea.getValue();
            if (Alertas.confirmar(formato.getNombre() + " listo",
                    "Se guardó " + archivo.getFileName() + ".\n¿Quieres abrirlo ahora?")) {
                Thread abrir = new Thread(() -> ReporteUtil.abrirConProgramaDelSistema(archivo), "abrir-reporte");
                abrir.setDaemon(true);
                abrir.start();
            }
        });
        tarea.setOnFailed(evento -> {
            terminar(origen);
            Alertas.error("No se pudo exportar a " + formato.getNombre(), tarea.getException());
        });

        Thread hilo = new Thread(tarea, "reporte-" + formato.getExtension());
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

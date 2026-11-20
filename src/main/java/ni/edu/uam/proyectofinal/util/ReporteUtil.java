package ni.edu.uam.proyectofinal.util;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Operaciones de bajo nivel con JasperReports: compilar plantillas .jrxml, llenarlas con datos
 * y exportarlas a PDF. La lógica de qué datos lleva cada reporte está en ReporteService.
 */
public final class ReporteUtil {

    private static final String RUTA_REPORTES = "/ni/edu/uam/proyectofinal/reports/";

    /** Compilar una plantilla tarda segundos: se compila una sola vez y se reutiliza. */
    private static final Map<String, JasperReport> COMPILADOS = new ConcurrentHashMap<>();

    private ReporteUtil() {
    }

    /**
     * Compila (o toma de la caché) y llena un reporte.
     *
     * @param nombreArchivoJrxml plantilla dentro de /reports, por ejemplo "ReporteListaSuper.jrxml"
     * @param parametros         valores de los $P{...} de la plantilla (puede ser null)
     * @param datosColeccion     filas de la tabla como JavaBeans con getters (puede ser null)
     */
    public static JasperPrint compilarYLlenar(String nombreArchivoJrxml, Map<String, Object> parametros,
                                              List<?> datosColeccion) throws JRException {
        JasperReport reporte = compilar(nombreArchivoJrxml);
        JRDataSource datos = datosColeccion != null && !datosColeccion.isEmpty()
                ? new JRBeanCollectionDataSource(datosColeccion)
                : new JREmptyDataSource();
        Map<String, Object> params = parametros != null ? new HashMap<>(parametros) : new HashMap<>();
        return JasperFillManager.fillReport(reporte, params, datos);
    }

    /** Compila la plantilla la primera vez que se pide y la guarda en caché. */
    public static JasperReport compilar(String nombreArchivoJrxml) throws JRException {
        JasperReport enCache = COMPILADOS.get(nombreArchivoJrxml);
        if (enCache != null) {
            return enCache;
        }
        String ruta = RUTA_REPORTES + nombreArchivoJrxml;
        try (InputStream plantilla = ReporteUtil.class.getResourceAsStream(ruta)) {
            if (plantilla == null) {
                throw new JRException("No se encontró la plantilla de reporte: " + ruta);
            }
            JasperReport compilado = JasperCompileManager.compileReport(plantilla);
            COMPILADOS.put(nombreArchivoJrxml, compilado);
            return compilado;
        } catch (IOException e) {
            throw new JRException("No se pudo leer la plantilla " + ruta, e);
        }
    }

    /** Exporta el reporte a PDF, creando la carpeta de destino si no existe. */
    public static Path exportarPdf(JasperPrint print, Path destino) throws JRException {
        try {
            Path carpeta = destino.toAbsolutePath().getParent();
            if (carpeta != null) {
                Files.createDirectories(carpeta);
            }
        } catch (IOException e) {
            throw new JRException("No se pudo crear la carpeta " + destino.getParent(), e);
        }
        JasperExportManager.exportReportToPdfFile(print, destino.toString());
        return destino;
    }

    /** Compatibilidad con el código anterior que recibía la ruta como texto. */
    public static void exportarPdf(JasperPrint print, String rutaDestino) throws JRException {
        exportarPdf(print, Path.of(rutaDestino));
    }

    /** Abre el visor interactivo de JasperReports en una ventana aparte. */
    public static void visualizar(JasperPrint print) {
        JasperViewer viewer = new JasperViewer(print, false);
        viewer.setTitle("Solea - Visualizador de Reporte");
        viewer.setVisible(true);
    }

    /** Abre el PDF con el lector predeterminado del sistema. Devuelve false si no se pudo. */
    public static boolean abrirPdfConLectorDelSistema(Path pdf) {
        try {
            File archivo = pdf.toFile();
            if (archivo.exists() && Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(archivo);
                return true;
            }
        } catch (IOException | UnsupportedOperationException e) {
            System.err.println("No se pudo abrir el PDF automáticamente: " + e.getMessage());
        }
        return false;
    }

    public static void abrirPdfConLectorDelSistema(String rutaPdf) {
        abrirPdfConLectorDelSistema(Path.of(rutaPdf));
    }
}

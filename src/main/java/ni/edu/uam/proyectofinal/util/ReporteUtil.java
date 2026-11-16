package ni.edu.uam.proyectofinal.util;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;

import java.awt.Desktop;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Clase utilitaria para facilitar a Daniella la generación y exportación
 * de reportes con JasperReports en la aplicación NutriFit.
 */
public class ReporteUtil {

    private static final String RUTA_REPORTES = "/ni/edu/uam/proyectofinal/reports/";

    private ReporteUtil() {}

    /**
     * Compila y llena un reporte JasperReports a partir de una plantilla .jrxml.
     *
     * @param nombreArchivoJrxml Nombre del archivo .jrxml (ej. "ReporteListaSuper.jrxml").
     * @param parametros         Mapa de parámetros (puede ser null).
     * @param datosColeccion     Lista de objetos Java (JavaBeans) para alimentar la tabla (puede ser null).
     * @return JasperPrint listo para visualizar o exportar a PDF.
     */
    public static JasperPrint compilarYLlenar(String nombreArchivoJrxml, Map<String, Object> parametros, List<?> datosColeccion) throws JRException {
        String rutaCompleta = RUTA_REPORTES + nombreArchivoJrxml;
        InputStream reportStream = ReporteUtil.class.getResourceAsStream(rutaCompleta);

        if (reportStream == null) {
            throw new JRException("No se encontró la plantilla de reporte en: " + rutaCompleta);
        }

        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

        JRDataSource dataSource;
        if (datosColeccion != null && !datosColeccion.isEmpty()) {
            dataSource = new JRBeanCollectionDataSource(datosColeccion);
        } else {
            dataSource = new JREmptyDataSource();
        }

        Map<String, Object> params = (parametros != null) ? parametros : new HashMap<>();
        return JasperFillManager.fillReport(jasperReport, params, dataSource);
    }

    /**
     * Exporta el reporte generado a un archivo PDF en el disco.
     *
     * @param print       Objeto JasperPrint generado.
     * @param rutaDestino Ruta donde se guardará el PDF (ej. "reporte_super.pdf").
     */
    public static void exportarPdf(JasperPrint print, String rutaDestino) throws JRException {
        JasperExportManager.exportReportToPdfFile(print, rutaDestino);
        System.out.println("Reporte PDF exportado con éxito a: " + rutaDestino);
    }

    /**
     * Abre el visor interactivo de JasperReports en una ventana independiente.
     */
    public static void visualizar(JasperPrint print) {
        JasperViewer viewer = new JasperViewer(print, false);
        viewer.setTitle("NutriFit - Visualizador de Reporte");
        viewer.setVisible(true);
    }

    /**
     * Abre automáticamente el archivo PDF con el lector predeterminado de Windows/OS.
     */
    public static void abrirPdfConLectorDelSistema(String rutaPdf) {
        try {
            File pdfFile = new File(rutaPdf);
            if (pdfFile.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(pdfFile);
            }
        } catch (Exception e) {
            System.err.println("No se pudo abrir el PDF automáticamente: " + e.getMessage());
        }
    }
}

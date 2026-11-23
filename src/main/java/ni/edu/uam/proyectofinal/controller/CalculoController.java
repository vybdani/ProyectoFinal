package ni.edu.uam.proyectofinal.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import ni.edu.uam.proyectofinal.dao.ObjetivoDAO;
import ni.edu.uam.proyectofinal.dao.RequerimientoDiarioDAO;
import ni.edu.uam.proyectofinal.model.Objetivo;
import ni.edu.uam.proyectofinal.model.RequerimientoDiario;
import ni.edu.uam.proyectofinal.model.Usuario;
import ni.edu.uam.proyectofinal.service.CalculoNutricionalService;
import ni.edu.uam.proyectofinal.util.Alertas;
import ni.edu.uam.proyectofinal.reporte.FormatoReporte;
import ni.edu.uam.proyectofinal.reporte.ReporteService;
import ni.edu.uam.proyectofinal.util.ExportadorReporte;
import ni.edu.uam.proyectofinal.util.VistaPreviaReporte;
import ni.edu.uam.proyectofinal.util.Navegacion;
import ni.edu.uam.proyectofinal.util.SesionActual;

public class CalculoController {

    @FXML private Label lblObjetivo;
    @FXML private Label lblTMB;
    @FXML private Label lblCalorias;
    @FXML private Label lblProteinas;
    @FXML private Label lblCarbohidratos;
    @FXML private Label lblGrasas;

    private Usuario usuario;

    private final CalculoNutricionalService servicio = new CalculoNutricionalService();
    private final RequerimientoDiarioDAO requerimientoDAO = new RequerimientoDiarioDAO();
    private final ObjetivoDAO objetivoDAO = new ObjetivoDAO();

    @FXML
    public void initialize() {
        usuario = SesionActual.getUsuarioActivo();
        if (usuario == null) {
            lblObjetivo.setText("Inicia sesión para calcular tu requerimiento.");
            return;
        }
        try {
            Objetivo objetivo = buscarObjetivo();
            lblObjetivo.setText(objetivo != null
                    ? "Objetivo: " + objetivo.getTipo()
                    : "Sin objetivo elegido: se calculará para mantener el peso.");

            // Muestra el último cálculo guardado, si existe
            RequerimientoDiario ultimo = requerimientoDAO.buscarPorUsuario(usuario.getId());
            if (ultimo != null) {
                mostrarResultado(ultimo);
            }
        } catch (Exception e) {
            Alertas.error("No se pudo cargar el cálculo", e);
        }
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    @FXML
    public void calcular() {
        if (usuario == null) {
            Alertas.aviso("Sesión requerida", "Inicia sesión para calcular tu requerimiento.");
            return;
        }
        if (usuario.getPerfilFisico() == null) {
            Alertas.aviso("Perfil incompleto", "Completa tu perfil físico antes de calcular.");
            Navegacion.irAlPerfil(lblTMB);
            return;
        }
        try {
            Objetivo objetivo = buscarObjetivo();
            RequerimientoDiario resultado = servicio.calcular(usuario, objetivo);
            requerimientoDAO.guardar(resultado);
            if (objetivo != null) {
                objetivo.setCaloriasDiarias((int) Math.round(resultado.getCaloriasDiarias()));
                objetivo.setUsuario(usuario);
                objetivoDAO.guardar(objetivo);
            }
            mostrarResultado(resultado);
            Alertas.info("Cálculo guardado", String.format(
                    "Tu meta diaria es de %.0f kcal. Se guardó para tu plan y tu registro de consumo.",
                    resultado.getCaloriasDiarias()));
        } catch (Exception e) {
            Alertas.error("No se pudo calcular", e);
        }
    }

    /** Muestra la ficha nutricional como quedará en el reporte, con opción de exportarla. */
    @FXML
    private void handleVistaPrevia() {
        String modo = SesionActual.getModoActual();
        VistaPreviaReporte.mostrar(lblTMB, "Ficha nutricional", "FichaNutricional", usuario,
                () -> new ReporteService().prepararPerfilNutricional(usuario, modo));
    }

    /** Exporta la ficha nutricional (perfil + objetivo + requerimiento) a PDF. */
    @FXML
    private void handleExportarPdf() {
        exportar(FormatoReporte.PDF);
    }

    /** Exporta la ficha nutricional a Excel. */
    @FXML
    private void handleExportarExcel() {
        exportar(FormatoReporte.EXCEL);
    }

    private void exportar(FormatoReporte formato) {
        String modo = SesionActual.getModoActual();
        ExportadorReporte.exportar(lblTMB, "FichaNutricional", usuario, formato,
                destino -> new ReporteService().exportarPerfilNutricional(usuario, modo, destino));
    }

    @FXML
    private void handleVolver() {
        Navegacion.volver(lblTMB);
    }

    private Objetivo buscarObjetivo() {
        if (usuario == null || SesionActual.getModoActual() == null) {
            return null;
        }
        return objetivoDAO.buscarPorUsuarioYModo(usuario.getId(), SesionActual.getModoActual());
    }

    private void mostrarResultado(RequerimientoDiario resultado) {
        lblTMB.setText(String.format("%.0f kcal", resultado.getTmb()));
        lblCalorias.setText(String.format("%.0f kcal", resultado.getCaloriasDiarias()));
        lblProteinas.setText(String.format("%.0f g", resultado.getProteinas()));
        lblCarbohidratos.setText(String.format("%.0f g", resultado.getCarbohidratos()));
        lblGrasas.setText(String.format("%.0f g", resultado.getGrasas()));
    }
}

package ni.edu.uam.proyectofinal.reporte;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import ni.edu.uam.proyectofinal.dao.ItemListaSuperDAO;
import ni.edu.uam.proyectofinal.dao.ObjetivoDAO;
import ni.edu.uam.proyectofinal.dao.RequerimientoDiarioDAO;
import ni.edu.uam.proyectofinal.dao.RutinaDAO;
import ni.edu.uam.proyectofinal.dao.UsuarioDAO;
import ni.edu.uam.proyectofinal.model.ItemListaSuper;
import ni.edu.uam.proyectofinal.model.Objetivo;
import ni.edu.uam.proyectofinal.model.PerfilFisico;
import ni.edu.uam.proyectofinal.model.RequerimientoDiario;
import ni.edu.uam.proyectofinal.model.Rutina;
import ni.edu.uam.proyectofinal.model.RutinaEjercicio;
import ni.edu.uam.proyectofinal.model.Usuario;
import ni.edu.uam.proyectofinal.util.ReporteUtil;

import java.io.FileNotFoundException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Genera los reportes de Solea (PDF o Excel) con JasperReports a partir de los datos guardados en PostgreSQL.
 *
 * <ul>
 *   <li>Rutina de gimnasio: la última rutina del usuario, día por día.</li>
 *   <li>Lista del súper: los productos del usuario, pendientes primero.</li>
 *   <li>Ficha nutricional: perfil físico, objetivo y requerimiento diario calculado.</li>
 * </ul>
 *
 * Cada reporte se hace en dos pasos:
 * <ol>
 *   <li>{@code prepararXxx(...)} llena la plantilla y devuelve el reporte en memoria ({@link JasperPrint}),
 *       que es lo que se muestra en la vista previa.</li>
 *   <li>{@link #exportar(JasperPrint, Path)} lo guarda como PDF o Excel según la extensión del destino.</li>
 * </ol>
 * Los métodos {@code exportarXxx(...)} hacen los dos pasos de una vez.
 */
public class ReporteService {

    public static final String PLANTILLA_RUTINA = "ReporteRutinaGimnasio.jrxml";
    public static final String PLANTILLA_LISTA_SUPER = "ReporteListaSuper.jrxml";
    public static final String PLANTILLA_PERFIL = "ReportePerfilNutricional.jrxml";

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final RutinaDAO rutinaDAO = new RutinaDAO();
    private final ItemListaSuperDAO listaDAO = new ItemListaSuperDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final RequerimientoDiarioDAO requerimientoDAO = new RequerimientoDiarioDAO();
    private final ObjetivoDAO objetivoDAO = new ObjetivoDAO();

    // ------------------------------------------------------------ Rutina

    /** Exporta la última rutina del usuario. */
    public Path exportarRutina(Usuario usuario, Path destino) {
        validarUsuario(usuario);
        Rutina rutina = rutinaDAO.buscarUltimaPorUsuario(usuario.getId());
        if (rutina == null || rutina.getEjercicios().isEmpty()) {
            throw new IllegalStateException("Aún no tienes una rutina. Genera una antes de exportarla.");
        }
        return exportarRutina(usuario, rutina, destino);
    }

    /** Exporta una rutina ya cargada (con sus ejercicios y grupos musculares). */
    public Path exportarRutina(Usuario usuario, Rutina rutina, Path destino) {
        return exportar(prepararRutina(usuario, rutina), destino);
    }

    /** Llena el reporte de una rutina ya cargada, sin guardarlo. */
    public JasperPrint prepararRutina(Usuario usuario, Rutina rutina) {
        if (rutina == null || rutina.getEjercicios().isEmpty()) {
            throw new IllegalStateException("Aún no tienes una rutina. Genera una antes de exportarla.");
        }
        List<FilaRutinaReporte> filas = rutina.getEjercicios().stream()
                .sorted(Comparator.comparing(RutinaEjercicio::getDia).thenComparing(RutinaEjercicio::getOrden))
                .map(FilaRutinaReporte::new)
                .toList();

        Map<String, Object> parametros = new HashMap<>();
        parametros.put("USUARIO", usuario.getNombre());
        parametros.put("OBJETIVO", rutina.getObjetivo());
        parametros.put("DIAS_SEMANA", rutina.getDiasPorSemana());
        parametros.put("FECHA", hoy());
        return llenar(PLANTILLA_RUTINA, parametros, filas);
    }

    // --------------------------------------------------- Lista del súper

    /** Exporta la lista del súper del usuario. */
    public Path exportarListaSuper(Usuario usuario, Path destino) {
        validarUsuario(usuario);
        return exportarListaSuper(usuario, listaDAO.listarPorUsuario(usuario.getId()), destino);
    }

    public Path exportarListaSuper(Usuario usuario, List<ItemListaSuper> items, Path destino) {
        return exportar(prepararListaSuper(usuario, items), destino);
    }

    /** Llena el reporte de la lista del súper, sin guardarlo. */
    public JasperPrint prepararListaSuper(Usuario usuario, List<ItemListaSuper> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("Tu lista del súper está vacía: agrega productos antes de exportarla.");
        }
        List<FilaListaSuperReporte> filas = items.stream()
                .sorted(Comparator.comparing((ItemListaSuper i) -> Boolean.TRUE.equals(i.getComprado()))
                        .thenComparing(ItemListaSuper::getNombreProducto, String.CASE_INSENSITIVE_ORDER))
                .map(FilaListaSuperReporte::new)
                .toList();

        long pendientes = items.stream().filter(i -> !Boolean.TRUE.equals(i.getComprado())).count();
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("TITULO", "Lista del súper (" + pendientes + " pendiente" + (pendientes == 1 ? ")" : "s)"));
        parametros.put("USUARIO", usuario.getNombre());
        parametros.put("FECHA", hoy());
        return llenar(PLANTILLA_LISTA_SUPER, parametros, filas);
    }

    // ---------------------------------------------- Ficha nutricional

    /** Exporta la ficha nutricional con el objetivo del modo indicado (NUTRICION o GIMNASIO). */
    public Path exportarPerfilNutricional(Usuario usuario, String modo, Path destino) {
        return exportar(prepararPerfilNutricional(usuario, modo), destino);
    }

    public Path exportarPerfilNutricional(Usuario usuario, RequerimientoDiario requerimiento,
                                          Objetivo objetivo, Path destino) {
        return exportar(prepararPerfilNutricional(usuario, requerimiento, objetivo), destino);
    }

    /** Carga los datos con los DAO y llena la ficha nutricional, sin guardarla. */
    public JasperPrint prepararPerfilNutricional(Usuario usuario, String modo) {
        validarUsuario(usuario);
        Usuario conPerfil = usuarioDAO.obtenerUsuarioConPerfil(usuario.getId());
        RequerimientoDiario requerimiento = requerimientoDAO.buscarPorUsuario(usuario.getId());
        Objetivo objetivo = modo != null ? objetivoDAO.buscarPorUsuarioYModo(usuario.getId(), modo) : null;
        return prepararPerfilNutricional(conPerfil != null ? conPerfil : usuario, requerimiento, objetivo);
    }

    public JasperPrint prepararPerfilNutricional(Usuario usuario, RequerimientoDiario requerimiento,
                                                 Objetivo objetivo) {
        PerfilFisico perfil = usuario.getPerfilFisico();
        if (perfil == null) {
            throw new IllegalStateException("Completa tu perfil físico antes de exportar tu ficha nutricional.");
        }
        if (requerimiento == null) {
            throw new IllegalStateException("Calcula tu requerimiento diario antes de exportar tu ficha nutricional.");
        }

        Map<String, Object> parametros = new HashMap<>();
        parametros.put("NOMBRE_USUARIO", usuario.getNombre());
        parametros.put("CORREO", usuario.getCorreo());
        parametros.put("PESO", perfil.getPeso());
        parametros.put("ALTURA", perfil.getAltura());
        parametros.put("EDAD", perfil.getEdad());
        parametros.put("SEXO", perfil.getSexo());
        parametros.put("OBJETIVO", objetivo != null ? objetivo.getTipo() : "Mantener peso");
        parametros.put("CALORIAS_OBJETIVO", redondear(requerimiento.getCaloriasDiarias()));
        parametros.put("PROTEINAS_G", redondear(requerimiento.getProteinas()));
        parametros.put("CARBOHIDRATOS_G", redondear(requerimiento.getCarbohidratos()));
        parametros.put("GRASAS_G", redondear(requerimiento.getGrasas()));
        parametros.put("FECHA", hoy());
        return llenar(PLANTILLA_PERFIL, parametros, null);
    }

    // ------------------------------------------------------------ Común

    /**
     * Guarda un reporte ya llenado. El formato sale de la extensión del destino:
     * ".xlsx" genera un Excel; cualquier otra cosa, un PDF.
     */
    public Path exportar(JasperPrint impresion, Path destino) {
        if (destino == null) {
            throw new IllegalArgumentException("Elige dónde guardar el reporte.");
        }
        FormatoReporte formato = FormatoReporte.deArchivo(destino);
        Path archivo = formato.conExtension(destino);
        try {
            return formato == FormatoReporte.EXCEL
                    ? ReporteUtil.exportarXlsx(impresion, archivo, nombreHoja(impresion))
                    : ReporteUtil.exportarPdf(impresion, archivo);
        } catch (JRException e) {
            throw new ReporteException(mensajeDeError(e, archivo, formato), e);
        }
    }

    /** Nombre de archivo PDF sugerido, por ejemplo "Solea_Rutina_Ana_Maria_2026-11-20.pdf". */
    public static String nombreSugerido(String tipo, Usuario usuario) {
        return nombreSugerido(tipo, usuario, FormatoReporte.PDF);
    }

    /** Nombre de archivo sugerido con la extensión del formato, por ejemplo "Solea_Rutina_Ana_2026-11-20.xlsx". */
    public static String nombreSugerido(String tipo, Usuario usuario, FormatoReporte formato) {
        String nombre = usuario != null && usuario.getNombre() != null ? usuario.getNombre() : "usuario";
        String limpio = Normalizer.normalize(nombre, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^A-Za-z0-9]+", "_")
                .replaceAll("^_|_$", "");
        return "Solea_" + tipo + "_" + (limpio.isEmpty() ? "usuario" : limpio) + "_" + LocalDate.now()
                + "." + formato.getExtension();
    }

    private JasperPrint llenar(String plantilla, Map<String, Object> parametros, List<?> filas) {
        try {
            return ReporteUtil.compilarYLlenar(plantilla, parametros, filas);
        } catch (JRException e) {
            throw new ReporteException("No se pudo preparar el reporte. Inténtalo de nuevo.", e);
        }
    }

    /** Nombre de la hoja de Excel según la plantilla (JasperPrint guarda el nombre sin ".jrxml"). */
    private static String nombreHoja(JasperPrint impresion) {
        String nombre = impresion.getName() == null ? "" : impresion.getName() + ".jrxml";
        return switch (nombre) {
            case PLANTILLA_RUTINA -> "Rutina";
            case PLANTILLA_LISTA_SUPER -> "Lista del súper";
            case PLANTILLA_PERFIL -> "Ficha nutricional";
            default -> "Reporte";
        };
    }

    private static String mensajeDeError(Throwable e, Path archivo, FormatoReporte formato) {
        for (Throwable causa = e; causa != null; causa = causa.getCause()) {
            if (causa instanceof FileNotFoundException || causa instanceof AccessDeniedException) {
                return "No se pudo escribir " + archivo.getFileName() + ". Si el archivo está abierto en "
                        + (formato == FormatoReporte.EXCEL ? "Excel" : "otro programa")
                        + ", ciérralo, o elige otra carpeta.";
            }
        }
        return "No se pudo generar el reporte en " + formato.getNombre() + ". Inténtalo de nuevo.";
    }

    private static void validarUsuario(Usuario usuario) {
        if (usuario == null || usuario.getId() == null) {
            throw new IllegalArgumentException("Inicia sesión para generar reportes.");
        }
    }

    private static Integer redondear(Double valor) {
        return valor == null ? null : (int) Math.round(valor);
    }

    private static String hoy() {
        return LocalDate.now().format(FECHA);
    }
}

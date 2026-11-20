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
import java.util.Locale;
import java.util.Map;

/**
 * Genera los reportes PDF de Solea con JasperReports a partir de los datos guardados en PostgreSQL.
 *
 * <ul>
 *   <li>Rutina de gimnasio: la última rutina del usuario, día por día.</li>
 *   <li>Lista del súper: los productos del usuario, pendientes primero.</li>
 *   <li>Ficha nutricional: perfil físico, objetivo y requerimiento diario calculado.</li>
 * </ul>
 *
 * Cada reporte tiene dos versiones: una que carga los datos con los DAO y otra que recibe
 * los datos ya cargados (útil cuando la pantalla ya los tiene en memoria).
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
        List<FilaRutinaReporte> filas = rutina.getEjercicios().stream()
                .sorted(Comparator.comparing(RutinaEjercicio::getDia).thenComparing(RutinaEjercicio::getOrden))
                .map(FilaRutinaReporte::new)
                .toList();

        Map<String, Object> parametros = new HashMap<>();
        parametros.put("USUARIO", usuario.getNombre());
        parametros.put("OBJETIVO", rutina.getObjetivo());
        parametros.put("DIAS_SEMANA", rutina.getDiasPorSemana());
        parametros.put("FECHA", hoy());
        return generar(PLANTILLA_RUTINA, parametros, filas, destino);
    }

    // --------------------------------------------------- Lista del súper

    /** Exporta la lista del súper del usuario. */
    public Path exportarListaSuper(Usuario usuario, Path destino) {
        validarUsuario(usuario);
        return exportarListaSuper(usuario, listaDAO.listarPorUsuario(usuario.getId()), destino);
    }

    public Path exportarListaSuper(Usuario usuario, List<ItemListaSuper> items, Path destino) {
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
        return generar(PLANTILLA_LISTA_SUPER, parametros, filas, destino);
    }

    // ---------------------------------------------- Ficha nutricional

    /** Exporta la ficha nutricional con el objetivo del modo indicado (NUTRICION o GIMNASIO). */
    public Path exportarPerfilNutricional(Usuario usuario, String modo, Path destino) {
        validarUsuario(usuario);
        Usuario conPerfil = usuarioDAO.obtenerUsuarioConPerfil(usuario.getId());
        RequerimientoDiario requerimiento = requerimientoDAO.buscarPorUsuario(usuario.getId());
        Objetivo objetivo = modo != null ? objetivoDAO.buscarPorUsuarioYModo(usuario.getId(), modo) : null;
        return exportarPerfilNutricional(conPerfil != null ? conPerfil : usuario, requerimiento, objetivo, destino);
    }

    public Path exportarPerfilNutricional(Usuario usuario, RequerimientoDiario requerimiento,
                                          Objetivo objetivo, Path destino) {
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
        return generar(PLANTILLA_PERFIL, parametros, null, destino);
    }

    // ------------------------------------------------------------ Común

    /** Nombre de archivo sugerido, por ejemplo "Solea_Rutina_Ana_Maria_2026-11-20.pdf". */
    public static String nombreSugerido(String tipo, Usuario usuario) {
        String nombre = usuario != null && usuario.getNombre() != null ? usuario.getNombre() : "usuario";
        String limpio = Normalizer.normalize(nombre, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^A-Za-z0-9]+", "_")
                .replaceAll("^_|_$", "");
        return "Solea_" + tipo + "_" + (limpio.isEmpty() ? "usuario" : limpio) + "_" + LocalDate.now() + ".pdf";
    }

    private Path generar(String plantilla, Map<String, Object> parametros, List<?> filas, Path destino) {
        if (destino == null) {
            throw new IllegalArgumentException("Elige dónde guardar el PDF.");
        }
        Path pdf = destino.toString().toLowerCase(Locale.ROOT).endsWith(".pdf")
                ? destino
                : destino.resolveSibling(destino.getFileName() + ".pdf");
        try {
            JasperPrint impresion = ReporteUtil.compilarYLlenar(plantilla, parametros, filas);
            return ReporteUtil.exportarPdf(impresion, pdf);
        } catch (JRException e) {
            throw new ReporteException(mensajeDeError(e, pdf), e);
        }
    }

    private static String mensajeDeError(Throwable e, Path pdf) {
        for (Throwable causa = e; causa != null; causa = causa.getCause()) {
            if (causa instanceof FileNotFoundException || causa instanceof AccessDeniedException) {
                return "No se pudo escribir " + pdf.getFileName()
                        + ". Si el PDF está abierto en otro programa, ciérralo, o elige otra carpeta.";
            }
        }
        return "No se pudo generar el reporte PDF. Inténtalo de nuevo.";
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

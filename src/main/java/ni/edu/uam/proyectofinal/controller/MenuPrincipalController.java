package ni.edu.uam.proyectofinal.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import ni.edu.uam.proyectofinal.dao.ItemListaSuperDAO;
import ni.edu.uam.proyectofinal.dao.ObjetivoDAO;
import ni.edu.uam.proyectofinal.dao.RegistroConsumoDAO;
import ni.edu.uam.proyectofinal.dao.RequerimientoDiarioDAO;
import ni.edu.uam.proyectofinal.dao.RutinaDAO;
import ni.edu.uam.proyectofinal.model.ConsejoNutricional;
import ni.edu.uam.proyectofinal.model.ItemListaSuper;
import ni.edu.uam.proyectofinal.model.Objetivo;
import ni.edu.uam.proyectofinal.model.RegistroConsumo;
import ni.edu.uam.proyectofinal.model.RequerimientoDiario;
import ni.edu.uam.proyectofinal.model.Rutina;
import ni.edu.uam.proyectofinal.model.RutinaEjercicio;
import ni.edu.uam.proyectofinal.model.Usuario;
import ni.edu.uam.proyectofinal.service.NutricionApiClient;
import ni.edu.uam.proyectofinal.util.Alertas;
import ni.edu.uam.proyectofinal.util.Navegacion;
import ni.edu.uam.proyectofinal.util.SesionActual;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Inicio: resumen del día con las calorías y macros consumidos frente a la meta,
 * las comidas registradas, la rutina de hoy (modo gimnasio) o la lista del súper
 * (modo nutrición) y el consejo del día que entrega la API de Solea.
 */
public class MenuPrincipalController {

    /** Español de Nicaragua: fechas en español y miles con coma (2,640 kcal). */
    private static final Locale ES = Locale.forLanguageTag("es-NI");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", ES);

    @FXML private Text txtSaludo;
    @FXML private Text txtSaludoAcento;
    @FXML private Label lblBienvenida;

    @FXML private Text txtKcal;
    @FXML private Text txtKcalMeta;
    @FXML private Label lblKcalNota;
    @FXML private ProgressBar barKcal;
    @FXML private Text txtProt;
    @FXML private Text txtProtMeta;
    @FXML private ProgressBar barProt;
    @FXML private Label lblProtNota;
    @FXML private Text txtCarb;
    @FXML private Text txtCarbMeta;
    @FXML private ProgressBar barCarb;
    @FXML private Text txtGrasa;
    @FXML private Text txtGrasaMeta;
    @FXML private ProgressBar barGrasa;

    @FXML private Label lblComidasConteo;
    @FXML private VBox listaComidas;

    @FXML private Label lblSegundaTitulo;
    @FXML private Label lblSegundaDetalle;
    @FXML private Label lblSegundaSub;
    @FXML private VBox listaSegunda;
    @FXML private Button btnSegunda;

    @FXML private Label lblConsejo;
    @FXML private Label lblConsejoFuente;

    private final NutricionApiClient apiClient = new NutricionApiClient();

    private Usuario usuario;
    private boolean gimnasio;
    private String objetivoTipo = "Mantener peso";
    private ConsejoNutricional consejo;

    @FXML
    public void initialize() {
        usuario = SesionActual.getUsuarioActivo();
        gimnasio = SesionActual.MODO_GIMNASIO.equals(SesionActual.getModoActual());

        String nombre = usuario != null ? primerNombre(usuario.getNombre()) : "";
        String saludo = saludoSegunHora() + (nombre.isEmpty() ? ". " : ", " + nombre + ". ");
        txtSaludo.setText(saludo);
        txtSaludoAcento.setText("Empecemos.");

        cargarObjetivo();
        String fecha = LocalDate.now().format(FECHA);
        lblBienvenida.setText(Character.toUpperCase(fecha.charAt(0)) + fecha.substring(1)
                + " · Objetivo: " + objetivoTipo);

        double kcalFaltantes = cargarCifras();
        cargarComidas();
        String grupoDeHoy = gimnasio ? cargarRutina() : cargarListaSuper();

        // La parte en cursiva del saludo dice lo más útil del día
        if (gimnasio && grupoDeHoy != null) {
            txtSaludo.setText(saludo + "Hoy toca ");
            txtSaludoAcento.setText(grupoDeHoy.toLowerCase(ES) + ".");
        } else if (kcalFaltantes > 0) {
            txtSaludo.setText(saludo + "Te faltan ");
            txtSaludoAcento.setText(String.format(ES, "%,.0f kcal.", kcalFaltantes));
        } else if (kcalFaltantes == 0) {
            txtSaludoAcento.setText("Meta cumplida.");
        }

        cargarConsejo();
    }

    // ---------- Datos ----------

    private void cargarObjetivo() {
        if (usuario == null) {
            return;
        }
        try {
            Objetivo objetivo = new ObjetivoDAO().buscarPorUsuarioYModo(usuario.getId(), SesionActual.getModoActual());
            if (objetivo != null) {
                objetivoTipo = objetivo.getTipo();
            }
        } catch (Exception e) {
            // Sin objetivo guardado se muestra el valor por defecto
        }
    }

    /** Llena las cifras del día. Devuelve las kcal que faltan (o -1 si no hay meta). */
    private double cargarCifras() {
        if (usuario == null) {
            lblKcalNota.setText("Inicia sesión para ver tu día.");
            return -1;
        }
        double[] consumido = {0, 0, 0, 0};
        try {
            Object[] totales = new RegistroConsumoDAO().obtenerTotales(usuario.getId(), LocalDate.now());
            if (totales != null) {
                for (int i = 0; i < 4; i++) {
                    consumido[i] = totales[i] instanceof Number n ? n.doubleValue() : 0;
                }
            }
        } catch (Exception e) {
            // Sin registros se queda en cero
        }

        RequerimientoDiario meta = null;
        try {
            meta = new RequerimientoDiarioDAO().buscarPorUsuario(usuario.getId());
        } catch (Exception e) {
            // Se muestra el estado vacío
        }

        txtKcal.setText(entero(consumido[0]));
        txtProt.setText(entero(consumido[1]));
        txtCarb.setText(entero(consumido[2]));
        txtGrasa.setText(entero(consumido[3]));

        if (meta == null || meta.getCaloriasDiarias() == null) {
            lblKcalNota.setText("Calcula tu requerimiento para ver tu meta del día.");
            return -1;
        }

        double kcalMeta = valor(meta.getCaloriasDiarias());
        txtKcalMeta.setText("  / " + entero(kcalMeta) + " kcal");
        txtProtMeta.setText("  / " + entero(valor(meta.getProteinas())) + " g");
        txtCarbMeta.setText("  / " + entero(valor(meta.getCarbohidratos())) + " g");
        txtGrasaMeta.setText("  / " + entero(valor(meta.getGrasas())) + " g");

        double avanceKcal = proporcion(consumido[0], kcalMeta);
        double avanceProt = proporcion(consumido[1], valor(meta.getProteinas()));
        barKcal.setProgress(avanceKcal);
        barProt.setProgress(avanceProt);
        barCarb.setProgress(proporcion(consumido[2], valor(meta.getCarbohidratos())));
        barGrasa.setProgress(proporcion(consumido[3], valor(meta.getGrasas())));

        double faltan = Math.max(0, kcalMeta - consumido[0]);
        lblKcalNota.setText(faltan > 0
                ? String.format(ES, "Llevas el %.0f %% de tu meta.", avanceKcal * 100)
                : "Llegaste a tu meta de hoy.");

        // La proteína se queda atrás: se avisa con cuántos gramos faltan
        double protFaltante = valor(meta.getProteinas()) - consumido[1];
        if (consumido[0] > 0 && protFaltante > 0 && avanceProt < avanceKcal - 0.05) {
            lblProtNota.setText("Faltan " + entero(protFaltante) + " g");
            lblProtNota.setVisible(true);
            lblProtNota.setManaged(true);
            barProt.getStyleClass().add("alerta");
        }
        return faltan;
    }

    private void cargarComidas() {
        listaComidas.getChildren().clear();
        List<RegistroConsumo> registros = new ArrayList<>();
        if (usuario != null) {
            try {
                registros.addAll(new RegistroConsumoDAO().listarConsumoDiario(usuario.getId(), LocalDate.now()));
            } catch (Exception e) {
                // Lista vacía
            }
        }
        registros.sort(Comparator.comparing(RegistroConsumo::getHora,
                Comparator.nullsLast(Comparator.naturalOrder())));

        lblComidasConteo.setText(registros.size() == 1 ? "1 registro" : registros.size() + " registros");
        if (registros.isEmpty()) {
            listaComidas.getChildren().add(vacio("Aún no registras comidas hoy."));
            return;
        }
        int mostrados = 0;
        for (RegistroConsumo r : registros) {
            if (mostrados++ == 5) {
                listaComidas.getChildren().add(vacio("y " + (registros.size() - 5) + " más en Consumo."));
                break;
            }
            String alimento = r.getAlimento() != null ? r.getAlimento().getNombre() : "Alimento";
            String tipo = r.getTipoComida() != null ? r.getTipoComida() + " · " : "";
            listaComidas.getChildren().add(fila(tipo + alimento, entero(valor(r.getCaloriasConsumidas())) + " kcal", true));
        }
    }

    /** Rutina de hoy. Devuelve el nombre del día (p. ej. "Espalda y bíceps") o null si no hay rutina. */
    private String cargarRutina() {
        lblSegundaTitulo.setText("Rutina de hoy");
        btnSegunda.setText("Abrir rutina");
        listaSegunda.getChildren().clear();

        Rutina rutina = null;
        if (usuario != null) {
            try {
                rutina = new RutinaDAO().buscarUltimaPorUsuario(usuario.getId());
            } catch (Exception e) {
                // Sin rutina
            }
        }
        if (rutina == null || rutina.getEjercicios().isEmpty()) {
            lblSegundaSub.setText("Aún no generas tu rutina de gimnasio.");
            btnSegunda.setText("Generar rutina");
            return null;
        }

        int dias = rutina.getDiasPorSemana() != null && rutina.getDiasPorSemana() > 0 ? rutina.getDiasPorSemana() : 1;
        int diaDeHoy = (LocalDate.now().getDayOfWeek().getValue() - 1) % dias + 1;
        List<RutinaEjercicio> deHoy = rutina.getEjercicios().stream()
                .filter(re -> re.getDia() != null && re.getDia() == diaDeHoy)
                .sorted(Comparator.comparing(RutinaEjercicio::getOrden, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        lblSegundaDetalle.setText("Día " + diaDeHoy + " de " + dias);
        if (deHoy.isEmpty()) {
            lblSegundaSub.setText("Hoy es día de descanso.");
            return null;
        }
        RutinaEjercicio primero = deHoy.get(0);
        String nombreDia = limpiarNombreDia(primero.getNombreDia());
        lblSegundaSub.setText(nombreDia + " · " + primero.getSeries() + " × " + primero.getRepeticiones());
        for (RutinaEjercicio re : deHoy.stream().limit(5).toList()) {
            String ejercicio = re.getEjercicio() != null ? re.getEjercicio().getNombre() : "Ejercicio";
            listaSegunda.getChildren().add(fila(ejercicio, re.getSeries() + " series", false));
        }
        return nombreDia;
    }

    private String cargarListaSuper() {
        lblSegundaTitulo.setText("Lista del súper");
        btnSegunda.setText("Ver lista");
        listaSegunda.getChildren().clear();

        List<ItemListaSuper> items = new ArrayList<>();
        if (usuario != null) {
            try {
                items.addAll(new ItemListaSuperDAO().listarPorUsuario(usuario.getId()));
            } catch (Exception e) {
                // Lista vacía
            }
        }
        List<ItemListaSuper> pendientes = items.stream()
                .filter(i -> !Boolean.TRUE.equals(i.getComprado()))
                .toList();
        lblSegundaDetalle.setText(pendientes.size() + " de " + items.size() + " pendientes");
        if (items.isEmpty()) {
            lblSegundaSub.setText("Tu lista está vacía. Llénala desde el plan semanal.");
            return null;
        }
        lblSegundaSub.setText(pendientes.isEmpty() ? "Ya compraste todo." : "Lo que falta comprar:");
        for (ItemListaSuper i : pendientes.stream().limit(5).toList()) {
            String cantidad = i.getCantidad() != null
                    ? entero(i.getCantidad()) + (i.getUnidad() != null ? " " + i.getUnidad() : "")
                    : "";
            listaSegunda.getChildren().add(fila(i.getNombreProducto(), cantidad, false));
        }
        return null;
    }

    private void cargarConsejo() {
        if (usuario != null) {
            try {
                Objetivo ultimo = new ObjetivoDAO().buscarUltimoPorUsuario(usuario.getId());
                if (ultimo != null) {
                    objetivoTipo = ultimo.getTipo();
                }
            } catch (Exception e) {
                // Se pide el consejo general
            }
        }
        apiClient.obtenerConsejoAsync(objetivoTipo)
                .whenComplete((resultado, error) -> Platform.runLater(() -> {
                    if (error != null || resultado == null) {
                        lblConsejo.setText("Los consejos no están disponibles ahora.");
                        lblConsejoFuente.setText("Inicia la API de Solea para verlos aquí.");
                        return;
                    }
                    consejo = resultado;
                    lblConsejo.setText("“" + resultado.getConsejo() + "”");
                    lblConsejoFuente.setText(resultado.getFuente() != null ? "Fuente: " + resultado.getFuente() : "");
                }));
    }

    // ---------- Acciones ----------

    @FXML
    private void handleConsumo() {
        Navegacion.ir(lblBienvenida, "consumo-view.fxml", "Registro de consumo", 0, 0);
    }

    @FXML
    private void handlePlan() {
        Navegacion.ir(lblBienvenida, "plan-semanal-view.fxml", "Plan semanal", 0, 0);
    }

    @FXML
    private void handleSegunda() {
        if (gimnasio) {
            Navegacion.ir(lblBienvenida, "rutina-view.fxml", "Rutina de gimnasio", 0, 0);
        } else {
            Navegacion.ir(lblBienvenida, "lista-super-view.fxml", "Lista del súper", 0, 0);
        }
    }

    @FXML
    private void handleEditarPerfil() {
        Navegacion.irAlPerfil(lblBienvenida);
    }

    @FXML
    private void handleConsultarApiConsejo() {
        if (consejo != null) {
            mostrarDialogoConsejo(consejo);
            return;
        }
        lblBienvenida.getScene().setCursor(javafx.scene.Cursor.WAIT);
        apiClient.obtenerConsejoAsync(objetivoTipo)
                .whenComplete((resultado, error) -> Platform.runLater(() -> {
                    lblBienvenida.getScene().setCursor(javafx.scene.Cursor.DEFAULT);
                    if (error != null || resultado == null) {
                        Alertas.error("Consejo no disponible",
                                "No se pudo obtener el consejo nutricional. Revisa que la API de Solea esté encendida e inténtalo de nuevo.");
                    } else {
                        consejo = resultado;
                        mostrarDialogoConsejo(resultado);
                    }
                }));
    }

    private void mostrarDialogoConsejo(ConsejoNutricional c) {
        String mensaje = c.getConsejo() + "\n\n"
                + "Distribución de macros: " + c.getDistribucionMacrosSugerida() + "\n"
                + "Hidratación sugerida: " + c.getRecomendacionAgua() + "\n\n"
                + "Fuente: " + c.getFuente();
        Alertas.mostrar(Alert.AlertType.INFORMATION, c.getTitulo(), mensaje);
    }

    // ---------- Ayudantes de interfaz ----------

    private HBox fila(String texto, String dato, boolean hecho) {
        Label izquierda = new Label(texto);
        izquierda.setMinWidth(0);
        izquierda.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
        Label derecha = new Label(dato);
        derecha.getStyleClass().add("dato");
        derecha.setMinWidth(Region.USE_PREF_SIZE);
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox.setHgrow(izquierda, Priority.SOMETIMES);
        HBox fila = new HBox(12, izquierda, espacio, derecha);
        fila.getStyleClass().add("fila-lista");
        if (hecho) {
            fila.getStyleClass().add("hecho");
        }
        return fila;
    }

    private Label vacio(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("vacio");
        label.setWrapText(true);
        return label;
    }

    private static String saludoSegunHora() {
        int hora = LocalTime.now().getHour();
        if (hora < 12) {
            return "Buenos días";
        }
        return hora < 19 ? "Buenas tardes" : "Buenas noches";
    }

    private static String primerNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "";
        }
        return nombre.trim().split("\\s+")[0];
    }

    /** "Día 2 - Espalda y bíceps" → "Espalda y bíceps". */
    private static String limpiarNombreDia(String nombreDia) {
        if (nombreDia == null || nombreDia.isBlank()) {
            return "Entrenamiento";
        }
        String limpio = nombreDia.replaceFirst("(?i)^d[ií]a\\s*\\d+\\s*[-:·–]\\s*", "").trim();
        return limpio.isEmpty() ? nombreDia : limpio;
    }

    private static double valor(Double d) {
        return d != null ? d : 0;
    }

    private static double proporcion(double parte, double total) {
        return total <= 0 ? 0 : Math.min(1, parte / total);
    }

    private static String entero(double valor) {
        return String.format(ES, "%,.0f", valor);
    }
}

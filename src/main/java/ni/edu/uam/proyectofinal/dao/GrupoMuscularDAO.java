package ni.edu.uam.proyectofinal.dao;

import ni.edu.uam.proyectofinal.model.Ejercicio;
import ni.edu.uam.proyectofinal.model.GrupoMuscular;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GrupoMuscularDAO extends GenericDAO<GrupoMuscular> {

    public static final String PECHO = "Pecho";
    public static final String ESPALDA = "Espalda";
    public static final String PIERNA = "Pierna";
    public static final String HOMBRO = "Hombro";
    public static final String BICEPS = "Bíceps";
    public static final String TRICEPS = "Tríceps";
    public static final String CORE = "Core";

    public GrupoMuscularDAO() {
        super(GrupoMuscular.class, "el grupo muscular");
    }

    /** Grupos musculares ordenados por nombre. */
    @Override
    public List<GrupoMuscular> listarTodos() {
        return consultar("listar los grupos musculares", session ->
                session.createQuery("FROM GrupoMuscular g ORDER BY g.nombre", GrupoMuscular.class).list());
    }

    /** No se borra un grupo que todavía tiene ejercicios en el catálogo. */
    @Override
    public void eliminar(Long id) {
        Long ejercicios = consultar("verificar el grupo muscular", session -> session.createQuery(
                        "SELECT COUNT(e) FROM Ejercicio e WHERE e.grupoMuscular.id = :id", Long.class)
                .setParameter("id", id)
                .uniqueResult());
        if (ejercicios != null && ejercicios > 0) {
            throw new IllegalStateException("El grupo tiene " + ejercicios
                    + " ejercicio(s). Muévelos o elimínalos antes de borrar el grupo.");
        }
        super.eliminar(id);
    }

    /**
     * Si no hay grupos musculares registrados, carga los grupos y un catálogo
     * básico de ejercicios (todo en una sola transacción).
     */
    public void asegurarDatosIniciales() {
        Long cantidad = consultar("contar los grupos musculares", session ->
                session.createQuery("SELECT COUNT(g) FROM GrupoMuscular g", Long.class).uniqueResult());
        if (cantidad != null && cantidad > 0) {
            return;
        }
        ejecutar("cargar los ejercicios iniciales", session -> {
            for (Map.Entry<String[], String[][]> entrada : catalogo().entrySet()) {
                GrupoMuscular grupo = new GrupoMuscular(entrada.getKey()[0], entrada.getKey()[1]);
                for (String[] e : entrada.getValue()) {
                    grupo.agregarEjercicio(new Ejercicio(e[0], e[1], e[2], grupo));
                }
                session.persist(grupo);
                grupo.getEjercicios().forEach(session::persist);
            }
        });
    }

    /** Grupo {nombre, descripción} → ejercicios {nombre, equipo, descripción}. */
    private Map<String[], String[][]> catalogo() {
        Map<String[], String[][]> datos = new LinkedHashMap<>();
        datos.put(new String[]{PECHO, "Pectoral mayor y menor"}, new String[][]{
                {"Press de banca con barra", "Barra y banco", "Baja la barra al pecho y empuja hasta extender los brazos."},
                {"Press inclinado con mancuernas", "Mancuernas y banco", "Banco a 30-45°, empuja las mancuernas hacia arriba."},
                {"Aperturas con mancuernas", "Mancuernas y banco", "Abre los brazos con codos semiflexionados y vuelve a juntar."},
                {"Fondos en paralelas", "Paralelas", "Inclina el torso hacia adelante para trabajar el pecho."},
                {"Flexiones de pecho", "Peso corporal", "Cuerpo recto, baja el pecho casi al suelo."}
        });
        datos.put(new String[]{ESPALDA, "Dorsales, trapecio y romboides"}, new String[][]{
                {"Dominadas", "Barra fija", "Sube hasta pasar la barbilla sobre la barra."},
                {"Remo con barra", "Barra", "Torso inclinado, lleva la barra hacia el abdomen."},
                {"Jalón al pecho", "Polea alta", "Baja la barra hacia el pecho juntando escápulas."},
                {"Remo con mancuerna a una mano", "Mancuerna y banco", "Apoya una mano en el banco y tira del codo hacia atrás."},
                {"Remo en polea baja", "Polea baja", "Espalda recta, lleva el agarre al abdomen."}
        });
        datos.put(new String[]{PIERNA, "Cuádriceps, isquiotibiales, glúteos y pantorrilla"}, new String[][]{
                {"Sentadilla con barra", "Barra y rack", "Baja hasta que los muslos queden paralelos al suelo."},
                {"Prensa de piernas", "Máquina", "Empuja la plataforma sin bloquear las rodillas."},
                {"Peso muerto rumano", "Barra", "Rodillas casi rectas, baja la barra pegada a las piernas."},
                {"Zancadas con mancuernas", "Mancuernas", "Da un paso largo y baja la rodilla trasera."},
                {"Curl femoral en máquina", "Máquina", "Flexiona las rodillas llevando los talones hacia los glúteos."},
                {"Elevación de talones", "Máquina o mancuernas", "Sube sobre las puntas de los pies y baja lento."}
        });
        datos.put(new String[]{HOMBRO, "Deltoides anterior, lateral y posterior"}, new String[][]{
                {"Press militar", "Barra o mancuernas", "Empuja el peso por encima de la cabeza."},
                {"Elevaciones laterales", "Mancuernas", "Sube los brazos a los lados hasta la altura de los hombros."},
                {"Pájaros", "Mancuernas", "Torso inclinado, abre los brazos para el deltoide posterior."},
                {"Press Arnold", "Mancuernas", "Gira las palmas mientras empujas hacia arriba."},
                {"Face pull", "Polea con cuerda", "Tira de la cuerda hacia la cara separando las manos."}
        });
        datos.put(new String[]{BICEPS, "Bíceps braquial y braquial"}, new String[][]{
                {"Curl con barra", "Barra", "Codos pegados al cuerpo, sube la barra."},
                {"Curl martillo", "Mancuernas", "Palmas mirándose entre sí durante todo el movimiento."},
                {"Curl concentrado", "Mancuerna", "Sentado, apoya el codo en el muslo."},
                {"Curl en polea", "Polea baja", "Tensión constante en todo el recorrido."}
        });
        datos.put(new String[]{TRICEPS, "Tríceps braquial"}, new String[][]{
                {"Press francés", "Barra Z y banco", "Acostado, baja la barra hacia la frente doblando los codos."},
                {"Extensión en polea", "Polea alta", "Codos fijos, extiende los brazos hacia abajo."},
                {"Fondos entre bancos", "Bancos", "Baja el cuerpo doblando los codos detrás de ti."},
                {"Patada de tríceps", "Mancuerna", "Torso inclinado, extiende el brazo hacia atrás."}
        });
        datos.put(new String[]{CORE, "Abdomen y zona media"}, new String[][]{
                {"Plancha", "Peso corporal", "Mantén el cuerpo recto apoyado en antebrazos."},
                {"Crunch abdominal", "Peso corporal", "Eleva los hombros del suelo contrayendo el abdomen."},
                {"Elevación de piernas", "Peso corporal", "Acostado, sube las piernas rectas sin arquear la espalda."},
                {"Russian twist", "Disco o peso corporal", "Sentado, gira el torso de lado a lado."}
        });
        return datos;
    }
}

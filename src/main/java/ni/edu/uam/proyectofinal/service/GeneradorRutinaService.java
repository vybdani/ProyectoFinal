package ni.edu.uam.proyectofinal.service;

import ni.edu.uam.proyectofinal.dao.EjercicioDAO;
import ni.edu.uam.proyectofinal.dao.GrupoMuscularDAO;
import ni.edu.uam.proyectofinal.dao.RutinaDAO;
import ni.edu.uam.proyectofinal.model.Ejercicio;
import ni.edu.uam.proyectofinal.model.Rutina;
import ni.edu.uam.proyectofinal.model.RutinaEjercicio;
import ni.edu.uam.proyectofinal.model.Usuario;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static ni.edu.uam.proyectofinal.dao.GrupoMuscularDAO.*;

/**
 * Arma una rutina semanal según el objetivo de gimnasio y los días disponibles.
 * <ul>
 *   <li>3 días: Empuje / Tirón / Pierna</li>
 *   <li>4 días: Torso / Pierna / Torso / Pierna</li>
 *   <li>5 días: Pecho / Espalda / Pierna / Hombro y core / Brazos</li>
 * </ul>
 * Cada objetivo tiene su esquema de series, repeticiones y descanso (ver {@link #esquema}).
 * El core siempre se trabaja con 3 series de 15-20 repeticiones y 45 s de descanso.
 */
public class GeneradorRutinaService {

    public static final List<Integer> DIAS_DISPONIBLES = List.of(3, 4, 5);

    /** Series, repeticiones y descanso (en segundos) de los ejercicios de un objetivo. */
    public record Esquema(int series, String repeticiones, int descansoSegundos) {
    }

    /** Esquema de entrenamiento según el objetivo de gimnasio. */
    public static Esquema esquema(String objetivo) {
        return switch (objetivo) {
            case CalculoNutricionalService.VOLUMEN -> new Esquema(4, "8-12", 90);
            case CalculoNutricionalService.DEFINICION -> new Esquema(3, "12-15", 45);
            case CalculoNutricionalService.FUERZA -> new Esquema(5, "3-5", 180);
            case CalculoNutricionalService.RECOMPOSICION -> new Esquema(4, "6-10", 75);
            case CalculoNutricionalService.RESISTENCIA -> new Esquema(3, "15-20", 30);
            case CalculoNutricionalService.TONIFICACION -> new Esquema(3, "10-15", 60);
            default -> throw new IllegalArgumentException("Objetivo de gimnasio no válido: " + objetivo);
        };
    }

    private final EjercicioDAO ejercicioDAO = new EjercicioDAO();
    private final GrupoMuscularDAO grupoDAO = new GrupoMuscularDAO();
    private final RutinaDAO rutinaDAO = new RutinaDAO();

    public Rutina generar(Usuario usuario, String objetivo, int diasPorSemana) {
        if (usuario == null) {
            throw new IllegalArgumentException("Debe haber un usuario activo para generar la rutina.");
        }
        if (!CalculoNutricionalService.OBJETIVOS_GIMNASIO.contains(objetivo)) {
            throw new IllegalArgumentException("Primero elige y guarda tu objetivo de gimnasio en el menú del Modo Gimnasio.");
        }
        if (!DIAS_DISPONIBLES.contains(diasPorSemana)) {
            throw new IllegalArgumentException("Elige entre 3 y 5 días de entrenamiento por semana.");
        }

        grupoDAO.asegurarDatosIniciales();

        Esquema esquema = esquema(objetivo);
        int series = esquema.series();
        String repeticiones = esquema.repeticiones();
        int descanso = esquema.descansoSegundos();

        Rutina rutina = new Rutina(
                "Rutina " + objetivo + " - " + diasPorSemana + " días",
                objetivo,
                diasPorSemana,
                usuario
        );

        // Cuántas veces se ha usado cada grupo, para no repetir siempre el mismo ejercicio
        Map<String, Integer> usosPorGrupo = new HashMap<>();
        Map<String, List<Ejercicio>> cache = new HashMap<>();

        int numeroDia = 1;
        for (Map.Entry<String, Map<String, Integer>> dia : plantilla(diasPorSemana).entrySet()) {
            int orden = 1;
            for (Map.Entry<String, Integer> grupo : dia.getValue().entrySet()) {
                List<Ejercicio> disponibles = cache.computeIfAbsent(grupo.getKey(), ejercicioDAO::listarPorGrupo);
                if (disponibles.isEmpty()) {
                    continue;
                }
                int cantidad = Math.min(grupo.getValue(), disponibles.size());
                int inicio = usosPorGrupo.getOrDefault(grupo.getKey(), 0);
                for (int i = 0; i < cantidad; i++) {
                    Ejercicio ejercicio = disponibles.get((inicio + i) % disponibles.size());
                    boolean esCore = CORE.equals(grupo.getKey());
                    rutina.agregarEjercicio(new RutinaEjercicio(
                            ejercicio,
                            numeroDia,
                            dia.getKey(),
                            orden++,
                            esCore ? 3 : series,
                            esCore ? "15-20" : repeticiones,
                            esCore ? 45 : descanso
                    ));
                }
                usosPorGrupo.put(grupo.getKey(), inicio + cantidad);
            }
            numeroDia++;
        }

        if (rutina.getEjercicios().isEmpty()) {
            throw new IllegalStateException("No hay ejercicios registrados para armar la rutina.");
        }

        rutinaDAO.guardar(rutina);
        return rutina;
    }

    /** Día → (grupo muscular → número de ejercicios). */
    private Map<String, Map<String, Integer>> plantilla(int dias) {
        Map<String, Map<String, Integer>> semana = new LinkedHashMap<>();
        switch (dias) {
            case 3 -> {
                semana.put("Empuje", grupos(PECHO, 2, HOMBRO, 2, TRICEPS, 2));
                semana.put("Tirón", grupos(ESPALDA, 3, BICEPS, 2, CORE, 1));
                semana.put("Pierna", grupos(PIERNA, 4, CORE, 1));
            }
            case 4 -> {
                semana.put("Torso A", grupos(PECHO, 2, ESPALDA, 2, HOMBRO, 1, BICEPS, 1, TRICEPS, 1));
                semana.put("Pierna A", grupos(PIERNA, 4, CORE, 2));
                semana.put("Torso B", grupos(ESPALDA, 2, PECHO, 2, HOMBRO, 1, BICEPS, 1, TRICEPS, 1));
                semana.put("Pierna B", grupos(PIERNA, 4, CORE, 2));
            }
            default -> {
                semana.put("Pecho", grupos(PECHO, 4, TRICEPS, 1));
                semana.put("Espalda", grupos(ESPALDA, 4, BICEPS, 1));
                semana.put("Pierna", grupos(PIERNA, 5));
                semana.put("Hombro y core", grupos(HOMBRO, 4, CORE, 2));
                semana.put("Brazos", grupos(BICEPS, 3, TRICEPS, 3));
            }
        }
        return semana;
    }

    private Map<String, Integer> grupos(Object... pares) {
        Map<String, Integer> mapa = new LinkedHashMap<>();
        for (int i = 0; i < pares.length; i += 2) {
            mapa.put((String) pares[i], (Integer) pares[i + 1]);
        }
        return mapa;
    }
}

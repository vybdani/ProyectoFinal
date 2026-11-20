package ni.edu.uam.api.service;

import ni.edu.uam.api.dto.ConsejoNutricionalDTO;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio que genera pautas y recomendaciones nutricionales personalizadas
 * según el objetivo seleccionado en Solea.
 */
@Service
public class RecomendacionService {

    private final Map<String, ConsejoNutricionalDTO> recomendaciones = new HashMap<>();

    public RecomendacionService() {
        cargarRecomendacionesBase();
    }

    private void cargarRecomendacionesBase() {
        recomendaciones.put("bajar", ConsejoNutricionalDTO.builder()
                .objetivo("Pérdida de Grasa / Bajar de Peso")
                .titulo("Déficit Calórico Controlado y Alto en Proteína")
                .consejo("Mantén un déficit de 300 a 500 kcal diarias. Prioriza alimentos saciantes ricos en fibra (verduras, avena) y mantén la proteína alta para preservar masa muscular.")
                .distribucionMacrosSugerida("40% Carbohidratos | 35% Proteínas | 25% Grasas saludables")
                .recomendacionAgua("35 a 40 ml por cada kg de peso corporal al día.")
                .fuente("Solea Intelligence Engine")
                .build());

        recomendaciones.put("mantener", ConsejoNutricionalDTO.builder()
                .objetivo("Mantenimiento y Salud General")
                .titulo("Equilibrio Energético y Densidad Nutricional")
                .consejo("Consume tus calorías de mantenimiento diario. Varía tus fuentes de alimentos para garantizar micronutrientes y mantén un nivel de actividad física moderado.")
                .distribucionMacrosSugerida("50% Carbohidratos | 25% Proteínas | 25% Grasas saludables")
                .recomendacionAgua("30 a 35 ml por kg de peso corporal.")
                .fuente("Solea Intelligence Engine")
                .build());

        recomendaciones.put("fuerza", ConsejoNutricionalDTO.builder()
                .objetivo("Fuerza / Modo Gimnasio")
                .titulo("Cargas Pesadas, Pocas Repeticiones y Descanso Completo")
                .consejo("Trabaja con 3 a 5 repeticiones cerca de tu máximo y descansa 2 a 3 minutos entre series. "
                        + "Come un ligero superávit, reparte la proteína en 4 comidas y prioriza el sueño para recuperar el sistema nervioso.")
                .distribucionMacrosSugerida("45% Carbohidratos | 30% Proteínas | 25% Grasas")
                .recomendacionAgua("40 ml por kg de peso corporal.")
                .fuente("Solea Intelligence Engine")
                .build());
        recomendaciones.put("recomposicion", ConsejoNutricionalDTO.builder()
                .objetivo("Recomposición Corporal / Modo Gimnasio")
                .titulo("Perder Grasa y Ganar Músculo al Mismo Tiempo")
                .consejo("Mantén un déficit pequeño (cerca de 10 %) con proteína alta en cada comida. "
                        + "Entrena con progresión de cargas en rangos de 6 a 10 repeticiones y mide tu avance con fotos y medidas, no solo con la báscula.")
                .distribucionMacrosSugerida("35% Carbohidratos | 40% Proteínas | 25% Grasas")
                .recomendacionAgua("40 ml por kg de peso corporal.")
                .fuente("Solea Intelligence Engine")
                .build());
        recomendaciones.put("resistencia", ConsejoNutricionalDTO.builder()
                .objetivo("Resistencia Muscular / Modo Gimnasio")
                .titulo("Más Repeticiones, Menos Descanso y Buena Hidratación")
                .consejo("Haz series de 15 a 20 repeticiones con descansos cortos (30 s). "
                        + "Los carbohidratos son tu combustible: inclúyelos antes de entrenar y repón líquidos y electrolitos al terminar.")
                .distribucionMacrosSugerida("55% Carbohidratos | 20% Proteínas | 25% Grasas")
                .recomendacionAgua("45 ml por kg de peso corporal (más si sudas mucho).")
                .fuente("Solea Intelligence Engine")
                .build());
        recomendaciones.put("tonificacion", ConsejoNutricionalDTO.builder()
                .objetivo("Tonificación / Modo Gimnasio")
                .titulo("Marcar el Músculo con Déficit Ligero y Constancia")
                .consejo("Combina pesas en 10 a 15 repeticiones con 2 o 3 sesiones de cardio suave por semana. "
                        + "Un déficit ligero y proteína suficiente ayudan a que el músculo se note sin perder fuerza.")
                .distribucionMacrosSugerida("40% Carbohidratos | 30% Proteínas | 30% Grasas saludables")
                .recomendacionAgua("35 a 40 ml por kg de peso corporal.")
                .fuente("Solea Intelligence Engine")
                .build());
        recomendaciones.put("subir", ConsejoNutricionalDTO.builder()
                .objetivo("Subir de Peso")
                .titulo("Superávit Moderado y Comidas Densas en Energía")
                .consejo("Suma alrededor de 10 % a tus calorías de mantenimiento con comidas frecuentes y densas en energía (avena, frutos secos, lácteos, aguacate). Acompaña con entrenamiento de fuerza para que el peso ganado sea de calidad.")
                .distribucionMacrosSugerida("50% Carbohidratos | 20% Proteínas | 30% Grasas saludables")
                .recomendacionAgua("35 ml por kg de peso corporal.")
                .fuente("Solea Intelligence Engine")
                .build());
        recomendaciones.put("volumen", ConsejoNutricionalDTO.builder()
                .objetivo("Ganar Masa Muscular / Modo Gimnasio")
                .titulo("Superávit Calórico Limpio y Progresión de Cargas")
                .consejo("Consume un superávit de 250 a 400 kcal por encima de tu mantenimiento. Entrena cerca del fallo muscular (8-12 reps) y duerme de 7 a 9 horas para optimizar la síntesis proteica.")
                .distribucionMacrosSugerida("55% Carbohidratos | 25% Proteínas | 20% Grasas")
                .recomendacionAgua("40 a 45 ml por kg de peso corporal (mayor por sudoración en pesas).")
                .fuente("Solea Intelligence Engine")
                .build());

        recomendaciones.put("definicion", ConsejoNutricionalDTO.builder()
                .objetivo("Definición Muscular / Modo Gimnasio")
                .titulo("Déficit Moderado con Mantenimiento de Fuerza")
                .consejo("Reduce carbohidratos gradualmente manteniendo la intensidad en los levantamientos. Utiliza series de 10-15 reps con descansos controlados.")
                .distribucionMacrosSugerida("35% Carbohidratos | 40% Proteínas | 25% Grasas")
                .recomendacionAgua("40 ml por kg de peso corporal.")
                .fuente("Solea Intelligence Engine")
                .build());
    }

    public ConsejoNutricionalDTO obtenerConsejoPorObjetivo(String objetivo) {
        if (objetivo == null || objetivo.trim().isEmpty()) {
            return recomendaciones.get("mantener");
        }

        String clave = java.text.Normalizer.normalize(objetivo, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(java.util.Locale.ROOT).trim();
        for (String k : recomendaciones.keySet()) {
            if (clave.contains(k)) {
                return recomendaciones.get(k);
            }
        }

        return recomendaciones.get("mantener");
    }
}

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

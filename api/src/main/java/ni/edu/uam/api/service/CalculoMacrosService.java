package ni.edu.uam.api.service;

import ni.edu.uam.api.dto.CalculoMacrosRequest;
import ni.edu.uam.api.dto.CalculoMacrosResponse;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Cálculo de calorías y macronutrientes. Usa exactamente las mismas reglas que
 * CalculoNutricionalService de la app de escritorio, para que ambos den el mismo resultado:
 *
 * <ul>
 *   <li>TMB: Katch-McArdle si hay % de grasa; si no, Mifflin-St Jeor.</li>
 *   <li>Factor de actividad: sedentario 1.2, ligero 1.375, moderado 1.55, alto 1.725.</li>
 *   <li>Ajuste por objetivo: bajar -20 %, subir +10 %, volumen +12 %, definición -20 %,
 *       fuerza +5 %, recomposición -10 %, resistencia 0 %, tonificación -5 %.</li>
 *   <li>Mínimo seguro: 1200 kcal (mujeres) / 1500 kcal (hombres).</li>
 *   <li>Proteína por kg: 1.6 mantener/resistencia, 1.8 bajar/subir/tonificación, 2.0 volumen/fuerza,
 *       2.2 definición/recomposición.
 *       Grasa 25 % de las kcal; el resto, carbohidratos.</li>
 * </ul>
 */
@Service
public class CalculoMacrosService {

    public CalculoMacrosResponse calcular(CalculoMacrosRequest datos) {
        boolean mujer = "femenino".equalsIgnoreCase(datos.sexo());
        double peso = datos.pesoKg();
        Double grasa = datos.porcentajeGrasa();

        double tmb;
        String formula;
        if (grasa != null && grasa > 0) {
            tmb = 370 + 21.6 * peso * (1 - grasa / 100);
            formula = "Katch-McArdle";
        } else {
            tmb = 10 * peso + 6.25 * datos.alturaCm() - 5 * datos.edad() + (mujer ? -161 : 5);
            formula = "Mifflin-St Jeor";
        }

        double factor = factorActividad(datos.nivelActividad());
        double mantenimiento = tmb * factor;
        Objetivo objetivo = Objetivo.desde(datos.objetivo());
        double calorias = Math.max(mantenimiento * (1 + objetivo.ajuste), mujer ? 1200 : 1500);

        double proteinas = objetivo.proteinaPorKilo * peso;
        double grasas = calorias * 0.25 / 9;
        double carbohidratos = Math.max(0, (calorias - proteinas * 4 - grasas * 9) / 4);

        return new CalculoMacrosResponse(objetivo.nombre, formula,
                redondear(tmb), factor, redondear(mantenimiento), redondear(calorias),
                redondear(proteinas), redondear(carbohidratos), redondear(grasas));
    }

    private static double factorActividad(String nivel) {
        return switch (nivel.toLowerCase(Locale.ROOT)) {
            case "sedentario" -> 1.2;
            case "ligero" -> 1.375;
            case "moderado" -> 1.55;
            case "alto" -> 1.725;
            default -> throw new IllegalArgumentException("Nivel de actividad no válido: " + nivel);
        };
    }

    private static double redondear(double valor) {
        return Math.round(valor * 10) / 10.0;
    }

    /** Objetivos de Solea con su ajuste calórico y proteína por kg. */
    enum Objetivo {
        BAJAR("Bajar de peso", -0.20, 1.8),
        MANTENER("Mantener peso", 0, 1.6),
        SUBIR("Subir de peso", 0.10, 1.8),
        VOLUMEN("Volumen muscular", 0.12, 2.0),
        DEFINICION("Definición", -0.20, 2.2),
        FUERZA("Fuerza", 0.05, 2.0),
        RECOMPOSICION("Recomposición corporal", -0.10, 2.2),
        RESISTENCIA("Resistencia muscular", 0, 1.6),
        TONIFICACION("Tonificación", -0.05, 1.8);

        final String nombre;
        final double ajuste;
        final double proteinaPorKilo;

        Objetivo(String nombre, double ajuste, double proteinaPorKilo) {
            this.nombre = nombre;
            this.ajuste = ajuste;
            this.proteinaPorKilo = proteinaPorKilo;
        }

        /** Acepta el nombre completo ("Volumen muscular") o una palabra clave ("volumen"), con o sin tilde. */
        static Objetivo desde(String texto) {
            if (texto == null || texto.isBlank()) {
                return MANTENER;
            }
            String clave = Normalizer.normalize(texto, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
            for (Objetivo o : values()) {
                if (clave.contains(o.name().toLowerCase(Locale.ROOT))) {
                    return o;
                }
            }
            throw new IllegalArgumentException("Objetivo no válido: " + texto
                    + ". Usa Bajar de peso, Mantener peso, Subir de peso, Volumen muscular, Definición, Fuerza, Recomposición corporal, Resistencia muscular o Tonificación.");
        }
    }
}

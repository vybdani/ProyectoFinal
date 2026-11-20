package ni.edu.uam.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Datos para calcular calorías y macros (cuerpo JSON de POST /api/calculo/macros).
 * Los rangos son los mismos que valida la pantalla de perfil de la app de escritorio.
 */
public record CalculoMacrosRequest(
        @NotNull(message = "El peso es obligatorio")
        @DecimalMin(value = "30", message = "El peso debe estar entre 30 y 300 kg")
        @DecimalMax(value = "300", message = "El peso debe estar entre 30 y 300 kg")
        Double pesoKg,

        @NotNull(message = "La altura es obligatoria")
        @DecimalMin(value = "120", message = "La altura debe estar entre 120 y 230 cm")
        @DecimalMax(value = "230", message = "La altura debe estar entre 120 y 230 cm")
        Double alturaCm,

        @NotNull(message = "La edad es obligatoria")
        @Min(value = 18, message = "La edad debe estar entre 18 y 100 años")
        @Max(value = 100, message = "La edad debe estar entre 18 y 100 años")
        Integer edad,

        @NotBlank(message = "El sexo es obligatorio")
        @Pattern(regexp = "(?i)masculino|femenino", message = "El sexo debe ser Masculino o Femenino")
        String sexo,

        @NotBlank(message = "El nivel de actividad es obligatorio")
        @Pattern(regexp = "(?i)sedentario|ligero|moderado|alto",
                message = "El nivel de actividad debe ser Sedentario, Ligero, Moderado o Alto")
        String nivelActividad,

        /** Bajar de peso, Mantener peso, Subir de peso, Volumen muscular o Definición (vacío = mantener). */
        String objetivo,

        /** Opcional: si se envía (3-60 %) se usa la fórmula Katch-McArdle. */
        @DecimalMin(value = "3", message = "El % de grasa debe estar entre 3 y 60")
        @DecimalMax(value = "60", message = "El % de grasa debe estar entre 3 y 60")
        Double porcentajeGrasa
) {
}

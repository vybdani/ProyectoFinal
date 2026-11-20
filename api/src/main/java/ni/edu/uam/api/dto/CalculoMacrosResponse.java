package ni.edu.uam.api.dto;

/** Resultado de POST /api/calculo/macros. */
public record CalculoMacrosResponse(
        String objetivo,
        String formula,
        double tmb,
        double factorActividad,
        double caloriasMantenimiento,
        double caloriasObjetivo,
        double proteinasG,
        double carbohidratosG,
        double grasasG
) {
}

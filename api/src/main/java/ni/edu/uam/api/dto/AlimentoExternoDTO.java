package ni.edu.uam.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para transferir información de alimentos obtenidos
 * desde la API externa de internet (Open Food Facts) hacia la app de escritorio.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlimentoExternoDTO {
    private String id;
    private String nombre;
    private String marca;
    private Double caloriasPor100g;
    private Double proteinasPor100g;
    private Double carbohidratosPor100g;
    private Double grasasPor100g;
    private String categoria;
    private String imagenUrl;
    private String fuente;
}

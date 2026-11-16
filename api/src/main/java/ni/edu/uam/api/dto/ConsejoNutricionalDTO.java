package ni.edu.uam.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para transferir consejos y pautas nutricionales según
 * el objetivo del usuario (pérdida de grasa, mantenimiento, volumen/gimnasio).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsejoNutricionalDTO {
    private String objetivo;
    private String titulo;
    private String consejo;
    private String distribucionMacrosSugerida;
    private String recomendacionAgua;
    private String fuente;
}

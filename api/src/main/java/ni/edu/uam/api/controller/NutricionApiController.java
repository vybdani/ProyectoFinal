package ni.edu.uam.api.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ni.edu.uam.api.dto.AlimentoExternoDTO;
import ni.edu.uam.api.dto.CalculoMacrosRequest;
import ni.edu.uam.api.dto.CalculoMacrosResponse;
import ni.edu.uam.api.dto.ConsejoNutricionalDTO;
import ni.edu.uam.api.service.CalculoMacrosService;
import ni.edu.uam.api.service.OpenFoodFactsService;
import ni.edu.uam.api.service.RecomendacionService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Endpoints REST que consume la app de escritorio de Solea.
 *
 * <pre>
 * GET  /api/status                               estado del servicio
 * GET  /api/alimentos/buscar?q=avena&limite=8    alimentos de Open Food Facts (macros por 100 g)
 * GET  /api/consejos?objetivo=Volumen muscular   recomendación según el objetivo
 * POST /api/calculo/macros   (JSON)              calorías y macros, mismas fórmulas que la app
 * </pre>
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@Validated
public class NutricionApiController {

    private final OpenFoodFactsService openFoodFactsService;
    private final RecomendacionService recomendacionService;
    private final CalculoMacrosService calculoMacrosService;

    public NutricionApiController(OpenFoodFactsService openFoodFactsService,
                                  RecomendacionService recomendacionService,
                                  CalculoMacrosService calculoMacrosService) {
        this.openFoodFactsService = openFoodFactsService;
        this.recomendacionService = recomendacionService;
        this.calculoMacrosService = calculoMacrosService;
    }

    @GetMapping("/status")
    public Map<String, Object> estado() {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("estado", "ACTIVO");
        respuesta.put("servicio", "API REST Solea");
        respuesta.put("version", "1.1.0");
        respuesta.put("fuenteExterna", OpenFoodFactsService.FUENTE_RED);
        respuesta.put("hora", Instant.now().toString());
        return respuesta;
    }

    @GetMapping("/alimentos/buscar")
    public List<AlimentoExternoDTO> buscarAlimentos(
            @RequestParam(name = "q")
            @NotBlank(message = "Escribe el alimento que quieres buscar")
            @Size(min = 2, max = 60, message = "La búsqueda debe tener entre 2 y 60 caracteres")
            String criterio,
            @RequestParam(name = "limite", defaultValue = "8")
            @Min(value = 1, message = "El límite debe estar entre 1 y 20")
            @Max(value = 20, message = "El límite debe estar entre 1 y 20")
            int limite) {
        return openFoodFactsService.buscarAlimentos(criterio, limite);
    }

    @GetMapping("/consejos")
    public ConsejoNutricionalDTO obtenerConsejos(
            @RequestParam(name = "objetivo", defaultValue = "mantener")
            @Size(max = 60, message = "El objetivo admite como máximo 60 caracteres")
            String objetivo) {
        return recomendacionService.obtenerConsejoPorObjetivo(objetivo);
    }

    @PostMapping("/calculo/macros")
    public CalculoMacrosResponse calcularMacros(@Valid @RequestBody CalculoMacrosRequest datos) {
        return calculoMacrosService.calcular(datos);
    }
}

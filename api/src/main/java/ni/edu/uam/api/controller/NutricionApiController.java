package ni.edu.uam.api.controller;

import ni.edu.uam.api.dto.AlimentoExternoDTO;
import ni.edu.uam.api.dto.ConsejoNutricionalDTO;
import ni.edu.uam.api.service.OpenFoodFactsService;
import ni.edu.uam.api.service.RecomendacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST principal de la API de NutriFit.
 * Expone endpoints consumidos por la aplicación de escritorio JavaFX.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class NutricionApiController {

    private final OpenFoodFactsService openFoodFactsService;
    private final RecomendacionService recomendacionService;

    public NutricionApiController(OpenFoodFactsService openFoodFactsService, RecomendacionService recomendacionService) {
        this.openFoodFactsService = openFoodFactsService;
        this.recomendacionService = recomendacionService;
    }

    /**
     * Endpoint de estado y comprobación de salud de la API.
     * GET http://localhost:8080/api/status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> estado() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("estado", "ACTIVO");
        resp.put("servicio", "API REST NutriFit");
        resp.put("version", "1.0.0");
        resp.put("fuente_externa", "Open Food Facts API Conectada");
        return ResponseEntity.ok(resp);
    }

    /**
     * Endpoint para buscar alimentos y sus macronutrientes en internet.
     * GET http://localhost:8080/api/alimentos/buscar?q=avena
     */
    @GetMapping("/alimentos/buscar")
    public ResponseEntity<List<AlimentoExternoDTO>> buscarAlimento(@RequestParam(name = "q", defaultValue = "avena") String criterio) {
        List<AlimentoExternoDTO> alimentos = openFoodFactsService.buscarAlimentos(criterio);
        return ResponseEntity.ok(alimentos);
    }

    /**
     * Endpoint para obtener recomendaciones nutricionales y distribución de macros según objetivo.
     * GET http://localhost:8080/api/consejos?objetivo=volumen
     */
    @GetMapping("/consejos")
    public ResponseEntity<ConsejoNutricionalDTO> obtenerConsejos(@RequestParam(name = "objetivo", defaultValue = "mantener") String objetivo) {
        ConsejoNutricionalDTO consejo = recomendacionService.obtenerConsejoPorObjetivo(objetivo);
        return ResponseEntity.ok(consejo);
    }

    /**
     * Endpoint de cálculo nutricional instantáneo en la nube / backend.
     * POST http://localhost:8080/api/calculo/macros
     */
    @PostMapping("/calculo/macros")
    public ResponseEntity<Map<String, Object>> calcularMacros(
            @RequestParam double pesoKg,
            @RequestParam double alturaCm,
            @RequestParam int edad,
            @RequestParam String sexo,
            @RequestParam String objetivo) {

        // Fórmula Harris-Benedict revisada (Mifflin-St Jeor)
        double tmb;
        if ("F".equalsIgnoreCase(sexo) || "Femenino".equalsIgnoreCase(sexo)) {
            tmb = (10 * pesoKg) + (6.25 * alturaCm) - (5 * edad) - 161;
        } else {
            tmb = (10 * pesoKg) + (6.25 * alturaCm) - (5 * edad) + 5;
        }

        double gastoTotal = tmb * 1.55; // Factor actividad moderada por defecto
        double caloriasObjetivo = gastoTotal;

        if (objetivo.toLowerCase().contains("bajar") || objetivo.toLowerCase().contains("definicion")) {
            caloriasObjetivo -= 400;
        } else if (objetivo.toLowerCase().contains("volumen") || objetivo.toLowerCase().contains("subir")) {
            caloriasObjetivo += 350;
        }

        // Distribución en gramos
        double proteinaGramos = pesoKg * 2.0; // 2g por kg
        double grasaGramos = pesoKg * 0.9;    // 0.9g por kg
        double caloriasRestantes = caloriasObjetivo - ((proteinaGramos * 4) + (grasaGramos * 9));
        double carbohidratosGramos = Math.max(50.0, caloriasRestantes / 4.0);

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("calorias_mantenimiento", Math.round(gastoTotal));
        resultado.put("calorias_objetivo", Math.round(caloriasObjetivo));
        resultado.put("proteinas_g", Math.round(proteinaGramos));
        resultado.put("carbohidratos_g", Math.round(carbohidratosGramos));
        resultado.put("grasas_g", Math.round(grasaGramos));
        resultado.put("objetivo", objetivo);

        return ResponseEntity.ok(resultado);
    }
}

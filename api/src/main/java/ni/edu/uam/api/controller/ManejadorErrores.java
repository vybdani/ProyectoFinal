package ni.edu.uam.api.controller;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Convierte los errores en respuestas JSON claras, con el mismo formato siempre:
 * <pre>{ "estado": 400, "error": "Datos inválidos", "detalles": ["El peso debe estar entre 30 y 300 kg"] }</pre>
 */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    /** @Valid sobre un cuerpo JSON (por ejemplo POST /api/calculo/macros). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> cuerpoInvalido(MethodArgumentNotValidException e) {
        List<String> detalles = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .distinct()
                .toList();
        return respuesta(HttpStatus.BAD_REQUEST, "Datos inválidos", detalles);
    }

    /** Validaciones sobre parámetros de la URL (?q=, ?limite=). */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, Object>> parametrosInvalidos(HandlerMethodValidationException e) {
        List<String> detalles = e.getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .distinct()
                .toList();
        return respuesta(HttpStatus.BAD_REQUEST, "Parámetros inválidos", detalles);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> restriccion(ConstraintViolationException e) {
        List<String> detalles = e.getConstraintViolations().stream()
                .map(v -> v.getMessage())
                .distinct()
                .toList();
        return respuesta(HttpStatus.BAD_REQUEST, "Parámetros inválidos", detalles);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> faltaParametro(MissingServletRequestParameterException e) {
        return respuesta(HttpStatus.BAD_REQUEST, "Falta un parámetro",
                List.of("Falta el parámetro '" + e.getParameterName() + "'"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> tipoIncorrecto(MethodArgumentTypeMismatchException e) {
        return respuesta(HttpStatus.BAD_REQUEST, "Parámetro con formato incorrecto",
                List.of("El parámetro '" + e.getName() + "' tiene un valor no válido"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> jsonIlegible(HttpMessageNotReadableException e) {
        return respuesta(HttpStatus.BAD_REQUEST, "JSON inválido",
                List.of("El cuerpo de la petición no es un JSON válido o tiene tipos incorrectos"));
    }

    /** Reglas del negocio, por ejemplo un objetivo que no existe. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> reglaDeNegocio(IllegalArgumentException e) {
        return respuesta(HttpStatus.BAD_REQUEST, "Solicitud no válida", List.of(e.getMessage()));
    }

    /** Cualquier otro error: se registra y se responde sin exponer detalles internos. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> inesperado(Exception e) {
        log.error("Error inesperado en la API", e);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                List.of("Ocurrió un error inesperado. Inténtalo de nuevo."));
    }

    private static ResponseEntity<Map<String, Object>> respuesta(HttpStatus estado, String error, List<String> detalles) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("estado", estado.value());
        cuerpo.put("error", error);
        cuerpo.put("detalles", detalles);
        cuerpo.put("hora", Instant.now().toString());
        return ResponseEntity.status(estado).body(cuerpo);
    }
}

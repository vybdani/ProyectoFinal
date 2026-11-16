package ni.edu.uam.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * Clase principal de inicio para la API REST de NutriFit.
 * Para ejecutar el servidor backend:
 * 1. Abrir esta clase y pulsar Run en IntelliJ, o
 * 2. Ejecutar 'mvn spring-boot:run' dentro del directorio /api.
 */
@SpringBootApplication
public class ApiNutrifitApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiNutrifitApplication.class, args);
    }

    /**
     * Cliente HTTP RestTemplate para consumir la API externa de internet (Open Food Facts).
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

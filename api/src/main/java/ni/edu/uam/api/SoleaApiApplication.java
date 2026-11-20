package ni.edu.uam.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API REST de Solea.
 *
 * <p>Para ejecutarla: abrir esta clase en IntelliJ y pulsar Run, o
 * {@code mvn spring-boot:run} dentro de la carpeta /api. Queda en http://localhost:8080.</p>
 */
@SpringBootApplication
public class SoleaApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SoleaApiApplication.class, args);
    }
}

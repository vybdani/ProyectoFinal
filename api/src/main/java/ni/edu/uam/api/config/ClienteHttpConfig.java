package ni.edu.uam.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Cliente HTTP para Open Food Facts con tiempos máximos de espera
 * (sin ellos una petición lenta dejaría colgado el servidor) y el User-Agent que pide el servicio.
 */
@Configuration
public class ClienteHttpConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder,
                                     @Value("${app.openfoodfacts.user-agent}") String userAgent,
                                     @Value("${app.openfoodfacts.timeout-conexion-ms:3000}") long timeoutConexion,
                                     @Value("${app.openfoodfacts.timeout-lectura-ms:8000}") long timeoutLectura) {
        return builder
                .connectTimeout(Duration.ofMillis(timeoutConexion))
                .readTimeout(Duration.ofMillis(timeoutLectura))
                .defaultHeader(HttpHeaders.USER_AGENT, userAgent)
                .build();
    }
}

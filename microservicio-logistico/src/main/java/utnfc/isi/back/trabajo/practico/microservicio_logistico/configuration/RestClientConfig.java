package utnfc.isi.back.trabajo.practico.microservicio_logistico.configuration;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Configuration
public class RestClientConfig {
    private static final String HEADER_NAME = "X-Correlation-ID";
    @Bean
    RestClient restClient(@Value("${api.microservicio.comercial.base-url}") String baseUrl) {

        return RestClient.builder()
            .baseUrl(baseUrl)
            .requestInterceptor((request, body, execution) -> {

                // 1) Obtener el JWT del contexto de Spring Security
                var auth = SecurityContextHolder.getContext().getAuthentication();

                if (auth instanceof JwtAuthenticationToken jwtAuth) {
                    String token = jwtAuth.getToken().getTokenValue();

                    // 2) Agregar el header Authorization automáticamente
                    request.getHeaders().setBearerAuth(token);
                }

                return execution.execute(request, body);
            })
            .build();
    }
}

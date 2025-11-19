package utnfc.isi.back.trabajo.practico.microservicio_comercial.configuration;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class RestClientConfig {

    private static final String HEADER_NAME = "X-Correlation-ID";
    @Bean
    RestClient restClient(
            RestClient.Builder builder,
            @Value("${api.microservicio.logistico.base-url}") String baseUrl
    ) {
        return builder
            .requestInterceptor((request, body, execution) -> {

                var auth = SecurityContextHolder.getContext().getAuthentication();

                if (auth instanceof JwtAuthenticationToken jwtAuth) {
                    String token = jwtAuth.getToken().getTokenValue();
                    request.getHeaders().setBearerAuth(token);
                }

                return execution.execute(request, body);
            })
            .baseUrl(baseUrl)
            .requestInterceptor((request, body, execution) -> {
                String cid = MDC.get("correlationId"); // toma el CID del filtro
                if (cid != null) {
                    request.getHeaders().add(HEADER_NAME, cid); // agrega header
                }
                return execution.execute(request, body);
            })
            .build();
    }
}


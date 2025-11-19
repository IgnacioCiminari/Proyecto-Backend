package utnfc.isi.back.trabajo.practico.api_gateway.config;

import org.slf4j.Logger;              
import org.slf4j.LoggerFactory;        
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GWConfig {

    private static final Logger log = LoggerFactory.getLogger(GWConfig.class);

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {

        log.info("Configurando rutas del API Gateway..."); 

        return builder.routes()
            .route("comercial", r -> {
                log.info("Ruta configurada: /comercial/**  →  MS-Comercial (8081)"); 
                return r.path("/comercial/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://microservicio-comercial:8081");
            })
            .route("logistico", r -> {
                log.info("Ruta configurada: /logistico/**  →  MS-Logístico (8082)"); 
                return r.path("/logistico/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://microservicio-logistico:8082");
            })
            .build();
    }
}

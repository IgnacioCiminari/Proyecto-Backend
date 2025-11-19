package utnfc.isi.back.trabajo.practico.api_gateway.config;

import org.slf4j.Logger;           
import org.slf4j.LoggerFactory;     
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationIdGatewayFilter implements GlobalFilter {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdGatewayFilter.class);

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);

        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
            log.info("Generando nuevo Correlation-ID: {}", correlationId);
        } else {
            log.info("Correlation-ID recibido: {}", correlationId);
        }

        MDC.put("correlationId", correlationId);

        log.info("Pasando request a microservicio destino: {} {}",
            exchange.getRequest().getMethod(),
            exchange.getRequest().getURI()
        );

        ServerWebExchange newExchange = exchange.mutate()
            .request(
                exchange.getRequest().mutate()
                    .header(CORRELATION_ID_HEADER, correlationId)
                    .build()
            )
            .build();

        return chain.filter(newExchange)
            .doFinally(signal -> {
                log.info("Respuesta enviada desde API Gateway");
                MDC.clear();
            });
    }
}

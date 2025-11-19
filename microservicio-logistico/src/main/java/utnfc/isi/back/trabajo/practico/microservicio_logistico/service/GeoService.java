package utnfc.isi.back.trabajo.practico.microservicio_logistico.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.dto.DistanciaDTO;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class GeoService {

    private static final Logger log = LoggerFactory.getLogger(GeoService.class);

    private final WebClient webClient;

    public GeoService(@Value("${api.geoservice.base-url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public DistanciaDTO obtenerDistanciaDuracion(double origenLong,
     double origenLat, double destinoLong, double destinoLat) {

        log.info("Calculando distancia entre coordenadas...");

        String uri = String.format(
            Locale.US,
            "/route/v1/driving/%f,%f;%f,%f?overview=false",
            origenLong, origenLat, destinoLong, destinoLat
        );

        Map<String, Object> response = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        if (response != null && "Ok".equals(response.get("code"))) {
            List<Map<String, Object>> routes = (List<Map<String, Object>>) response.get("routes");

            if (routes != null && !routes.isEmpty()) {
                Map<String, Object> firstRoute = routes.get(0);
                Double distanciaMetros = ((Number) firstRoute.get("distance")).doubleValue();
                Integer duracionSegundos = ((Number) firstRoute.get("duration")).intValue();

                Double kilometros = Math.round(distanciaMetros / 10.0) / 100.0; // redondeo a 2 decimales

                log.info("Distancia calculada con éxito");
                return new DistanciaDTO(kilometros, duracionSegundos);
            }
        }
        log.warn("No se pudo calcular la distancia. Se devuelve 0.");
        return new DistanciaDTO(0.0, 0);
    }
}

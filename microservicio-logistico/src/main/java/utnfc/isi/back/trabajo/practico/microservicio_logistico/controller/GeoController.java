package utnfc.isi.back.trabajo.practico.microservicio_logistico.controller;

import lombok.RequiredArgsConstructor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.dto.DistanciaDTO;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.service.GeoService;

@RestController
@RequestMapping("/api/distancia")
@RequiredArgsConstructor
public class GeoController {

    private static final Logger log = LoggerFactory.getLogger(GeoController.class);

    private final GeoService geoService;

    @GetMapping
    public ResponseEntity<DistanciaDTO> obtenerDistancia(
            @RequestParam double origenLat,
            @RequestParam double origenLong,
            @RequestParam double destinoLat,
            @RequestParam double destinoLong
    ) {
        log.info("GET /api/distancia buscando distancia...");
        return ResponseEntity.status(200).body(geoService.obtenerDistanciaDuracion(origenLong, origenLat, destinoLong, destinoLat));
    }
}

package utnfc.isi.back.trabajo.practico.microservicio_logistico.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.dto.CamionDTO;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.model.Camion;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.security.TransportistaSecurity;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.service.CamionService;

@RestController
@RequestMapping("/camiones")
@Validated
public class CamionController {

    private static final Logger log = LoggerFactory.getLogger(CamionController.class);

    private final CamionService camionService;
    private final TransportistaSecurity transportistaSecurity;

    CamionController(CamionService camionService, TransportistaSecurity transportistaSecurity) {
        this.camionService = camionService;
        this.transportistaSecurity = transportistaSecurity;
    }

    // GET: obtener un camión por dominio
    @GetMapping("/{dominio}")
    public ResponseEntity<Camion> obtenerCamionPorId(
        @PathVariable @NotBlank String dominio
    ) {
        log.info("GET /camiones/{} - solicitando camion", dominio);
        return ResponseEntity.status(200).body(camionService.findById(dominio));
    }

    // GET: obtener camiones por transportista
    @GetMapping("/por-transportista/{idTransportista}")
    public ResponseEntity<Page<Camion>> obtenerCamionesPorTransportista(
        @PathVariable @Positive Long idTransportista,
        @RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
        @RequestParam(defaultValue = "10") @Positive int tamañoPagina
    ) {
        log.info("GET /camiones/por-transportista/{}", idTransportista);
        transportistaSecurity.validarId(idTransportista);

        log.info("Camiones encontrados para transportista {}", idTransportista);
        return ResponseEntity.status(200).body(camionService.obtenerPorTransportista(idTransportista, pagina, tamañoPagina));
    }

    // Get: obtener camiones disponibles por capacidad
    @GetMapping("/disponibles")
    public ResponseEntity<Page<Camion>> obtenerCamionesDisponiblesPorCapacidad(
        @RequestParam(required = true) @Positive Integer capacidad,
        @RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
        @RequestParam(defaultValue = "10") @Positive int tamañoPagina
    ) {
        log.info("Buscando Camiones Disponibles por capacidad...");
        return ResponseEntity.status(200).body(camionService.obtenerDisponiblesPorCapacidad(capacidad, pagina, tamañoPagina));
    }

    // POST: crear nuevo camión
    @PostMapping("")
    public ResponseEntity<Camion> crearCamion(
        @Valid @RequestBody CamionDTO camionDTO
    ) {
        log.info("POST /camiones - creando camion dominio={} transportista={}", 
                 camionDTO.getDominio(), camionDTO.getIdTransportista());
        return ResponseEntity.status(201).body(camionService.crear(camionDTO));
    }

    // DELETE: eliminar camión (físico)
    @DeleteMapping("/{idCamion}")
    public ResponseEntity<Camion> eliminarCamion(@PathVariable @NotBlank String idCamion) {
       log.warn("DELETE /camiones/{} - eliminando camion", idCamion);

        Camion camion = camionService.eliminar(idCamion);

        log.info("Camion {} eliminado correctamente", idCamion);

        return ResponseEntity.status(200).body(camion);
    }
}

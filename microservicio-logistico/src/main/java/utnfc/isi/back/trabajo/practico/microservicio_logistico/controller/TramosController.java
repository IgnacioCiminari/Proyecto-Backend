package utnfc.isi.back.trabajo.practico.microservicio_logistico.controller;

import java.util.List;

import org.slf4j.Logger;                      
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.common.lang.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.model.Tramo;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.service.TramoService;

@RestController
@RequestMapping("/tramos")
@Validated
public class TramosController {

    private static final Logger log = LoggerFactory.getLogger(TramosController.class);

    private final TramoService tramoService;

    TramosController(TramoService tramoService){
        this.tramoService = tramoService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tramo> getTramoById(
        @PathVariable @Positive Long id
    ) {
        log.info("[GET /tramos/{}] buscando tramo...", id);
        Tramo tramo = tramoService.findById(id);

        log.info("Tramo {} encontrado con éxito", id);
        return ResponseEntity.status(200).body(tramo);
    }

    @GetMapping("/ruta/{rutaId}")
    public ResponseEntity<List<Tramo>> getTramosByRuta(
        @PathVariable @Positive Long rutaId,
        @RequestParam(required = false) @Nullable Tramo.EstadoTramo estado
    ) {
        log.info("[GET /tramos/ruta/{}] buscando tramos...", rutaId);
        List<Tramo> tramos = tramoService.obtenerTramosPorRuta(rutaId, estado);

        log.info("Tramos de ruta {} obtenidos con éxito", rutaId);
        return ResponseEntity.status(200).body(tramos);
    }

    @GetMapping("/camion/{dominio}")
    public ResponseEntity<Page<Tramo>> getTramosByCamion(
        @PathVariable @NotBlank String dominio,
        @RequestParam(required = false) Tramo.EstadoTramo estado, 
        @RequestParam(required = false, defaultValue = "0") @PositiveOrZero int pag,
        @RequestParam(required = false, defaultValue = "10") @Positive int tamPag
    ) {
        log.info("[GET /tramos/camion/{}] buscando tramos por camion...", dominio);
        Page<Tramo> tramos = tramoService.findTramosByDominioPage(dominio, estado, pag, tamPag);

        log.info("Tramos de camión {} obtenidos con éxito", dominio);
        return ResponseEntity.status(200).body(tramos);
    } 


    @PutMapping("/{idTramo}/asignar-camion/{dominio}")
    public ResponseEntity<Tramo> asignarCamionATramo(
        @PathVariable @Positive Long idTramo,
        @PathVariable @NotBlank String dominio
    ) {
        log.info("[PUT /tramos/{}/asignar-camion/{}] asignando camión...", idTramo, dominio);
        Tramo tramo = tramoService.asignarCamionATramo(idTramo, dominio);

        log.info("Camión {} asignado al tramo {}", dominio, idTramo);
        return ResponseEntity.status(200).body(tramo);
    }


    @PutMapping("/{idTramo}/quitar-camion")
    public ResponseEntity<Tramo> quitarCamionATramo(
        @PathVariable @Positive Long idTramo
    ) {
        log.info("[PUT /tramos/{}/quitar-camion] quitando camión...", idTramo);
        Tramo tramo = tramoService.quitarCamionATramo(idTramo);

        log.info("Camión quitado del tramo {}", idTramo);
        return ResponseEntity.status(200).body(tramo);
    }



    @PutMapping("/{idTramo}/iniciar")
    public ResponseEntity<Tramo> iniciarTramo(
        @PathVariable @Positive Long idTramo
    ) {
        log.info("[PUT /tramos/{}/iniciar] iniciando tramo...", idTramo);
        Tramo tramo = tramoService.iniciarTramo(idTramo);

        log.info("Tramo {} iniciado con éxito", idTramo);
        return ResponseEntity.status(200).body(tramo);
    }

    @PutMapping("/{idTramo}/finalizar")
    public ResponseEntity<Tramo> finalizarTramo(
        @PathVariable @Positive Long idTramo
    ) {
        log.info("[PUT /tramos/{}/finalizar] finalizando tramo...", idTramo);
        Tramo tramo = tramoService.finalizarTramo(idTramo);

        log.info("Tramo {} finalizado con éxito", idTramo);
        return ResponseEntity.status(200).body(tramo);
    }
}

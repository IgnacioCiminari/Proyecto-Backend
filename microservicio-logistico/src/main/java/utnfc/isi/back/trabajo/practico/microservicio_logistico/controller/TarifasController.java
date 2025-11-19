package utnfc.isi.back.trabajo.practico.microservicio_logistico.controller;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.dto.TarifaDTO;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.model.Tarifa;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.service.TarifasService;


@RestController
@RequestMapping("/tarifas")
@Validated
@Slf4j
public class TarifasController {
    private final TarifasService tarifasService;

    public TarifasController(TarifasService tarifasService){
        this.tarifasService = tarifasService;
    }

    @GetMapping("/{idTarifa}")
    public ResponseEntity<Tarifa> obtenerTarifaPorId(
        @PathVariable @Positive Long idTarifa
    ){
        log.info("GET /tarifas/{} - buscando tarifa...", idTarifa);
        Tarifa tarifa = tarifasService.findById(idTarifa);

        log.info("Tarifa {} encontrada con éxito", idTarifa);
        return ResponseEntity.status(200).body(tarifa);
    }

    @GetMapping("/todas")
    public ResponseEntity<Page<Tarifa>> obtenerTodasLasTarifas(
        @RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
        @RequestParam(defaultValue = "10") @Positive int tamañoPagina
    ) {
        log.info("GET /tarifas/todas - obteniendo todas las tarifas...");
        Page<Tarifa> tarifas = tarifasService.ObtenerTodas(pagina, tamañoPagina);

        log.info("Tarifas obtenidas correctamente");
        return ResponseEntity.status(200).body(tarifas);
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<Tarifa>> obtenerTarifasDisponibles(
        @RequestParam(required = false) @Positive Double capacidadCarga
    ) {
        log.info("GET /tarifas/disponibles - buscando tarifas disponibles...");
        List<Tarifa> tarifas = tarifasService.findAllAvailable(capacidadCarga);

        log.info("Tarifas disponibles obtenidas con éxito");
        return ResponseEntity.status(200).body(tarifas);
    }

    @PostMapping("")
    public ResponseEntity<Tarifa> crearTarifa(
        @Valid @RequestBody TarifaDTO data
    ) {
        log.info("POST /tarifas - creando nueva tarifa...");
        Tarifa tarifa = tarifasService.crear(data);

        log.info("Tarifa creada con éxito con ID {}", tarifa.getIdTarifa());
        return ResponseEntity.status(201).body(tarifa);
    }

    @DeleteMapping("/{idTarifa}")
    public ResponseEntity<Tarifa> eliminarTarifa(
        @PathVariable @Positive Long idTarifa
    ) {
        log.info("DELETE /tarifas/{} - eliminando tarifa...", idTarifa);
        Tarifa tarifa = tarifasService.Eliminar(idTarifa);

        log.info("Tarifa {} eliminada (lógica) con éxito", idTarifa);
        return ResponseEntity.status(200).body(tarifa);
    }
}

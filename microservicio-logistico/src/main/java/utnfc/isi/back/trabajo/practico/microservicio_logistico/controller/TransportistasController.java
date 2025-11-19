package utnfc.isi.back.trabajo.practico.microservicio_logistico.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.dto.TransportistaDTO;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.model.Transportista;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.service.TransportistasService;

@RestController
@RequestMapping("/transportistas")
@Validated
public class TransportistasController {

    private static final Logger log = LoggerFactory.getLogger(TransportistasController.class);

    private final TransportistasService transportistasService;

    public TransportistasController(TransportistasService transportistasService) {
        this.transportistasService = transportistasService;
    }

    // GET por ID
    @GetMapping("/{idTransportista}")
    public ResponseEntity<Transportista> obtenerTransportistaPorId(
        @PathVariable @Positive Long idTransportista
    ) {
        log.info("[GET /transportistas/{}] Buscando transportista...", idTransportista);
        Transportista transportista = transportistasService.findById(idTransportista);

        log.info("Transportista {} encontrado con éxito", idTransportista);
        return ResponseEntity.ok(transportista);
    }

    // GET todos
    @GetMapping("/todos")
    public ResponseEntity<Page<Transportista>> obtenerTodosLosTransportistas(
        @RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
        @RequestParam(defaultValue = "10") @Positive int tamañoPagina
    ) {
        log.info("[GET /transportistas/todos] Buscando todos los transportistas...");
        Page<Transportista> transportistas = transportistasService.obtenerTodos(pagina, tamañoPagina);
        
        log.info("Listado de transportistas obtenido con éxito");
        return ResponseEntity.ok(transportistas);
    }

    // POST crear
    @PostMapping("")
    public ResponseEntity<Transportista> crearTransportista(
        @Valid @RequestBody TransportistaDTO dto
    ) {
        log.info("[POST /transportistas] Creando transportista...");
        Transportista nuevo = transportistasService.crear(dto);

        log.info("Transportista creado con éxito (ID: {})", nuevo.getIdTransportista());
        return ResponseEntity.status(201).body(nuevo);
    }

    // PUT actualizar
    @PutMapping("/{idTransportista}")
    public ResponseEntity<Transportista> actualizarTransportista(
        @PathVariable @Positive Long idTransportista,
        @Valid @RequestBody TransportistaDTO dto
    ) {
        log.info("[PUT /transportistas/{}] Actualizando transportista...", idTransportista);
        Transportista actualizado = transportistasService.actualizar(idTransportista, dto);

        log.info("Transportista {} actualizado con éxito", idTransportista);
        return ResponseEntity.ok(actualizado);
    }

    // DELETE eliminar (por ahora física)
    @DeleteMapping("/{idTransportista}")
    public ResponseEntity<Transportista> eliminarTransportista(
        @PathVariable @Positive Long idTransportista
    ) {
        log.warn("[DELETE /transportistas/{}] Eliminando transportista...", idTransportista);
        Transportista eliminado = transportistasService.eliminar(idTransportista);

        log.info("Transportista {} eliminado con éxito", idTransportista);
        return ResponseEntity.ok(eliminado);
    }
}

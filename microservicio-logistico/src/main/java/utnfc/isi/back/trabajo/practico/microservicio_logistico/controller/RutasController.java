package utnfc.isi.back.trabajo.practico.microservicio_logistico.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Positive;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.dto.TramoDTO;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.dto.UbicacionDTO;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.model.Ruta;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.model.Tramo;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.service.RutaService;

@RestController
@RequestMapping("/rutas")
@Validated
public class RutasController {

    private static final Logger log = LoggerFactory.getLogger(RutasController.class);
    
    private final RutaService rutaService;

    RutasController(RutaService rutaService) {
        this.rutaService = rutaService;
    }

    @GetMapping("/health-check")
    public ResponseEntity<String> healthCheck() {
        log.info("GET /rutas/health-check comprobando estado del servicio...");
        return ResponseEntity.ok("Service is up and running");
    }

    @GetMapping("/prueba")
    public ResponseEntity<String> rutaPrueba() {
        log.info("GET /rutas/prueba ejecutando ruta de prueba...");
        String prueba = rutaService.rutaDePrueba();
        log.info("Ruta de prueba ejecutada con éxito");
        return ResponseEntity.ok(prueba);
    }


    @GetMapping("/{idRuta}")
    public ResponseEntity<Ruta> obtenerPorId(
        @PathVariable @Positive Long idRuta
    ) {
        log.info("GET /rutas/{} buscando ruta...", idRuta);
        Ruta ruta = rutaService.findById(idRuta);
        log.info("Ruta {} encontrada con éxito", idRuta);
        return ResponseEntity.status(200).body(ruta);
    }

    @GetMapping("/solicitud/{idSolicitud}")
    public ResponseEntity<Ruta> obtenerPorIdSolicitud(
        @PathVariable @Positive Long idSolicitud
    ) {
        log.info("GET /rutas/solicitud/{} buscando ruta por solicitud...", idSolicitud);
        Ruta ruta = rutaService.findByIdSolicitud(idSolicitud);
        log.info("Ruta de solicitud {} encontrada con éxito", idSolicitud);
        return ResponseEntity.status(200).body(ruta);
    }

    @GetMapping("/ubicacion/{idSolicitud}")
    public ResponseEntity<UbicacionDTO> obtenerUbicacionPorIdSolicitud(
        @PathVariable @Positive Long idSolicitud
    ) {
        log.info("GET /rutas/ubicacion/{} buscando ubicación...", idSolicitud);
        UbicacionDTO ubicacion = rutaService.obtenerUbicacionPorIdSolicitud(idSolicitud);

        log.info("Ubicación obtenida con éxito para solicitud {}", idSolicitud);
        return ResponseEntity.status(200).body(ubicacion);
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<Ruta>> obtenerRutasPendientes() {

        log.info("GET /rutas/pendientes buscando rutas pendientes...");
        List<Ruta> rutasPendientes = rutaService.obtenerRutasPendientes();

        log.info("Rutas pendientes recuperadas con éxito");
        return ResponseEntity.status(200).body(rutasPendientes);
    }

    @GetMapping("/alternativas/{idRuta}")
    public ResponseEntity<List<List<Tramo>>> obtenerRutasAlternativas(
        @PathVariable @Positive Long idRuta,
        @RequestParam(defaultValue = "3") @Positive int cantidadRutas
    ) {
        log.info("GET /rutas/alternativas/{} generando rutas alternativas...", idRuta);
        List<List<Tramo>> rutasAlternativas = rutaService.crearRutasAlternativos(idRuta, cantidadRutas);
        
        log.info("Rutas alternativas generadas con éxito para {}", idRuta);
        return ResponseEntity.status(200).body(rutasAlternativas);
    }

    @PostMapping("crear/{idSolicitud}")
    public ResponseEntity<Ruta> crearRuta(
        @PathVariable @Positive Long idSolicitud,
        @RequestBody Ruta data
    ) {
        log.info("POST /rutas/crear/{} creando ruta...", idSolicitud);
        Ruta ruta = rutaService.crear(idSolicitud,data);

        log.info("Ruta creada con éxito para solicitud {}", idSolicitud);
        return ResponseEntity.status(201).body(ruta);
    }

    @PostMapping("crear/{idSolicitud}/tramos")
    public ResponseEntity<Ruta> crearTramosParaRuta(
        @PathVariable @Positive Long idSolicitud,
        @RequestBody List<TramoDTO> tramosDTO
    ) {
        log.info("POST /rutas/crear/{}/tramos creando tramos para ruta...", idSolicitud);
        Ruta ruta = rutaService.crearTramosParaRuta(idSolicitud, tramosDTO);

        log.info("Tramos creados con éxito para solicitud {}", idSolicitud);
        return ResponseEntity.status(201).body(ruta);
    }

    @DeleteMapping("/{idRuta}")
    public ResponseEntity<Ruta> eliminarRuta(
        @PathVariable @Positive Long idRuta
    ) {
        log.warn("DELETE /rutas/{} eliminando ruta...", idRuta);
        Ruta rutaEliminada = rutaService.eliminarPorId(idRuta);
        log.info("Ruta {} eliminada con éxito", idRuta);
        return ResponseEntity.status(200).body(rutaEliminada);
    }

    // Solo accesible si la ruta se cancela desde el microservicio comercial
    @DeleteMapping("/solicitud/{idSolicitud}")
    public ResponseEntity<Ruta> eliminarRutaPorIdSolicitud(
        @PathVariable @Positive Long idSolicitud
    ) {
        log.info("DELETE /rutas/solicitud/{} eliminando ruta por solicitud...", idSolicitud);
        Ruta ruta = rutaService.eliminarRutaPorIdSolicitud(idSolicitud);
        log.info("Ruta de solicitud {} eliminada con éxito", idSolicitud);
        return ResponseEntity.status(200).body(ruta);
    }
}

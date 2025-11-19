package utnfc.isi.back.trabajo.practico.microservicio_comercial.controller;

import java.net.http.HttpResponse.ResponseInfo;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import utnfc.isi.back.trabajo.practico.microservicio_comercial.dto.EntregarSolicitudDTO;
import utnfc.isi.back.trabajo.practico.microservicio_comercial.dto.PlanificarSolicitudDTO;
import utnfc.isi.back.trabajo.practico.microservicio_comercial.dto.SolicitudDTO;
import utnfc.isi.back.trabajo.practico.microservicio_comercial.dto.UbicacionDTO;
import utnfc.isi.back.trabajo.practico.microservicio_comercial.model.Contenedor;
import utnfc.isi.back.trabajo.practico.microservicio_comercial.model.Solicitud;
import utnfc.isi.back.trabajo.practico.microservicio_comercial.security.ClienteSecurity;
import utnfc.isi.back.trabajo.practico.microservicio_comercial.service.ClienteService;
import utnfc.isi.back.trabajo.practico.microservicio_comercial.service.SolicitudesService;

@Controller
@RestController
@RequestMapping("/solicitudes")
public class SolicitudesController {

    private static final Logger log = LoggerFactory.getLogger(SolicitudesController.class);
    private final SolicitudesService solicitudesService;
    private final ClienteService clienteService;
    private final ClienteSecurity clienteSecurity;

    SolicitudesController(SolicitudesService solicitudesService, ClienteService clienteService, ClienteSecurity clienteSecurity) {
        this.solicitudesService = solicitudesService;
        this.clienteService = clienteService;
        this.clienteSecurity = clienteSecurity;
    }

    @GetMapping("/health-check")
    public ResponseEntity<String> healthCheck() {
        log.info("GET /solicitudes/health-check");
        return ResponseEntity.ok("Servicio de solicitudes activo");
    }

    @GetMapping("/prueba")
    public ResponseEntity<String> rutaPrueba() {
        log.info("GET /solicitudes/prueba");
        String prueba = solicitudesService.rutaDePrueba();
        log.info("Prueba resultado={}", prueba);
        return ResponseEntity.ok(prueba);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Solicitud> obtenerSolicitudPorId(@PathVariable Long id) {
        log.info("GET /solicitudes/{} recibido", id);
        Solicitud solicitud = solicitudesService.findById(id);
        log.info("Solicitud encontrada: id={}", solicitud.getIdSolicitud());
        return ResponseEntity.status(200).body(solicitud);
    }

    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<Page<Solicitud>> obtenerSolicitudesPorClienteYEstado(
        @PathVariable Long idCliente,
        @RequestParam(required = false) Solicitud.EstadoSolicitud estado,
        @RequestParam(defaultValue = "0") Integer pagina,
        @RequestParam(defaultValue = "10") Integer tamaño
    ) {
        log.info("GET /solicitudes/cliente/{}",
        idCliente);
        Page<Solicitud> solicitudes = solicitudesService.obtenerPorCliente(idCliente, estado, pagina, tamaño);
        log.info("Solicitudes recuperadas: {}", solicitudes.getContent().size());
        return ResponseEntity.status(200).body(solicitudes);
    }

    @GetMapping("/contenedor")
    public ResponseEntity<List<UbicacionDTO>> obtenerSolicitudPorContenedor(
        @RequestParam Contenedor.EstadoContenedor estado
    ) {
        log.info("GET /solicitudes/contenedor");
        List<UbicacionDTO> ubicacionYEstadoContenedor = solicitudesService.obtenerUbicacionYEstadoContenedor(estado);
        log.info("Se encontraron {} Contenedores", ubicacionYEstadoContenedor.size());
        return ResponseEntity.status(200).body(ubicacionYEstadoContenedor);
    }

    // Endpoint que permite al cliente chequear estado de su solicitud
    @GetMapping("/seguimiento/{idSolicitud}")
    public ResponseEntity<UbicacionDTO> seguimientoSolicitud(@PathVariable Long idSolicitud) {
        UbicacionDTO ubicacionSolicitud = solicitudesService.seguimientoSolicitud(idSolicitud);
        return ResponseEntity.status(200).body(ubicacionSolicitud);
    } 

    @PostMapping("")
    public ResponseEntity<Solicitud> crearSolicitud(@RequestBody SolicitudDTO data) {
        log.info("POST /solicitudes crearSolicitud");
        Solicitud solicitud = clienteService.crearSolicitudConCliente(data);
        log.info("Solicitud creada id={}", solicitud.getIdSolicitud());
        return ResponseEntity.status(201).body(solicitud);
    }

    // Solo accesible si se crea una ruta desde el microservicio logistico
    @PutMapping("/planificar/{idSolicitud}")
    public ResponseEntity<Solicitud> planificarSolicitud(
        @PathVariable Long idSolicitud,
        @RequestBody PlanificarSolicitudDTO data
    ) {
        log.info("PUT /solicitudes/planificar/{}", idSolicitud);
        Solicitud solicitud = solicitudesService.planificar(idSolicitud, data);
        log.info("Solicitud: id={}; planificada", solicitud.getIdSolicitud());
        return ResponseEntity.status(200).body(solicitud);
    }

    // Solo accesible si la ruta se cancela desde el microservicio logistico
    @PutMapping("/cancelar-planificacion/{idSolicitud}")
    public ResponseEntity<Solicitud> cancelarPlanificacionSolicitud(
        @PathVariable Long idSolicitud
    ) {
        log.info("PUT /solicitudes/cancelar-planificacion/{}", idSolicitud);
        Solicitud solicitud = solicitudesService.cancelarPlanificacion(idSolicitud);
        log.info("Planificación cancelada para solicitud: id={}", solicitud.getIdSolicitud());
        return ResponseEntity.status(200).body(solicitud);
    }

    // Solo accesible si la ruta se inicia desde el microservicio logistico
    @PutMapping("/en-transito/{idSolicitud}")
    public ResponseEntity<Solicitud> enTransitoSolicitud(
        @PathVariable Long idSolicitud
    ) {
        log.info("PUT /solicitudes/en-transito/{}", idSolicitud);
        Solicitud solicitud = solicitudesService.enTransito(idSolicitud);
        log.info("Solicitud id: id={}; en tránsito", solicitud.getIdSolicitud());
        return ResponseEntity.status(200).body(solicitud);
    }

    // Solo accesible si la ruta se termina desde el microservicio logistico
    @PutMapping("/entregar/{idSolicitud}")
    public ResponseEntity<Solicitud> entregarSolicitud(
        @PathVariable Long idSolicitud,
        @RequestBody EntregarSolicitudDTO data
    ) {
        log.info("PUT /solicitudes/entregar/{}", idSolicitud);
        Solicitud solicitud = solicitudesService.entregar(idSolicitud, data);
        log.info("Solicitud id: id={}; entregada", solicitud.getIdSolicitud());
        return ResponseEntity.status(200).body(solicitud);
    }

    @DeleteMapping("/cancelar/{idSolicitud}")
    public ResponseEntity<Solicitud> cancelarSolicitud(@PathVariable Long idSolicitud) {
        log.info("DELETE /solicitudes/cancelar/{}", idSolicitud);
        Solicitud solicitud = solicitudesService.cancelar(idSolicitud);
        log.info("Solicitud cancelada id={}", solicitud.getIdSolicitud());
        return ResponseEntity.status(200).body(solicitud);
    }
}

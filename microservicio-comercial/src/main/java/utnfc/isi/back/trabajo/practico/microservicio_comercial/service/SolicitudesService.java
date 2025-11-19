package utnfc.isi.back.trabajo.practico.microservicio_comercial.service;

import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import utnfc.isi.back.trabajo.practico.microservicio_comercial.dto.EntregarSolicitudDTO;
import utnfc.isi.back.trabajo.practico.microservicio_comercial.dto.PlanificarSolicitudDTO;
import utnfc.isi.back.trabajo.practico.microservicio_comercial.dto.SolicitudDTO;
import utnfc.isi.back.trabajo.practico.microservicio_comercial.dto.UbicacionDTO;
import utnfc.isi.back.trabajo.practico.microservicio_comercial.excepciones.HttpException;
import utnfc.isi.back.trabajo.practico.microservicio_comercial.model.Contenedor;
import utnfc.isi.back.trabajo.practico.microservicio_comercial.model.Solicitud;
import utnfc.isi.back.trabajo.practico.microservicio_comercial.repository.SolicitudesRepository;

import utnfc.isi.back.trabajo.practico.microservicio_comercial.security.ClienteSecurity;

@Service
public class SolicitudesService {
    private static final Logger log = LoggerFactory.getLogger(SolicitudesService.class);
    private final SolicitudesRepository solicitudesRepository;
    private final ContenedoresService contenedoresService;
    private final RestClient restClient;
    private final ClienteSecurity securityConfig;

    SolicitudesService(SolicitudesRepository solicitudesRepository, ContenedoresService contenedoresService, RestClient restClient, ClienteSecurity securityConfig) {
        this.solicitudesRepository = solicitudesRepository;
        this.contenedoresService = contenedoresService;
        this.restClient = restClient;
        this.securityConfig = securityConfig;
    }

    public String rutaDePrueba() {
        log.info("Llamando a MS Logístico");
        return restClient.get()
            .uri("/rutas/health-check")
            .retrieve()
            .body(String.class);
    }

    @Transactional(rollbackFor = RestClientException.class)
    public Solicitud crear(SolicitudDTO data) {
        securityConfig.validarId(data.cliente().getIdCliente());
        log.info("Creando nueva solicitud");
        Solicitud solicitud = Solicitud.builder()
            .cliente(data.cliente())
            .estado(Solicitud.EstadoSolicitud.PENDIENTE)
            .build();

        solicitud.setContenedor(
            contenedoresService.crear(data.contenedorDTO(), data.cliente())
        );

        solicitudesRepository.save(solicitud);
        log.info("Solicitud guardada en BD con id={}", solicitud.getIdSolicitud());

        log.info("Notificando a MS Logístico la creación de ruta para solicitud={}",
        solicitud.getIdSolicitud());
        restClient
            .post()
            .uri("/rutas/crear/{idSolicitud}", solicitud.getIdSolicitud())
            .body(data.rutaDTO())
            .retrieve()
            .toBodilessEntity();

        log.info("Ruta creada correctamente en MS Logístico para solicitud={}", solicitud.getIdSolicitud());    
        return solicitud;
    }

    @Transactional(rollbackFor = HttpException.class)
    public Solicitud planificar(Long idSolicitud, PlanificarSolicitudDTO data) {
        log.info("Planificando solicitud id={}", idSolicitud);
        Solicitud solicitud = solicitudesRepository.findById(idSolicitud).orElseThrow(() -> new HttpException(404, "Solicitud con ID " + idSolicitud + " no encontrada"));

        solicitud.getEstado().validarTransicion(Solicitud.EstadoSolicitud.PLANIFICADA);

        solicitud.setCostoEstimado(data.costo());
        solicitud.setTiempoEstimado(data.tiempo());
        solicitud.setEstado(Solicitud.EstadoSolicitud.PLANIFICADA);
        log.info("Solicitud id={} planificada correctamente", idSolicitud);
        return solicitudesRepository.save(solicitud);
    }

    @Transactional(rollbackFor = HttpException.class)
    public Solicitud cancelarPlanificacion(Long idSolicitud) {
        log.info("Cancelando planificación de solicitud id={}", idSolicitud);
        Solicitud solicitud = solicitudesRepository.findById(idSolicitud).orElseThrow(() -> new HttpException(404, "Solicitud con ID " + idSolicitud + " no encontrada"));

        solicitud.getEstado().validarTransicion(Solicitud.EstadoSolicitud.PENDIENTE);

        solicitud.setCostoEstimado(null);
        solicitud.setTiempoEstimado(null);
        solicitud.setEstado(Solicitud.EstadoSolicitud.PENDIENTE);

        log.info("Planificación cancelada para solicitud id={}", idSolicitud);
        return solicitudesRepository.save(solicitud);
    }

    @Transactional(rollbackFor = HttpException.class)
    public Solicitud enTransito(Long idSolicitud) {
        log.info("Solicitud id={} pasa a estar en transoti", idSolicitud);
        Solicitud solicitud = solicitudesRepository.findById(idSolicitud).orElseThrow(() -> new HttpException(404, "Solicitud con ID " + idSolicitud + " no encontrada"));

        solicitud.getEstado().validarTransicion(Solicitud.EstadoSolicitud.EN_TRANSITO);

        solicitud.setEstado(Solicitud.EstadoSolicitud.EN_TRANSITO);
        solicitud.setFechaHoraSalida(LocalDateTime.now());

        contenedoresService.iniciarTransitoContenedor(solicitud.getContenedor().getIdContenedor());
        
        log.info("Solicitud id={} cambia estado a en transito", idSolicitud);
        return solicitudesRepository.save(solicitud);
    }

    @Transactional(rollbackFor = HttpException.class)
    public Solicitud entregar(Long idSolicitud, EntregarSolicitudDTO data) {
        log.info("Solicitud id={} pasa a ENTREGADA", idSolicitud);
        Solicitud solicitud = solicitudesRepository.findById(idSolicitud)
            .orElseThrow(() -> new HttpException(404, "Solicitud con ID " + idSolicitud + " no encontrada"));

        solicitud.getEstado().validarTransicion(Solicitud.EstadoSolicitud.ENTREGADA);

        solicitud.setCostoFinal(data.costo());
        solicitud.setTiempoReal(data.tiempo());
        solicitud.setEstado(Solicitud.EstadoSolicitud.ENTREGADA);

        contenedoresService.entregarContenedor(solicitud.getContenedor().getIdContenedor());

        log.info("Solicitud id={} entregada correctamente", idSolicitud);
        return solicitudesRepository.save(solicitud);
    }
    
    @Transactional(rollbackFor = HttpException.class)
    public Solicitud cancelar(Long idSolicitud) {
        log.warn("Cancelando solicitud id={}", idSolicitud);
        Solicitud solicitud = solicitudesRepository.findById(idSolicitud)
            .orElseThrow(() -> new HttpException(404, "Solicitud con ID " + idSolicitud + " no encontrada"));

        solicitud.getEstado().validarTransicion(Solicitud.EstadoSolicitud.CANCELADA);
        
        solicitud.setEstado(Solicitud.EstadoSolicitud.CANCELADA);

        contenedoresService.eliminar(solicitud.getContenedor().getIdContenedor());

        solicitudesRepository.save(solicitud);

        try{
            log.info("Notificando cancelación de solicitud={} al MS Logístico", idSolicitud);
            restClient.delete()
                .uri("rutas/solicitud/{idSolicitud}", idSolicitud)
                .retrieve()
                .toBodilessEntity();
        }catch(HttpException e){
            //Si no se puede cancelar la ruta, no se cancela la solicitud
            log.error("Fallo al cancelar solicitud en MS Logístico con id={}", idSolicitud);
            throw new HttpException(409, "No se puede cancelar la solicitud con ID " + idSolicitud + " porque el contenedor ya está en tránsito");
        }

        log.info("Solicitud id={} cancelada correctamente", idSolicitud);
        return solicitud;
    }

    public Page<Solicitud> obtenerPorCliente(Long idCliente,
     Solicitud.EstadoSolicitud estado, Integer pagina, Integer tamañoPagina) {
        securityConfig.validarId(idCliente);
        log.info("Buscando solicitudes para cliente={} estado={}", idCliente, estado);
        if (estado == null) {
            return findByCliente(idCliente, pagina, tamañoPagina);
        }
        return findByClienteYEstado(idCliente, estado, pagina, tamañoPagina);
    }

    public Solicitud findById(Long idSolicitud) {
        log.info("Buscando solicitud id={}", idSolicitud);
        Solicitud solicitud = solicitudesRepository.findById(idSolicitud).orElse(null);
        if (solicitud == null) {
            log.warn("Solicitud id={} no encontrada", idSolicitud);
            throw new HttpException(404, "Solicitud con ID " + idSolicitud + " no encontrada");
        }
        securityConfig.validarId(solicitud.getCliente().getIdCliente());
        return solicitud;
    }

    public Page<Solicitud> findByCliente(Long idCliente, Integer pagina, Integer tamañoPagina) {
        securityConfig.validarId(idCliente);
        Pageable pageable = PageRequest.of(pagina, tamañoPagina);
        return solicitudesRepository.findByCliente_IdCliente(idCliente, pageable);
    }

    public List<UbicacionDTO> obtenerUbicacionYEstadoContenedor(Contenedor.EstadoContenedor estado) {
        log.info("Obteniendo ubicación de contenedores con estado={}", estado);
        List<UbicacionDTO> ubicacionEstadoContenedorList = new ArrayList<>();
        List<Solicitud> solicitudes;

        if (estado != null) {
            contenedoresService.esEstadoTerminal(estado);
            solicitudes = solicitudesRepository.findByContenedor_Estado(estado);
        } else {
            solicitudes = solicitudesRepository.findNoFinalizados(Solicitud.EstadoSolicitud.ENTREGADA, Solicitud.EstadoSolicitud.CANCELADA);
        }

        log.info("Solicitudes encontradas para estado {}: {}", estado, solicitudes.size());

        for (Solicitud solicitud : solicitudes) {
            Contenedor contenedor = solicitud.getContenedor();


            log.info("Llamando a MS Logístico para ubicación del contenedor idCont={}", contenedor.getIdContenedor());
            UbicacionDTO data = restClient.get()
                .uri("/rutas/ubicacion/{idSolicitud}", solicitud.getIdSolicitud())
                .retrieve()
                .body(UbicacionDTO.class);

            UbicacionDTO ubicacionEstadoContenedor = new UbicacionDTO(
                contenedor.getIdContenedor(),
                contenedor.getEstado().name(),
                data.latitud(),
                data.longitud(),
                data.direccion(),
                data.fechaHora()
            );

            ubicacionEstadoContenedorList.add(ubicacionEstadoContenedor);
        }
        
        log.info("Ubicaciones generadas={}", ubicacionEstadoContenedorList.size());
        return ubicacionEstadoContenedorList;
    }

    public UbicacionDTO seguimientoSolicitud(Long idSolicitud) {
        Solicitud solicitud = solicitudesRepository.findById(idSolicitud).orElseThrow(() -> new HttpException(404, "Solicitud con ID " + idSolicitud + " no encontrada"));
        securityConfig.validarId(solicitud.getCliente().getIdCliente());
        Contenedor contenedor = solicitud.getContenedor();

        UbicacionDTO data = restClient.get()
                .uri("/rutas/ubicacion/{idSolicitud}", solicitud.getIdSolicitud())
                .retrieve()
                .body(UbicacionDTO.class);
        
        UbicacionDTO ubicacion = new UbicacionDTO(
                contenedor.getIdContenedor(),
                contenedor.getEstado().name(),
                data.latitud(),
                data.longitud(),
                data.direccion(),
                data.fechaHora()
            );
        
        return ubicacion;
    }

    public Page<Solicitud> findByClienteYEstado(Long idCliente, Solicitud.EstadoSolicitud estado, Integer pagina, Integer tamañoPagina) {
        securityConfig.validarId(idCliente);
        Pageable pageable = PageRequest.of(pagina, tamañoPagina);
        return solicitudesRepository.findByCliente_IdClienteAndEstado(idCliente, estado, pageable);
    }

    @Transactional(rollbackFor = HttpException.class)
    public void cancelarPorCliente(Long idCliente) {
        log.warn("Cancelando TODAS las solicitudes para cliente={}", idCliente);
        List<Solicitud> solicitudes = solicitudesRepository.findByCliente_IdCliente(idCliente);
        for (Solicitud solicitud : solicitudes) {
            try{
                this.cancelar(solicitud.getIdSolicitud());
                log.info("Solicitud id={} cancelada exitosamente", 
                     solicitud.getIdSolicitud());
            }catch(HttpException e){
                log.error("No se pudo cancelar solicitud id={} para cliente={}. Error={}",
                        solicitud.getIdSolicitud(), idCliente, e.getMessage());
                continue;
            }
        }
    }
}

package utnfc.isi.back.trabajo.practico.microservicio_logistico.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;


import org.slf4j.Logger;                       
import org.slf4j.LoggerFactory;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.dto.SolicitudDTO;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.dto.TramoDTO;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.excepciones.HttpException;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.model.Deposito;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.model.Ruta;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.model.Tarifa;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.model.Tramo;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.repository.TramoRepository;

@Service
public class TramoService {
    private static final Logger log = LoggerFactory.getLogger(TramoService.class);

    private final TramoRepository tramoRepository;
    private final TarifasService tarifasService;
    private final DepositosService depositoService;
    private final CamionService camionService;
    private final RestClient restClient;

    TramoService(TramoRepository tramoRepository, TarifasService tarifasService, DepositosService depositoService, CamionService camionService, RestClient restClient) {
        this.tramoRepository = tramoRepository;
        this.tarifasService = tarifasService;
        this.depositoService = depositoService;
        this.camionService = camionService;
        this.restClient = restClient;
    }

    public Tramo findById(Long idTramo){
        log.info("Buscando tramo {}...", idTramo);
        return tramoRepository.findById(idTramo)
        .orElseThrow(() -> new HttpException(404, "Tramo con id "+ idTramo + " no encontrado"));
    }

    public List<Tramo> obtenerTramosPorRuta(Long rutaId, Tramo.EstadoTramo estado){
        log.info("Buscando tramos para ruta {}...", rutaId);
        if(estado != null) {
            return tramoRepository.findByRutaIdRutaAndEstado(rutaId, estado);
        }
        return tramoRepository.findByRutaIdRuta(rutaId);
    }

    public List<Tramo> findTramosByRuta(Long rutaId){
        return tramoRepository.findByRutaIdRuta(rutaId);
    }

    public List<Tramo> findTramosByRutaAndEstado(Long rutaId, Tramo.EstadoTramo estado){
        return tramoRepository.findByRutaIdRutaAndEstado(rutaId, estado);
    }

    public Page<Tramo> findTramosByDominioPage(String dominio, Tramo.EstadoTramo estado, int pag, int tamPag){
        log.info("Buscando tramos para camión {}...", dominio);
        Pageable pageable = PageRequest.of(pag, tamPag);

        if (estado != null) {
            return tramoRepository.findByCamionDominioAndEstado(dominio, estado, pageable);
        }
        return tramoRepository.findByCamionDominio(dominio, pageable);
    }

    @Transactional(rollbackFor = HttpException.class)
    public Tramo crear(TramoDTO data, Ruta ruta){

        log.info("Creando tramo para ruta {}...", ruta.getIdRuta());

        Tarifa tarifa = tarifasService.findById(data.tarifaId());
        Double costoAproximado = tarifa.calcularCostoTotal(data.distancia());
        Tramo tramo = Tramo.builder()
            .ruta(ruta)
            .tarifa(tarifa)
            .estado(Tramo.EstadoTramo.PENDIENTE)
            .distancia(data.distancia())
            .tiempoEstimado(data.tiempoEstimado())
            .build();

        if (data.origenId() != null) {
            Deposito depositoOrigen = depositoService.findById(data.origenId());
            tramo.setOrigen(depositoOrigen);
            costoAproximado += depositoOrigen.getCostoEstadiaDiario();
        }else{
            tramo.setOrigen(null);
        }

        if (data.destinoId() != null) {
            tramo.setDestino(depositoService.findById(data.destinoId()));
        }else{
            tramo.setDestino(null);
        }
        
        tramo.setCostoAproximado(costoAproximado);
        tramoRepository.save(tramo);

        log.info("Tramo creado con éxito para ruta {}", ruta.getIdRuta());
        return tramo;
    }

    @Transactional(rollbackFor = HttpException.class)
    public Tramo eliminar(Long idTramo){
        log.info("Eliminando tramo {}...", idTramo);

        Tramo tramo = findById(idTramo);
        if (tramo.getCamion() != null) {
            camionService.liberar(tramo.getCamion().getDominio());
        }
        tramoRepository.delete(tramo);
        log.info("Tramo {} eliminado con éxito", idTramo);
        return tramo;
    }

    @Transactional(rollbackFor = HttpException.class)
    public Tramo asignarCamionATramo(Long idTramo, String dominio){

        log.info("Asignando camión {} al tramo {}...", dominio, idTramo);

        Tramo tramo = findById(idTramo);
        tramo.setCamion(camionService.findById(dominio));

        tramo.getEstado().validarTransicion(Tramo.EstadoTramo.PLANIFICADO);

        tramo.setEstado(Tramo.EstadoTramo.PLANIFICADO);
        camionService.ocupar(dominio);
        tramoRepository.save(tramo);

        log.info("Camión {} asignado al tramo {}", dominio, idTramo);
        return tramo;
    }

    @Transactional(rollbackFor = HttpException.class)
    public Tramo quitarCamionATramo(Long idTramo){

        log.info("Quitando camión del tramo {}...", idTramo);
        Tramo tramo = findById(idTramo);

        tramo.getEstado().validarTransicion(Tramo.EstadoTramo.PENDIENTE);

        tramo.setEstado(Tramo.EstadoTramo.PENDIENTE);

        camionService.liberar(tramo.getCamion().getDominio());
        tramo.setCamion(null);
        
        tramoRepository.save(tramo);

        log.info("Camión quitado del tramo {}", idTramo);
        return tramo;
    }

    @Transactional(rollbackFor = HttpException.class)
    public Tramo iniciarTramo(Long idTramo){
        log.info("Iniciando tramo {}...", idTramo);

        Tramo tramo = findById(idTramo);
        Tramo tramoAnterior = findByIdYRuta(idTramo, tramo.getRuta().getIdRuta());

        tramo.getEstado().validarTransicion(Tramo.EstadoTramo.INICIADO);

        tramo.setEstado(Tramo.EstadoTramo.INICIADO);
        tramo.setFechaHoraInicio(LocalDateTime.now());
        tramo.calcularCostoReal(tramoAnterior.getFechaHoraFin(), tramo.getTarifa().getPrecioCombustible());

        tramoRepository.save(tramo);
        if (tramo.getOrigen() == null){
            restClient.put()
                .uri("/solicitudes/en-transito/{idSolicitud}", tramo.getRuta().getIdSolicitud())
                .retrieve()
                .toBodilessEntity();
        }

        log.info("Tramo {} iniciado con éxito", idTramo);
        return tramo;
    }

    @Transactional(rollbackFor = HttpException.class)
    public Tramo finalizarTramo(Long idTramo){

        log.info("Finalizando tramo {}...", idTramo);

        Tramo tramo = findById(idTramo);

        tramo.getEstado().validarTransicion(Tramo.EstadoTramo.TERMINADO);

        tramo.setEstado(Tramo.EstadoTramo.TERMINADO);
        tramo.setFechaHoraFin(LocalDateTime.now());

        camionService.liberar(tramo.getCamion().getDominio());

        tramoRepository.save(tramo);

        if (tramo.getDestino() == null){
            SolicitudDTO costoReal = calcularCostoYTiempoRealPorRuta(tramo.getRuta().getIdRuta());
            restClient.put()
                .uri("/solicitudes/entregar/{idSolicitud}", tramo.getRuta().getIdSolicitud())
                .body(costoReal)
                .retrieve()
                .toBodilessEntity();
        }
        
        log.info("Tramo {} finalizado con éxito", idTramo);
        return tramo;
    }

    public Tramo findByIdYRuta(Long idTramo, Long idRuta){
        return tramoRepository.findByIdTramoAndRutaIdRuta(idTramo, idRuta);
    }

    public Long contarTramosPorRuta(Long idRuta) {
        return tramoRepository.contarTramosPorRuta(idRuta);
    }

    public SolicitudDTO calcularCostoYTiempoTotalPorRuta(Long idRuta) {
        Double costoTotal = 0.0;
        Long tiempoTotal = 0L;

        List<Tramo> tramos = findTramosByRuta(idRuta);
        for (Tramo tramo : tramos) {
            costoTotal += tramo.getCostoAproximado();
            tiempoTotal += tramo.getTiempoEstimado();
        }

        return new SolicitudDTO(costoTotal, tiempoTotal);
    }

    public SolicitudDTO calcularCostoYTiempoRealPorRuta(Long idRuta) {
        Double costoTotal = 0.0;
        Long tiempoTotal = 0L;

        List<Tramo> tramos = findTramosByRuta(idRuta);
        for (Tramo tramo : tramos) {
            costoTotal += tramo.getCostoReal();
            if (tramo.getFechaHoraInicio() != null && tramo.getFechaHoraFin() != null) {
                tiempoTotal += Duration.between(tramo.getFechaHoraInicio(), tramo.getFechaHoraFin()).getSeconds();
            }
        }

        return new SolicitudDTO(costoTotal, tiempoTotal);
    }

    public void validarCancelacionDeTramosPorRuta(Long idRuta) {
        List<Tramo> tramos = findTramosByRuta(idRuta);
        for (Tramo tramo : tramos) {
            if (tramo.getEstado() == Tramo.EstadoTramo.INICIADO || tramo.getEstado() == Tramo.EstadoTramo.TERMINADO) {
                throw new HttpException(409, "No se puede cancelar la ruta porque el tramo con ID " + tramo.getIdTramo() + " ya está en estado " + tramo.getEstado());
            }
        }
    }

    public Tramo findUltimoTramoTerminado(Long idRuta) {
        List<Tramo> tramos = findTramosByRutaAndEstado(idRuta, Tramo.EstadoTramo.TERMINADO);
        if (tramos.isEmpty()) {
            return null;
        }
        
        tramos.sort((t1, t2) -> t2.getFechaHoraFin().compareTo(t1.getFechaHoraFin()));
        return tramos.get(0);
    }
}


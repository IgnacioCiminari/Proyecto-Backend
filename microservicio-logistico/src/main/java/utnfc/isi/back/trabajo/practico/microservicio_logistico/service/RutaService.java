package utnfc.isi.back.trabajo.practico.microservicio_logistico.service;

import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import utnfc.isi.back.trabajo.practico.microservicio_logistico.dto.DistanciaDTO;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.dto.SolicitudDTO;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.dto.TramoDTO;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.dto.UbicacionDTO;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.excepciones.HttpException;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.model.Enlace;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.model.Grafo;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.model.Nodo;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.model.Ruta;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.model.Tramo;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.repository.RutaRepository;

@Service
public class RutaService {

    private static final Logger log = LoggerFactory.getLogger(RutaService.class);

    private final RutaRepository rutaRepository; 
    private final TramoService tramoService;
    private final Grafo grafo;
    private final EnlacesService enlacesService;
    private final TarifasService tarifasService;
    private final CamionService camionesService;
    private final RestClient restClient;
    private final GeoService geoService;

    RutaService(RutaRepository rutaRepository, TramoService tramoService, Grafo grafo, EnlacesService enlacesService, TarifasService tarifasService, CamionService camionService, RestClient restClient, GeoService geoService) {
        this.rutaRepository = rutaRepository;
        this.tramoService = tramoService;
        this.grafo = grafo;
        this.enlacesService = enlacesService;
        this.tarifasService = tarifasService;
        this.camionesService = camionService;
        this.restClient = restClient;
        this.geoService = geoService;
    }

    public String rutaDePrueba() {
        log.info("Ejecutando ruta de prueba hacia MS Comercial...");
        return restClient.get()
            .uri("/solicitudes/health-check")
            .retrieve()
            .body(String.class);
    }

    public Page<Ruta> obtenerTodas(Integer pagina, Integer tamañoPagina) {
        log.info("Buscando todas las rutas...");
        Pageable pageable = PageRequest.of(pagina, tamañoPagina);
        return rutaRepository.findAll(pageable);
    }
    
    public Ruta findById(Long idRuta) {
        log.info("Buscando ruta {}", idRuta);
        Ruta ruta = rutaRepository.findById(idRuta).orElse(null);
        if (ruta == null) {
            log.warn("Ruta {} no encontrada", idRuta);
            throw new HttpException(404, "Ruta con ID " + idRuta + " no encontrada");
        }
        log.info("Ruta {} encontrada con éxito", idRuta);
        return ruta;
    }

    @Transactional(rollbackFor = HttpException.class)
    public Ruta crear(Long idSolicitud, Ruta data) {
        log.info("Creando ruta para solicitud {}...", idSolicitud);
        Ruta ruta = Ruta.builder()
            .idSolicitud(idSolicitud)
            .origenLatitud(data.getOrigenLatitud())
            .origenLongitud(data.getOrigenLongitud())
            .destinoLatitud(data.getDestinoLatitud())
            .destinoLongitud(data.getDestinoLongitud())
            .build();
        
        log.info("Ruta creada con éxito, ID {}", ruta.getIdRuta());

        return rutaRepository.save(ruta);
    }

    @Transactional(rollbackFor = HttpException.class)
    public Ruta crearTramosParaRuta(Long idSolicitud, List<TramoDTO> tramosDTO) {

        log.info("Creando tramos para ruta de solicitud {}...", idSolicitud);

        Ruta ruta = this.findByIdSolicitud(idSolicitud);

        ruta.setCantidadDepositos(tramosDTO.size() - 1);
        ruta.setCantidadTramos(tramosDTO.size());

        for (TramoDTO tramoDTO : tramosDTO) {
            tramoService.crear(tramoDTO, ruta);
        }

        SolicitudDTO costoTotal = tramoService.calcularCostoYTiempoTotalPorRuta(ruta.getIdRuta());
        
        restClient.put()
            .uri("/solicitudes/planificar/{idSolicitud}", ruta.getIdSolicitud())
            .body(costoTotal)
            .retrieve()             // Realiza la peticion directamente a la uri
            .toBodilessEntity();    // Defino que no espero que devuelva nada

        log.info("Tramos creados con éxito para solicitud {}", idSolicitud);

        return ruta;
    }

    @Transactional(rollbackFor = HttpException.class)
    public Ruta eliminarPorId(Long idRuta) {

        log.info("Eliminando ruta {}...", idRuta);

        Ruta ruta = rutaRepository.findById(idRuta).orElseThrow(() -> new HttpException(404, "Ruta con ID " + idRuta + " no encontrada."));
        
        tramoService.validarCancelacionDeTramosPorRuta(idRuta);

        tramoService.findTramosByRuta(idRuta).forEach(tramo -> {
            tramoService.eliminar(tramo.getIdTramo());
        });

        ruta.setCantidadDepositos(0);
        ruta.setCantidadTramos(0);

        rutaRepository.save(ruta);

        
        restClient.put()
            .uri("/solicitudes/cancelar-planificacion/{idSolicitud}", ruta.getIdSolicitud())
            .retrieve()
            .toBodilessEntity();

        log.info("Ruta {} eliminada con éxito", idRuta);
        return ruta;
    }

    public long calcularTramosPorRuta(Long idRuta) {
        log.info("Buscando ruta por solicitud...");
        return tramoService.contarTramosPorRuta(idRuta);
    }

    public Ruta findByIdSolicitud(Long idSolicitud) {
        Ruta ruta = rutaRepository.findByIdSolicitud(idSolicitud);
        if (ruta == null) {
            log.warn("Ruta no encontrada para solicitud {}", idSolicitud);
            throw new HttpException(404, "Ruta con ID de solicitud " + idSolicitud + " no encontrada");
        }
        log.info("Ruta obtenida con éxito para solicitud {}", idSolicitud);
        return ruta;
    }

    @Transactional(rollbackFor = HttpException.class)
    public Ruta eliminarRutaPorIdSolicitud(Long idSolicitud) {

        log.info("Eliminando ruta asociada a solicitud {}...", idSolicitud);

        Ruta ruta = this.findByIdSolicitud(idSolicitud);

        tramoService.validarCancelacionDeTramosPorRuta(ruta.getIdRuta());

        tramoService.findTramosByRuta(ruta.getIdRuta()).forEach(tramo -> {
            tramoService.eliminar(tramo.getIdTramo());
        });

        rutaRepository.delete(ruta);

        log.info("Ruta eliminada con éxito para solicitud {}", idSolicitud);
        return ruta;
    }

    public UbicacionDTO obtenerUbicacionPorIdSolicitud(Long idSolicitud) {

        log.info("Obteniendo ubicación para solicitud {}...", idSolicitud);

        Ruta ruta = this.findByIdSolicitud(idSolicitud);
        Tramo tramoFinalizado = tramoService.findUltimoTramoTerminado(ruta.getIdRuta());

        // Caso 1: Nunca se inició la ruta
        if (tramoFinalizado == null) {
            return UbicacionDTO.builder()
                    .latitud(ruta.getOrigenLatitud())
                    .longitud(ruta.getOrigenLongitud())
                    .direccion("Ruta no iniciada")
                    .fechaHora(null)
                    .build();
        }

        // Caso 2: El tramo finalizado NO TIENE DESTINO
        // Esto pasa cuando es el tramo final (entrega)
        if (tramoFinalizado.getDestino() == null) {
            return UbicacionDTO.builder()
                    .latitud(ruta.getDestinoLatitud())      // o el dato que tengas
                    .longitud(ruta.getDestinoLongitud())    // si no tenés destino, usa el origen del tramo
                    .direccion("Destino final alcanzado")
                    .fechaHora(tramoFinalizado.getFechaHoraFin())
                    .build();
        }

        // Caso 3: Tramo normal con destino definido
        return UbicacionDTO.builder()
                .latitud(tramoFinalizado.getDestino().getLatitud())
                .longitud(tramoFinalizado.getDestino().getLongitud())
                .direccion(tramoFinalizado.getDestino().getDireccion())
                .fechaHora(tramoFinalizado.getFechaHoraFin())
                .build();
    }


    public List<Ruta> obtenerRutasPendientes() {
        log.info("Buscando rutas pendientes...");
        return rutaRepository.findRutasPendientes();
    }

    public List<List<Tramo>> crearRutasAlternativos(Long idRuta, int cantidadRutas) {
        log.info("Generando rutas alternativas para ruta {}...", idRuta);
        Ruta rutaSolicitada = this.findById(idRuta);
        List<List<Nodo>> rutasConNodos = this.grafo.encontrarCaminos(rutaSolicitada.getOrigenLatitud(), rutaSolicitada.getOrigenLongitud(), rutaSolicitada.getDestinoLatitud(), rutaSolicitada.getDestinoLongitud(), cantidadRutas);

        System.out.println("Rutas encontradas: " + rutasConNodos);

        Double costoBaseXKmPromedio = camionesService.obtenerCostoPromedioBaseXKm(0.0);
        Double consumoBaseXKmPromedio = camionesService.obtenerConsumoPromedioXKm(0.0);
        Double precioCombustible = tarifasService.obtenerPrecioCombustibleActual();

        List<List<Tramo>> rutasAlternativas = new ArrayList<>();

        for (List<Nodo> rutaConNodos : rutasConNodos) {
            List<Tramo> rutaAlternativa = new ArrayList<>();
    
            // Tramo desde el origen real hasta el primer nodo
            if (!rutaConNodos.isEmpty()) {
                Nodo primerNodo = rutaConNodos.get(0);
                DistanciaDTO distanciaDuracion = geoService.obtenerDistanciaDuracion(
                                                    rutaSolicitada.getOrigenLongitud(),
                                                    rutaSolicitada.getOrigenLatitud(),
                                                    primerNodo.getLongitud(),
                                                    primerNodo.getLatitud()
                                                );
    
                Tramo tramoOrigen = Tramo.builder()
                        .ruta(rutaSolicitada)
                        .origen(null) // origen real de la ruta
                        .destino(primerNodo.getDeposito())
                        .distancia(distanciaDuracion.kilometros())
                        .tiempoEstimado(distanciaDuracion.duracionSegundos())
                        .costoAproximado(costoBaseXKmPromedio * distanciaDuracion.kilometros()
                                        + consumoBaseXKmPromedio * precioCombustible * distanciaDuracion.kilometros())
                        .build();
    
                rutaAlternativa.add(tramoOrigen);
            }
    
            // Tramos intermedios entre nodos consecutivos
            for (int i = 1; i < rutaConNodos.size(); i++) {
                Nodo origenTramo = rutaConNodos.get(i - 1);
                Nodo destinoTramo = rutaConNodos.get(i);
                Enlace enlace = enlacesService.obtenerEnlacePorNodos(origenTramo, destinoTramo);
    
                Double costoAproximado = costoBaseXKmPromedio * enlace.getDistancia() 
                                        + consumoBaseXKmPromedio * precioCombustible * enlace.getDistancia();
                if (origenTramo.getDeposito() != null) {
                    costoAproximado += origenTramo.getDeposito().getCostoEstadiaDiario();
                }
    
                Tramo tramo = Tramo.builder()
                        .ruta(rutaSolicitada)
                        .origen(origenTramo.getDeposito())
                        .destino(destinoTramo.getDeposito())
                        .distancia(enlace.getDistancia())
                        .tiempoEstimado(enlace.getTiempoEstimado())
                        .costoAproximado(costoAproximado)
                        .build();
    
                rutaAlternativa.add(tramo);
            }
    
            // Tramo desde el último nodo hasta el destino real
            if (!rutaConNodos.isEmpty()) {
                Nodo ultimoNodo = rutaConNodos.get(rutaConNodos.size() - 1);
                DistanciaDTO distanciaDuracion = geoService.obtenerDistanciaDuracion(
                                                    ultimoNodo.getLongitud(),
                                                    ultimoNodo.getLatitud(),
                                                    rutaSolicitada.getDestinoLongitud(),
                                                    rutaSolicitada.getDestinoLatitud()
                                                );
    
                Tramo tramoDestino = Tramo.builder()
                        .ruta(rutaSolicitada)
                        .origen(ultimoNodo.getDeposito())
                        .destino(null) // destino real de la ruta
                        .distancia(distanciaDuracion.kilometros())
                        .tiempoEstimado(distanciaDuracion.duracionSegundos())
                        .costoAproximado(costoBaseXKmPromedio * distanciaDuracion.kilometros()
                                        + consumoBaseXKmPromedio * precioCombustible * distanciaDuracion.kilometros())
                        .build();
    
                rutaAlternativa.add(tramoDestino);
            }
    
            rutasAlternativas.add(rutaAlternativa);
        }
        log.info("Rutas alternativas generadas con éxito para ruta {}", idRuta);
        return rutasAlternativas; 
    }
}

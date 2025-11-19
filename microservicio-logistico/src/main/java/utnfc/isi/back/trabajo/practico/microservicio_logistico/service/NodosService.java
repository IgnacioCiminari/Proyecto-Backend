package utnfc.isi.back.trabajo.practico.microservicio_logistico.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import utnfc.isi.back.trabajo.practico.microservicio_logistico.dto.DistanciaDTO;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.excepciones.HttpException;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.model.Deposito;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.model.Nodo;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.repository.NodosRepository;

@Service
public class NodosService {

    private static final Logger log = LoggerFactory.getLogger(NodosService.class);

    private final NodosRepository nodosRepository;
    private final EnlacesService enlacesService;
    private final GeoService geoService;

    NodosService(NodosRepository nodosRepository, EnlacesService enlacesService, GeoService geoService) {
        this.nodosRepository = nodosRepository;
        this.enlacesService = enlacesService;
        this.geoService = geoService;
    }

    public List<Nodo> obtenerTodos() {
        log.info("Buscando nodos...");
        return nodosRepository.findAll();
    }

    @Transactional(rollbackFor = HttpException.class)
    public Nodo crearNodoParaDeposito(Deposito deposito) {
        
        log.info("Creando nodo para depósito {}...", deposito.getIdDeposito());

        List<Nodo> nodosExistentes = this.obtenerTodos();
        Nodo nodo = Nodo.builder()
            .deposito(deposito)
            .latitud(deposito.getLatitud())
            .longitud(deposito.getLongitud())
            .build();
        nodosRepository.save(nodo);
        enlacesService.crearEnlacesParaNodo(nodo, nodosExistentes);
        log.info("Nodo {} creado con éxito", nodo.getIdNodo());
        return nodo;
    }

    public DistanciaDTO calcularDistancia(Nodo a, Nodo b) {
        log.info("Calculando distancia entre nodos...");
        return geoService.obtenerDistanciaDuracion(a.getLongitud(), a.getLatitud(), b.getLongitud(), b.getLatitud());
    }

    public long contar() {
        log.info("Contando nodos...");
        return nodosRepository.count();
    }
}

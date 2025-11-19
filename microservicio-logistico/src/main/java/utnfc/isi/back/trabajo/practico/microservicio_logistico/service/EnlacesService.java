package utnfc.isi.back.trabajo.practico.microservicio_logistico.service;

import java.util.List;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import utnfc.isi.back.trabajo.practico.microservicio_logistico.dto.DistanciaDTO;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.excepciones.HttpException;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.model.Enlace;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.model.Nodo;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.repository.EnlacesRepository;

@Service
public class EnlacesService {

    private static final Logger log = LoggerFactory.getLogger(EnlacesService.class);

    private final EnlacesRepository enlacesRepository;
    private final GeoService geoService;

    public EnlacesService(EnlacesRepository enlacesRepository, GeoService geoService) {
        this.enlacesRepository = enlacesRepository;
        this.geoService = geoService;
    }

    public List<Enlace> obtenerTodos() {
        log.info("Buscando todos los enlaces...");
        return enlacesRepository.findAll();
    }

    @Transactional(rollbackFor = HttpException.class)
    public void crearEnlacesParaNodo(Nodo nodo, List<Nodo> nodos) {
        log.info("Creando enlaces para nodo {}...", nodo.getIdNodo());
        Double distanciaUmbral = 400.0; // Umbral de distancia en kilómetros

        List<Nodo> nodosExistentes = nodos;
        for (Nodo nodoExistente : nodosExistentes) {

            if (nodoExistente.equals(nodo)) continue;

            // Esto es un objeto con la distancia y el tiempo entre nodos
            DistanciaDTO costoEnlace = calcularCostoEnlace(nodo, nodoExistente);
            if (costoEnlace.kilometros() <= distanciaUmbral) {
                // Crear enlace desde el nuevo nodo al nodo existente
                Enlace enlace1 = Enlace.builder()
                        .origen(nodo)
                        .destino(nodoExistente)
                        .distancia(costoEnlace.kilometros())
                        .tiempoEstimado(costoEnlace.duracionSegundos())
                        .build();
                enlacesRepository.save(enlace1);

                // Crear enlace desde el nodo existente al nuevo nodo
                Enlace enlace2 = Enlace.builder()
                        .origen(nodoExistente)
                        .destino(nodo)
                        .distancia(costoEnlace.kilometros())
                        .tiempoEstimado(costoEnlace.duracionSegundos())
                        .build();
                enlacesRepository.save(enlace2);
            }
        }
        log.info("Enlaces creados con éxito para nodo {}", nodo.getIdNodo());
    }

    public DistanciaDTO calcularCostoEnlace(Nodo origen, Nodo destino) {
        log.info("Calculando costo de enlace");
        return geoService.obtenerDistanciaDuracion(
            origen.getLongitud(), origen.getLatitud(),
            destino.getLongitud(), destino.getLatitud()
        );
    }

    public long contar() {
        log.info("Contando enlaces...");
        return enlacesRepository.count();
    }

    public Enlace obtenerEnlacePorNodos(Nodo origen, Nodo destino) {
        log.info("Buscando enlace entre nodos");
        return enlacesRepository.findByOrigenAndDestino(origen, destino);
    }
}

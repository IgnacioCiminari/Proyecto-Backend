package utnfc.isi.back.trabajo.practico.microservicio_logistico.service;
import java.time.LocalDateTime;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import utnfc.isi.back.trabajo.practico.microservicio_logistico.dto.TarifaDTO;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.excepciones.HttpException;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.model.Tarifa;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.repository.TarifaRepository;


@Service
@Slf4j
public class TarifasService {
    private final TarifaRepository tarifaRepository;
    private final CamionService camionService;

    public TarifasService(TarifaRepository tarifaRepository, CamionService camionService) {
        this.tarifaRepository = tarifaRepository;
        this.camionService = camionService;
    }


    // crear tarifa y asignar fecha desde, como esta vigente no le asigno fecha hasta (null)
    @Transactional(rollbackFor = HttpException.class)
    public Tarifa crear(TarifaDTO data) {

        log.info("Creando tarifa...");

        Tarifa tarifa = new Tarifa();
        tarifa.setVolumenMin(data.volumenMin());
        tarifa.setVolumenMax(data.volumenMax());
        tarifa.setCostoBaseXKm(camionService.obtenerCostoPromedioBaseXKm(data.volumenMin()));
        tarifa.setConsumoXKm(camionService.obtenerConsumoPromedioXKm(data.volumenMin()));
        tarifa.setPrecioCombustible(data.precioCombustible());
        tarifa.setFechaDesde(LocalDateTime.now());
        tarifa.setFechaHasta(null);

        tarifaRepository.save(tarifa);

        log.info("Tarifa creada con éxito");
        return tarifa;
    }


    // buscar tarifa por id
    public Tarifa findById(Long idTarifa){
        log.info("Buscando tarifa {}...", idTarifa);
        return tarifaRepository.findById(idTarifa)
        .orElseThrow(() -> new HttpException(404,
         "Tarifa con id "+ idTarifa + " no encontrada"));
    }

    //obtener tarifas con paginacion
    public Page<Tarifa> ObtenerTodas(int pag, int tamPag) {
        log.info("Obteniendo todas las tarifas...");
        Pageable pageable = PageRequest.of(pag, tamPag);
        return tarifaRepository.findAll(pageable);
    }

    // eliminacion logica de una tarifa seteandole su fecha de fin y guardandola nuevamente
    @Transactional(rollbackFor = HttpException.class)
    public Tarifa Eliminar(Long idTarifa) {
        log.warn("Eliminando tarifa {} (baja lógica)...", idTarifa);
        Tarifa tarifa = tarifaRepository.findById(idTarifa)
            .orElseThrow(() -> new HttpException(404,"Tarifa con id "+ idTarifa + " no encontrada"));
            
        tarifa.setFechaHasta(LocalDateTime.now());
        tarifaRepository.save(tarifa);
        log.info("Tarifa {} eliminada con éxito", idTarifa);
        return tarifa;
    }

    public Double obtenerPrecioCombustibleActual() {
        return tarifaRepository.findTop1ByFechaHastaIsNull().getPrecioCombustible();
    }

    public List<Tarifa> findAllAvailable(Double capacidadCarga) {
        log.info("Buscando tarifas disponibles...");
        if (capacidadCarga != null) {
            return tarifaRepository.findTarifaVigentePorCapacidad(capacidadCarga);
        } else {
            return tarifaRepository.findByFechaHastaIsNull();
        }
    }
}

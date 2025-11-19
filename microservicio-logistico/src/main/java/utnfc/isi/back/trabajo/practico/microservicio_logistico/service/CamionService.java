package utnfc.isi.back.trabajo.practico.microservicio_logistico.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import utnfc.isi.back.trabajo.practico.microservicio_logistico.dto.CamionDTO;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.excepciones.HttpException;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.model.Camion;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.model.Transportista;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.repository.CamionRepository;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.repository.TransportistaRepository;

@Service
@Validated
public class CamionService {

    private static final Logger log = LoggerFactory.getLogger(CamionService.class);
    private final CamionRepository camionRepository;
    private final TransportistaRepository transportistaRepository;

    public CamionService(CamionRepository camionRepository, TransportistaRepository transportistaRepository) {
        this.camionRepository = camionRepository;
        this.transportistaRepository = transportistaRepository;
    }

    // Obtener un camión por dominio
    public Camion findById(
        @NotNull @NonNull String dominio
    ) {
        log.info("Buscando camion dominio={}", dominio);
        return camionRepository.findById(dominio).orElseThrow(() -> new EntityNotFoundException("Camión no encontrado con dominio: " + dominio));
    }

    // Obtener todos los camiones paginados
    public Page<Camion> obtenerTodos(
        @NotNull @PositiveOrZero int pagina, 
        @NotNull @Positive int tamañoPagina
    ) {log.info("Buscando todos los camiones");
        return camionRepository.findAll(PageRequest.of(pagina, tamañoPagina));
    }

    // Obtener camiones por transportista
    public Page<Camion> obtenerPorTransportista(
        @NotNull @Positive Long idTransportista, 
        @NotNull @PositiveOrZero int pagina, 
        @NotNull @Positive int tamañoPagina
    ) {
        log.info("Buscando camiones por transportista id={}", idTransportista);
        return camionRepository.findByTransportista_IdTransportista(idTransportista, PageRequest.of(pagina, tamañoPagina));
    }

    // Crear un camión nuevo
    @Transactional(rollbackFor = HttpException.class)
    public Camion crear(
        @Valid CamionDTO data
    ) {
        log.info("Creando nuevo camion...");
        Transportista transportista = transportistaRepository.findById(data.getIdTransportista()).orElseThrow(() -> new EntityNotFoundException("Transportista no encontrado con ID: " + data.getIdTransportista()));
        Camion nuevo = Camion.builder()
                .dominio(data.getDominio())
                .transportista(transportista)
                .capacidadPeso(data.getCapacidadPeso())
                .capacidadVolumen(data.getCapacidadVolumen())
                .costoBaseXKm(data.getCostoBaseXKm())
                .consumoXKm(data.getConsumoXKm())
                .disponibilidad(true) // Por defecto disponible
                .build();
        
        log.info("Camion creado con exito...");
        return camionRepository.save(nuevo);
    }

    // Cambiar estado a "ocupado"
    @Transactional(rollbackFor = HttpException.class)
    public Camion ocupar(
        @NotBlank String dominio
    ) {
        log.info("Ocupando camion dominio={}", dominio);
        Camion camion = findById(dominio);

        if (!camion.getDisponibilidad()) {
            log.warn("No se puede ocupar camion {} porque ya está ocupado", dominio);
            throw new HttpException(409, "No se puede ocupar el camion porque el camion con dominio " + camion.getDominio() + " ya esta ocupado");
        }

        camion.setDisponibilidad(false);
        log.info("Camion dominio={} se encuentra ahora ocupado...", dominio);
        return camionRepository.save(camion);
    }

    // Cambiar estado a "disponible"
    @Transactional(rollbackFor = HttpException.class)
    public Camion liberar(
        @NotBlank String dominio
    ) {
        log.info("Liberando camion dominio={}", dominio);
        Camion camion = findById(dominio);
        camion.setDisponibilidad(true);
        log.info("Camion dominio={} ahora se encuentra disponible", dominio);
        return camionRepository.save(camion);
    }

    // Eliminar camión (logicamente)
    @Transactional(rollbackFor = HttpException.class)
    public Camion eliminar(
        @NotBlank String dominio
    ) {
        log.warn("Eliminando (baja lógica) camion dominio={}...", dominio);
        Camion camion = findById(dominio);
        log.info("Camion dominio={} dado de baja...", dominio);
        return camionRepository.save(camion);
    }

    // Eliminar camión por transportista (logicamente). Metodo invocado desde transportistaService cuando se da de baja un transportista
    @Transactional(rollbackFor = HttpException.class)
    public List<Camion> eliminarPorTransportista(
        Long idTransportista
    ) {
        List<Camion> camiones = camionRepository.findByTransportista_IdTransportista(idTransportista);
        for (Camion camion : camiones) {
            this.eliminar(camion.getDominio());
        }
        return camiones;
    }

    public Double obtenerCostoPromedioBaseXKm(
        @NotNull @Positive Double volumenMin
    ) {
        log.info("Calculando promedio costoBaseXKm...");
        return camionRepository.avgCostoBaseXKm(volumenMin);
    }

    public Double obtenerConsumoPromedioXKm(
        @NotNull @Positive Double volumenMin
    ) {
        log.info("Calculando consumo promedio XKm");
        return camionRepository.avgConsumoXKm(volumenMin);
    }

    // metodo para validar que no se supere el peso y el volumen maximos del camion, si no se supera devuelve true, si se supera devuelve false.
    public boolean validarPesoYVolumen(
        @NotNull @Positive Double peso, 
        @NotNull @Positive Double volumen, 
        @NotBlank String dominio
    ) {
        log.info("Validando peso y volumen para camion dominio={}", dominio);
        Camion camion = findById(dominio);
        if(peso <= camion.getCapacidadPeso() && volumen <= camion.getCapacidadVolumen()){
            log.info("peso y volumen validos para camion dominio= {}", dominio);
            return true;
        }
        return false;
    }

    public Page<Camion> obtenerDisponiblesPorCapacidad(
        @NotNull @Positive Integer capacidad, 
        @NotNull @PositiveOrZero int pagina, 
        @NotNull @Positive int tamañoPagina
    ) {
        log.info("Buscando camiones disponibles por capacidad...");
        Pageable pageable = PageRequest.of(pagina, tamañoPagina);
        return camionRepository.findByCapacidadPesoGreaterThanEqualAndDisponibilidadTrue(capacidad, pageable);
    }
}

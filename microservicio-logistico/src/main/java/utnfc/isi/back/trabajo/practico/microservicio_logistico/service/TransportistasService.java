package utnfc.isi.back.trabajo.practico.microservicio_logistico.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.dto.TransportistaDTO;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.excepciones.HttpException;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.model.Transportista;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.repository.TransportistaRepository;

@Service
public class TransportistasService {

    private static final Logger log = LoggerFactory.getLogger(TransportistasService.class);

    private final TransportistaRepository transportistaRepository;
    private final CamionService camionService;

    public TransportistasService(TransportistaRepository transportistaRepository, CamionService camionService) {
        this.transportistaRepository = transportistaRepository;
        this.camionService = camionService;
    }

    // Obtener por ID
    public Transportista findById(Long id) {

        log.info("Buscando transportista {}...", id);

        return transportistaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró el transportista con ID: " + id));
    }

    // Obtener todos paginados
    public Page<Transportista> obtenerTodos(int pagina, int tamañoPagina) {

        log.info("Buscando todos los transportistas...");

        return transportistaRepository.findAll(PageRequest.of(pagina, tamañoPagina));
    }

    // Crear nuevo transportista
    @Transactional(rollbackFor = HttpException.class)
    public Transportista crear(TransportistaDTO dto) {

        log.info("Creando transportista...");

        Transportista nuevo = new Transportista();
        nuevo.setNombre(dto.nombre());
        nuevo.setApellido(dto.apellido());
        nuevo.setTelefono(dto.telefono());
        nuevo.setDireccion(dto.direccion());
        nuevo.setCorreo(dto.correo());

        log.info("Transportista con ID: {} creado con éxito", nuevo.getIdTransportista());
        return transportistaRepository.save(nuevo);
    }

    // Actualizar transportista existente
    @Transactional(rollbackFor = HttpException.class)
    public Transportista actualizar(Long id, TransportistaDTO dto) {

        log.info("Actualizando transportista {}...", id);

        Transportista existente = findById(id);
        existente.setNombre(dto.nombre());
        existente.setApellido(dto.apellido());
        existente.setTelefono(dto.telefono());
        existente.setDireccion(dto.direccion());
        existente.setCorreo(dto.correo());

        log.info("Transportista {} actualizado con éxito", id);
        return transportistaRepository.save(existente);
    }

    @Transactional(rollbackFor = HttpException.class)
    public Transportista eliminar(Long id) {

        log.info("Eliminando transportista {}...", id);

        Transportista existente = findById(id);
        existente.setActivo(false);
        transportistaRepository.save(existente);

        // Eliminar camiones asociados a transportista
        camionService.eliminarPorTransportista(id);

        log.info("Transportista {} eliminado con éxito", id);
      
        return existente;
    }
}

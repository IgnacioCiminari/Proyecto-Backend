package utnfc.isi.back.trabajo.practico.microservicio_comercial.service;

import org.slf4j.Logger; 
import org.slf4j.LoggerFactory;

import utnfc.isi.back.trabajo.practico.microservicio_comercial.repository.ClientesRepository;
import utnfc.isi.back.trabajo.practico.microservicio_comercial.dto.ClienteDTO;
import utnfc.isi.back.trabajo.practico.microservicio_comercial.dto.SolicitudDTO;
import utnfc.isi.back.trabajo.practico.microservicio_comercial.excepciones.HttpException;
import utnfc.isi.back.trabajo.practico.microservicio_comercial.model.Cliente;
import utnfc.isi.back.trabajo.practico.microservicio_comercial.model.Solicitud;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {

    private static final Logger log = LoggerFactory.getLogger(ClienteService.class);
    // Defino como atributo del service el repositorio del cliente.
    private final ClientesRepository clientesRepository;
    private final SolicitudesService solicitudesService;

    ClienteService(ClientesRepository clientesRepository, SolicitudesService solicitudesService) {
        this.clientesRepository = clientesRepository;
        this.solicitudesService = solicitudesService;
    }

    // Aca defino los metodos de Alta, Baja, Modificacion y Consluta de la BD para la entidad Cliente

    // ========== ALTA ==========
    @Transactional(rollbackFor = HttpException.class)
    public Cliente crear(Cliente data) {

        log.info("Creando cliente DNI={} Nombre={} {}", 
                 data.getDni(), data.getNombre(), data.getApellido());

        Cliente cliente = Cliente.builder()
            .nombre(data.getNombre())
            .apellido(data.getApellido())
            .dni(data.getDni())
            .correo(data.getCorreo())
            .telefono(data.getTelefono())
            .direccion(data.getDireccion())
            .build();
        // JpaRepository utiliza el metodo "save" tanto para crear como para modificar. En este caso lo usamos para crear

        log.info("Cliente creado con id={}", cliente.getIdCliente());
        return clientesRepository.save(cliente);
    }

    // ========== OBTENER TODOS ==========
    public List<Cliente> obtenerTodos() {
        log.info("Buscando todos los clientes activos");
        List<Cliente> listadoClientes = clientesRepository.findByActivo(1);
        log.info("Cantidad obtenida={}", listadoClientes.size());
        return listadoClientes;
    }

    // ========== OBTENER POR ID ==========
    public Cliente obtenerPorId(Long idCliente) {
        log.info("Buscando cliente con id={}", idCliente);
        Cliente cliente = clientesRepository.findById(idCliente).orElseThrow(() -> new HttpException(404, "Cliente con ID " + idCliente + " no encontrado"));
        log.info("Cliente encontrado DNI={} Nombre={} {}", 
                 cliente.getDni(), cliente.getNombre(), cliente.getApellido());
        return cliente;
    }

    // ========== BAJA ==========
    @Transactional(rollbackFor = HttpException.class)
    public Cliente darDeBaja(Long idCliente) {
        log.warn("Dando de baja cliente id={}", idCliente);
        Cliente cliente = clientesRepository.findById(idCliente).orElseThrow(() -> new HttpException(404, "Cliente con ID " + idCliente + " no encontrado"));
        cliente.setActivo(0);

        log.info("Cliente id={} marcado como inactivo.", idCliente);
        log.info("Cancelando solicitudes asociadas.");  

        solicitudesService.cancelarPorCliente(idCliente);

        log.info("Cliente id={} dado de baja correctamente", idCliente);
        return clientesRepository.save(cliente);
    }

    // ========== MODIFICACION ==========
    @Transactional(rollbackFor = HttpException.class)
    public Cliente modificar(Long idCliente, ClienteDTO data) {
        log.info("Modificando cliente id={}", idCliente);
        Cliente cliente =  clientesRepository.findById(idCliente).orElseThrow(() -> new HttpException(404, "Cliente de ID: " + idCliente + " no encontrado"));

        cliente.setNombre(data.getNombre());
        cliente.setApellido(data.getApellido());
        cliente.setDni(data.getDni());
        cliente.setCorreo(data.getCorreo());
        cliente.setTelefono(data.getTelefono());
        cliente.setDireccion(data.getDireccion());

        // JpaRepository utiliza el metodo "save" tanto para crear como para modificar. En este caso lo usamos para crear
        log.info("Cliente id={} modificado correctamente", idCliente);
        return clientesRepository.save(cliente);
        

    }

    @Transactional(rollbackFor = HttpException.class)
    public Solicitud crearSolicitudConCliente(SolicitudDTO data) {
        log.info("Creando solicitud con cliente");
        Cliente cliente = data.cliente().getIdCliente() == null ? this.crear(data.cliente()) : this.obtenerPorId(data.cliente().getIdCliente());
        SolicitudDTO solicitudConCliente = new SolicitudDTO(cliente, data.contenedorDTO(), data.rutaDTO());
        log.info("Cliente id={} listo para crear solicitud", cliente.getIdCliente());
        return solicitudesService.crear(solicitudConCliente);
    }
    
}

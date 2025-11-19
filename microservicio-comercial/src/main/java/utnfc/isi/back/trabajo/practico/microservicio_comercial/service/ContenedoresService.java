package utnfc.isi.back.trabajo.practico.microservicio_comercial.service;

import utnfc.isi.back.trabajo.practico.microservicio_comercial.repository.ContenedoresRepository;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import utnfc.isi.back.trabajo.practico.microservicio_comercial.dto.ContenedorDTO;
import utnfc.isi.back.trabajo.practico.microservicio_comercial.excepciones.HttpException;
import utnfc.isi.back.trabajo.practico.microservicio_comercial.model.Cliente;
import utnfc.isi.back.trabajo.practico.microservicio_comercial.model.Contenedor;

@Service
public class ContenedoresService {

    private static final Logger log = LoggerFactory.getLogger(ContenedoresService.class);
    // Defino como atributo del service el repositorio del cliente.
    private final ContenedoresRepository contenedoresRepository;

    // Asigno el repositorio contenedor a "contenedoresRepository" para poder usar sus metodos desde el service
    ContenedoresService(ContenedoresRepository repoContenedores) {
        this.contenedoresRepository = repoContenedores;
    }

    // Aca defino los metodos de Alta, Baja, Modificacion y Consluta de la BD para la entidad Contenedor

    // ========== ALTA ==========
    @Transactional(rollbackFor = HttpException.class)
    public Contenedor crear(ContenedorDTO data, Cliente cliente) {
        log.info("Creando contenedor para cliente={} peso={} volumen={}",
                 cliente.getIdCliente(), data.peso(), data.volumen());
        Contenedor contenedor = Contenedor.builder()
            .peso(data.peso())
            .volumen(data.volumen())
            .estado(Contenedor.EstadoContenedor.ASIGNADO)
            .cliente(cliente)
            .build();
        // JpaRepository utiliza el metodo "save" tanto para crear como para modificar. En este caso lo usamos para crear
        log.info("Contenedor creado con id={} estado={}", 
        contenedor.getIdContenedor(), contenedor.getEstado());
        return contenedoresRepository.save(contenedor);
    }

    // ========== OBTENER POR ID ==========
    public Contenedor obtenerPorId(Long idContenedor) {
        log.info("Buscando contenedor id={}", idContenedor);
        log.info("Contenedor id={} encontrado", idContenedor);
        return contenedoresRepository.findById(idContenedor).orElseThrow(() -> new HttpException(404, "Cliente con ID " + idContenedor + " no encontrado"));
    }

    // ========== OBTENER POR CLIENTE ==========
    public Page<Contenedor> obtenerPorCliente(Long idCliente, int pagina, int tamañoPagina) {
        log.info("Buscando contenedores para cliente={}", idCliente);
        Pageable pageable = PageRequest.of(pagina, tamañoPagina);
        log.info("Contenedores encontrados con exito para cliente={}",idCliente);
        return contenedoresRepository.findByCliente_IdCliente(idCliente, pageable);
    }

    // ========== ACTUALIZAR ESTADO A EN_TRANSITO ==========
    @Transactional(rollbackFor = HttpException.class)
    public Contenedor iniciarTransitoContenedor(Long idContenedor) {
        log.info("Marcando contenedor id={} como en transito", idContenedor);
        Contenedor contenedor =  contenedoresRepository.findById(idContenedor).orElseThrow(() -> new HttpException(404, "Cliente de ID: " + idContenedor + " no encontrado"));
        
        contenedor.getEstado().validarTransicion(Contenedor.EstadoContenedor.EN_TRANSITO);

        contenedor.setEstado(Contenedor.EstadoContenedor.EN_TRANSITO);
        log.info("Contenedor id={} cambio estado a en transito", idContenedor);
        return contenedoresRepository.save(contenedor);
    }

    // ========== ACTUALIZAR ESTADO A ENTREGADO ==========
    @Transactional(rollbackFor = HttpException.class)
    public Contenedor entregarContenedor(Long idContenedor) {
        log.info("Marcando contenedor id={} como entregado", idContenedor);
        Contenedor contenedor =  contenedoresRepository.findById(idContenedor).orElseThrow(() -> new HttpException(404, "Cliente de ID: " + idContenedor + " no encontrado"));
        
        contenedor.getEstado().validarTransicion(Contenedor.EstadoContenedor.ENTREGADO);
        
        contenedor.setEstado(Contenedor.EstadoContenedor.ENTREGADO);
        log.info("Contenedor id={} entregado con exito", idContenedor);
        return contenedoresRepository.save(contenedor);
    }

    // ========== ELIMINAR ==========
    @Transactional(rollbackFor = HttpException.class)
    public Contenedor eliminar(Long idContenedor) {
        log.warn("Cancelando contenedor id={}", idContenedor);
        Contenedor contenedor =  contenedoresRepository.findById(idContenedor).orElseThrow(() -> new HttpException(404, "Cliente de ID: " + idContenedor + " no encontrado"));
        
        contenedor.getEstado().validarTransicion(Contenedor.EstadoContenedor.CANCELADO);
        
        contenedor.setEstado(Contenedor.EstadoContenedor.CANCELADO);
        log.info("Contenedor id={} CANCELADO", idContenedor);
        return contenedoresRepository.save(contenedor);
    }

    // ========== BAJA POR CLIENTE ==========
    @Transactional(rollbackFor = HttpException.class)
    public void darDeBajaPorCliente(Long idCliente) {
        log.warn("Cancelando TODOS los contenedores del cliente={}", idCliente);
        List<Contenedor> contenedores = contenedoresRepository.findByCliente_IdCliente(idCliente);
        for (Contenedor contenedor : contenedores) {
            try{
                this.eliminar(contenedor.getIdContenedor());
                log.info("Contenedor id={} cancelado", contenedor.getIdContenedor());
            }catch(HttpException e){
                log.error("Error al cancelar contenedor id={} : {}", 
                          contenedor.getIdContenedor(), e.getMessage());
                continue;
            }
        }
    }

    public void esEstadoTerminal(Contenedor.EstadoContenedor estado) {
        log.info("Validando si es terminal");
        if (estado == Contenedor.EstadoContenedor.ENTREGADO || estado == Contenedor.EstadoContenedor.CANCELADO) {
            throw new HttpException(400, "El contenedor se encuentra en un estado final y ya fue entregado o cancelado");
        }
    }
}

package utnfc.isi.back.trabajo.practico.microservicio_comercial.controller;

import org.slf4j.Logger; 
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;

import utnfc.isi.back.trabajo.practico.microservicio_comercial.service.ClienteService;
import utnfc.isi.back.trabajo.practico.microservicio_comercial.dto.ClienteDTO;
import utnfc.isi.back.trabajo.practico.microservicio_comercial.model.Cliente;
import utnfc.isi.back.trabajo.practico.microservicio_comercial.security.ClienteSecurity;

import java.util.List;

@RestController
@RequestMapping("/clientes")
public class ClienteController {
    private static final Logger log = LoggerFactory.getLogger(ClienteController.class);

    private final ClienteService clienteService;
    private final ClienteSecurity clienteSecurity;

    ClienteController(ClienteService servicioCliente, ClienteSecurity clienteSecurity) {
        this.clienteService = servicioCliente;
        this.clienteSecurity = clienteSecurity;
    }

    // Ruta para obtener TODOS los clientes
    @GetMapping("/todos")
    public ResponseEntity<List<Cliente>> obtenerTodos() {
        log.info("GET /clientes/todos - Buscando todos los clientes activos");
        List<Cliente> cliente = clienteService.obtenerTodos();
        log.info("Cantidad encontrada={}", cliente.size());
        return ResponseEntity.status(200).body(cliente);
    }

    // Ruta para obtener UN CLIENTE por ID
    @GetMapping("/{idCliente}")
    public ResponseEntity<Cliente> obtenerPorId(@PathVariable Long idCliente) {
        log.info("GET /clientes/{} - Solicitando datos del cliente", idCliente);
        clienteSecurity.validarId(idCliente);
        Cliente cliente = clienteService.obtenerPorId(idCliente);
        log.info("Cliente: {} encontrado", idCliente);
        return ResponseEntity.status(200).body(cliente);
    }

    @PostMapping("")
    public ResponseEntity<Cliente> crearCliente(@RequestBody Cliente cliRequest) {
        log.info("POST /clientes - Creando nuevo cliente DNI={}", cliRequest.getDni());
        Cliente cliente = clienteService.crear(cliRequest);
        log.info("Cliente creado con id={}", cliente.getIdCliente());
        return ResponseEntity.status(201).body(cliente);
    }
    // Ruta para DAR DE BAJA LOGICA un cliente. Setea el atributo "activo" a 0
    @DeleteMapping("/{idCliente}")
    public ResponseEntity<Cliente> darDeBaja(@PathVariable Long idCliente) {
        log.warn("DELETE /clientes/{} - Dando de baja cliente", idCliente);
        clienteSecurity.validarId(idCliente);
        Cliente cliente = clienteService.darDeBaja(idCliente);
        log.info("Cliente dado de baja correctamente", idCliente);
        return ResponseEntity.status(200).body(cliente);
    }

    // Ruta para MODIFICAR un cliente
    @PutMapping("/{idCliente}")
    public ResponseEntity<Cliente> modificarCliente(@RequestBody 
    ClienteDTO cliRequest, @PathVariable Long idCliente) {
        log.info("PUT /clientes/{} - Modificando cliente", idCliente);
        clienteSecurity.validarId(idCliente);
        Cliente cliente = clienteService.modificar(idCliente, cliRequest);
        log.info("Cliente modificado correctamente", idCliente);
        return ResponseEntity.status(201).body(cliente);
    }
}

// @PathVariable Extrae valores dinámicos de la URL y los asigna a parámetros del método.
// @RequestBody Extrae los datos del objeto que se mandan en la peticion.

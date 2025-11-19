package utnfc.isi.back.trabajo.practico.microservicio_comercial.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import utnfc.isi.back.trabajo.practico.microservicio_comercial.service.ContenedoresService;
import utnfc.isi.back.trabajo.practico.microservicio_comercial.model.Contenedor;
import utnfc.isi.back.trabajo.practico.microservicio_comercial.security.ClienteSecurity;

// -> Defino el protocolo de comunicacion con el que va a trabajar y es REST.
// -> No hace falta declarar @Controller debido a que RestController ya lo trae incluido implicitamente.
@RestController

// -> Defino el prefijo de ruta para todos los endpoints de Contenedores
@RequestMapping("/contenedores")
public class ContenedoresController {

    private static final Logger log = LoggerFactory.getLogger(ContenedoresController.class);

    // -> Declaro una referencia a contenedoresService. Uso inyeccion de dependencias. Encapsulo la logica de negocio de contenedoresService
    private final ContenedoresService contenedoresService;
    private final ClienteSecurity clienteSecurity;

    // -> Inyecto la responsabilidad de ContenedoresService al construir el controlador
    ContenedoresController(ContenedoresService servicioContenedor, ClienteSecurity clienteSecurity) {

        // -> Guardo la dependencia (servicioContenedor) dentro del atributo final para poder usar sus métodos
        this.contenedoresService = servicioContenedor;
        this.clienteSecurity = clienteSecurity;
    }

    // Ruta para OBTENER UN CONTENEDOR por ID
    @GetMapping("/{idContenedor}")
    public ResponseEntity<Contenedor> obtenerPorId(@PathVariable Long idContenedor) {
        log.info("GET /contenedores/{} - solicitando contenedor", idContenedor);
        Contenedor contenedor = contenedoresService.obtenerPorId(idContenedor);
        log.info("contenedor= {} encontrado", idContenedor);
        return ResponseEntity.status(200).body(contenedor);
    }

    // Ruta para OBTENER TODOS LOS CONTENEDORES de un Cliente
    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<Page<Contenedor>> obtenerPorCliente(
        @PathVariable Long idCliente,
        @RequestParam(defaultValue = "0") int pagina,
        @RequestParam(defaultValue = "10") int tamañoPagina
    ) {
        log.info("GET /contenedores/cliente/{} - buscando contenedores",idCliente);
        clienteSecurity.validarId(idCliente);
        Page<Contenedor> contenedores = contenedoresService.obtenerPorCliente(idCliente, pagina, tamañoPagina);
        log.info("encontrados {} contenedores",contenedores.getContent().size());
        return ResponseEntity.status(200).body(contenedores);
    }
}

// @PathVariable Extrae valores dinámicos de la URL y los asigna a parámetros del método.
// @RequestBody Extrae los datos del objeto que se mandan en la peticion.

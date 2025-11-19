package utnfc.isi.back.trabajo.practico.microservicio_logistico.controller;

import java.util.List;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import utnfc.isi.back.trabajo.practico.microservicio_logistico.service.EnlacesService;

import utnfc.isi.back.trabajo.practico.microservicio_logistico.model.Enlace;

@RestController
@RequestMapping("/enlaces")
public class EnlacesController {

    private static final Logger log = LoggerFactory.getLogger(EnlacesController.class);

    private final EnlacesService enlacesService;

    public EnlacesController(EnlacesService enlacesService) {
        this.enlacesService = enlacesService;
    }

    @GetMapping("/todos")
    public List<Enlace> getAllEnlaces() {
        log.info("GET /enlaces/todos buscando enlaces...");
        return enlacesService.obtenerTodos();
    }

    @GetMapping("/contar")
    public ResponseEntity<Long> contarEnlaces() {
        log.info("GET /enlaces/contar contando enlaces...");
        long cantidad = enlacesService.contar();
        log.info("Cantidad de enlaces obtenida con éxito");
        return ResponseEntity.status(200).body(cantidad);
    }
}

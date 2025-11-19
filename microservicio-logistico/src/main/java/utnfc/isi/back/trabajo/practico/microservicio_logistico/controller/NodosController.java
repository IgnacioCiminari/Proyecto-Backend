package utnfc.isi.back.trabajo.practico.microservicio_logistico.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import utnfc.isi.back.trabajo.practico.microservicio_logistico.service.NodosService;

import utnfc.isi.back.trabajo.practico.microservicio_logistico.model.Nodo;

@RestController
@RequestMapping("/nodos")
public class NodosController {

    private static final Logger log = LoggerFactory.getLogger(NodosController.class);

    private final NodosService nodosService;

    public NodosController(NodosService nodosService) {
        this.nodosService = nodosService;
    }

    @GetMapping("/todos")
    public ResponseEntity<List<Nodo>> getAllNodos() {
        log.info("GET /nodos/todos buscando nodos...");
        return ResponseEntity.status(200).body(nodosService.obtenerTodos());
    }

    @GetMapping("/contar")
    public ResponseEntity<Long> contarNodos() {
        log.info("GET /nodos/contar contando nodos...");
        long cantidad = nodosService.contar();
        log.info("Cantidad de nodos obtenida con éxito");
        return ResponseEntity.status(200).body(cantidad);
    }
}

package utnfc.isi.back.trabajo.practico.microservicio_logistico.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.dto.DepositoDTO;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.model.Deposito;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.service.DepositosService;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/depositos")
@Validated
public class DepositosController {
    private static final Logger log = LoggerFactory.getLogger(DepositosController.class);

    private final DepositosService depositosService;

    public DepositosController(DepositosService depositosService) {
        this.depositosService = depositosService;
    }

    @GetMapping("/{idDeposito}")
    public ResponseEntity<Deposito> obtenerDepositoPorId(
        @PathVariable @Positive Long idDeposito
    ) {
        log.info("GET /depositos/{} buscando depósito...", idDeposito);
        Deposito deposito = depositosService.findById(idDeposito);

        log.info("Depósito {} encontrado con éxito", idDeposito);
        return ResponseEntity.status(200).body(deposito);
    }

    @GetMapping("/todos")
    public ResponseEntity<Page<Deposito>> obtenerTodosLosDepositos(
        @RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
        @RequestParam(defaultValue = "10") @Positive int tamañoPagina
    ) {
        log.info("GET /depositos/todos buscando depósitos...");
        Page<Deposito> depositos = depositosService.obtenerTodos(pagina, tamañoPagina);

        log.info("Depósitos recuperados con éxito");
        return ResponseEntity.status(200).body(depositos);
    }

    @GetMapping("/contar")
    public ResponseEntity<Long> contarDepositos() {
        log.info("GET /depositos/contar contando depósitos...");
        long cantidad = depositosService.contar();

        log.info("Cantidad de depósitos obtenida con éxito");
        return ResponseEntity.status(200).body(cantidad);
    }

    @PostMapping("")
    public ResponseEntity<Deposito> crearDeposito(
        @Valid @RequestBody DepositoDTO data
    ) {
        log.info("POST /depositos creando depósito...");
        Deposito deposito = depositosService.crear(data);

        log.info("Depósito {} creado con éxito", deposito.getIdDeposito());
        return ResponseEntity.status(201).body(deposito);
    }

    @PostMapping("/varios")
    public ResponseEntity<List<Deposito>> crearVariosDepositos(
        @RequestBody List<DepositoDTO> datos
    ) {
        log.info("POST /depositos/varios creando varios depósitos...");
        List<Deposito> depositos = depositosService.crearVarios(datos);

        log.info("Varios depósitos creados con éxito");
        return ResponseEntity.status(201).body(depositos);
    }

    @PutMapping("/{idDeposito}/costo-estadia")
    public ResponseEntity<Deposito> actualizarCostoEstadiaDeposito(
        @PathVariable @Positive Long idDeposito,
        @RequestBody Map<String, Double> nuevoCosto
    ) {
        log.info("PUT /depositos/{}/costo-estadia   actualizando costo...", idDeposito);
        Deposito deposito = depositosService.actualizarCostoEstadia(idDeposito, nuevoCosto);

        log.info("Depósito {} actualizado con éxito", idDeposito);
        return ResponseEntity.status(200).body(deposito);
    }
}

package utnfc.isi.back.trabajo.practico.microservicio_logistico.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import utnfc.isi.back.trabajo.practico.microservicio_logistico.dto.DepositoDTO;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.excepciones.HttpException;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.model.Deposito;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.repository.DepositosRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DepositosService {

    private static final Logger log = LoggerFactory.getLogger(DepositosService.class);

    private final DepositosRepository depositosRepository;
    private final NodosService nodosService;

    public DepositosService(DepositosRepository depositosRepository, NodosService nodosService) {
        this.depositosRepository = depositosRepository;
        this.nodosService = nodosService;
    }

    // Crear nuevo depósito
    @Transactional(rollbackFor = HttpException.class)
    public Deposito crear(DepositoDTO data) {
        log.info("Creando depósito...");

        Deposito deposito = new Deposito();
        deposito.setNombre(data.nombre());
        deposito.setDireccion(data.direccion());
        deposito.setLatitud(data.latitud());
        deposito.setLongitud(data.longitud());
        deposito.setCostoEstadiaDiario(data.costoEstadiaDiario());
        depositosRepository.save(deposito);

        nodosService.crearNodoParaDeposito(deposito);

        log.info("Depósito {} creado con éxito", deposito.getIdDeposito());

        return deposito;
    }

    public long contar() {
        log.info("Contando depósitos...");
        return depositosRepository.count();
    }

    @Transactional(rollbackFor = HttpException.class)
    public List<Deposito> crearVarios(List<DepositoDTO> datos) {

        log.info("Creando varios depósitos...");
        List<Deposito> depositosCreados = new ArrayList<>();

        for (DepositoDTO data : datos) {
            Deposito deposito = crear(data);
            depositosCreados.add(deposito);
        }

        log.info("Varios depósitos creados con éxito");

        return depositosCreados;
    }

    // Obtener depósito por ID
    public Deposito findById(Long idDeposito) {

        log.info("Buscando depósito {}", idDeposito); // ← agregado

        Deposito deposito = depositosRepository.findById(idDeposito)
                .orElseThrow(() -> new HttpException(404,
                 "Depósito con ID " + idDeposito + " no encontrado"));

        log.info("Depósito {} encontrado con éxito", idDeposito); // ← agregado
        return deposito;
    }

    // Obtener todos paginados
    public Page<Deposito> obtenerTodos(int pagina, int tamañoPagina) {
        log.info("Buscando todos los depósitos...");
        Pageable pageable = PageRequest.of(pagina, tamañoPagina);
        return depositosRepository.findAll(pageable);
    }

    // Actualizar costo de estadía
    @Transactional(rollbackFor = HttpException.class)
    public Deposito actualizarCostoEstadia(Long idDeposito, Map<String, Double> nuevoCosto) {

        log.info("Actualizando costo de estadía del depósito {}", idDeposito);

        Deposito deposito = depositosRepository.findById(idDeposito)
                .orElseThrow(() -> new HttpException(404, "Depósito con ID " + idDeposito + " no encontrado"));
        if (nuevoCosto == null || !nuevoCosto.containsKey("costoEstadiaDiario")) {
            throw new HttpException(400, "Debe incluir el campo 'costoEstadiaDiario'");
        }

        deposito.setCostoEstadiaDiario(nuevoCosto.get("costoEstadiaDiario"));
        depositosRepository.save(deposito);
        log.info("Depósito {} actualizado con éxito", idDeposito);
        return deposito;
    }
}

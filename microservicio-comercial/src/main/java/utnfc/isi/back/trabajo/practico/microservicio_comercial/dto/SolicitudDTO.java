package utnfc.isi.back.trabajo.practico.microservicio_comercial.dto;

import utnfc.isi.back.trabajo.practico.microservicio_comercial.model.Cliente;

public record SolicitudDTO(
    Cliente cliente,
    ContenedorDTO contenedorDTO,
    RutaDTO rutaDTO
) {}

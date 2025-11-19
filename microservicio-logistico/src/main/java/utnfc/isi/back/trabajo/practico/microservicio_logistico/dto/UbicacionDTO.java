package utnfc.isi.back.trabajo.practico.microservicio_logistico.dto;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record UbicacionDTO(
    Double latitud,
    Double longitud,
    String direccion,
    LocalDateTime fechaHora
) {
} 

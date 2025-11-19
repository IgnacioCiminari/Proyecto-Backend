package utnfc.isi.back.trabajo.practico.microservicio_comercial.dto;

import java.time.LocalDateTime;

public record UbicacionDTO(
    Long idContenedor,
    String estado,
    Double latitud,
    Double longitud,
    String direccion,
    LocalDateTime fechaHora
) {
} 

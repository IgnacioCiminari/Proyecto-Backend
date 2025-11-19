package utnfc.isi.back.trabajo.practico.microservicio_logistico.dto;

import jakarta.validation.constraints.NotBlank;

public record TransportistaDTO(
    @NotBlank
    String nombre,

    @NotBlank
    String apellido,

    @NotBlank
    String telefono,

    @NotBlank
    String direccion,
    
    @NotBlank
    String correo
) {}

package utnfc.isi.back.trabajo.practico.microservicio_comercial.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {
    private String nombre;
    private String apellido;
    private Integer dni;
    private String correo;
    private String telefono;
    private String direccion;
}


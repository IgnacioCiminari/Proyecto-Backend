package utnfc.isi.back.trabajo.practico.microservicio_logistico.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TRANSPORTISTAS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transportista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TRANSP_ID")
    private Long idTransportista;

    @NotBlank
    @Column(name = "NOMBRE", nullable = false, length = 255)
    private String nombre;

    @NotBlank
    @Column(name = "APELLIDO", nullable = false, length = 255)
    private String apellido;

    @NotBlank
    @Column(name = "TELEFONO", length = 20)
    private String telefono;

    @NotBlank
    @Email
    @Column(name = "CORREO", nullable = false, length = 255)
    private String correo;

    @NotBlank
    @Column(name = "DIRECCION", nullable = false, length = 255)
    private String direccion;

    @NotNull
    @Column(name = "ACTIVO", nullable = false)
    private Boolean activo = true;
}

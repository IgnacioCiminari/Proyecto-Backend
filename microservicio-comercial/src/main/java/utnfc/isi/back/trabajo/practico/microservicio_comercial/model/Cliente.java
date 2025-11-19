package utnfc.isi.back.trabajo.practico.microservicio_comercial.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CLIENTES")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CLI_ID")
    private Long idCliente;

    @Column(name = "NOMBRE", nullable = false, length = 255)
    private String nombre;

    @Column(name = "APELLIDO", nullable = false, length = 255)
    private String apellido;

    @Column(name = "DNI", nullable = false, length = 8)
    private Integer dni;

    @Column(name = "CORREO", nullable = false, length = 255, unique = true)
    private String correo;

    @Column(name = "TELEFONO", length = 20)
    private String telefono;

    @Column(name = "DIRECCION", length = 500)
    private String direccion;

    // Determina si el cliente esta activo o no. 1 si lo esta, 0 si no lo esta
    @Builder.Default
    @Column(name = "ACTIVO", length = 1)
    private Integer activo = 1;
}

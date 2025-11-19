package utnfc.isi.back.trabajo.practico.microservicio_logistico.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "DEPOSITOS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Deposito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DEPO_ID")
    private Long idDeposito;

    @NotBlank
    @Column(name = "NOMBRE", nullable = false, length = 255)
    private String nombre;

    @NotBlank
    @Column(name = "DIRECCION", nullable = false, length = 255)
    private String direccion;

    @NotNull
    @Column(name = "LATITUD", nullable = false)
    @DecimalMin(value = "-90.0", inclusive = true)
    @DecimalMax(value = "90.0", inclusive = true)
    private Double latitud;

    @NotNull
    @Column(name = "LONGITUD", nullable = false)
    @DecimalMin(value = "-180.0", inclusive = true)
    @DecimalMax(value = "180.0", inclusive = true)
    private Double longitud;

    @NotNull
    @Positive
    @Column(name = "COSTO_ESTADIA_DIARIO", nullable = false)
    private Double costoEstadiaDiario;

    public double calcularCostoEstadia(long diasEstadia) {
        return diasEstadia * this.costoEstadiaDiario;
    }
}

package utnfc.isi.back.trabajo.practico.microservicio_logistico.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CAMIONES")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Camion {

    @Id
    @NotBlank
    @Column(name = "DOMINIO", nullable = false, length = 100)
    private String dominio;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_TRANSPORTISTA", nullable = false)
    @NotNull
    private Transportista transportista;

    @NotNull
    @Positive
    @Column(name = "CAPACIDAD_PESO", nullable = false)
    private Double capacidadPeso;

    @NotNull
    @Positive
    @Column(name = "CAPACIDAD_VOLUMEN", nullable = false)
    private Double capacidadVolumen;

    @NotNull
    @Column(name = "DISPONIBILIDAD", nullable = false)
    private Boolean disponibilidad;

    @NotNull
    @Positive
    @Column(name = "CONSUMO_BASE_X_KM", nullable = false)
    private Double costoBaseXKm;

    @NotNull
    @Positive
    @Column(name = "CONSUMO_X_KM", nullable = false)
    private Double consumoXKm;

    @Builder.Default
    @Column(name = "ACTIVO", nullable = false, length = 1)
    private Integer activo = 1;

    public Double calcularCostoViaje(Double distancia, Double precioCombustible) {
        Double costoDistancia = costoBaseXKm * distancia;
        Double costoCombustible = consumoXKm * distancia * precioCombustible;
        return costoDistancia + costoCombustible;
    }
}

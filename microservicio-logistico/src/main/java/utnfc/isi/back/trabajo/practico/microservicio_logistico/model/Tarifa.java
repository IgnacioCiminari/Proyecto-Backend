package utnfc.isi.back.trabajo.practico.microservicio_logistico.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "TARIFAS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tarifa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TARIFA_ID")
    private Long idTarifa;

    @NotNull
    @Positive
    @Column(name = "VOLUMEN_MIN", nullable = false)
    private Double volumenMin;

    @NotNull
    @Positive
    @Column(name = "VOLUMEN_MAX", nullable = false)
    private Double volumenMax;

    @NotNull
    @Positive
    @Column(name = "COSTO_BASE_X_KM", nullable = false)
    private Double costoBaseXKm;

    @NotNull
    @Positive
    @Column(name = "CONSUMO_X_KM", nullable = false)
    private Double consumoXKm;

    @NotNull
    @Positive
    @Column(name = "PRECIO_COMBUSTIBLE", nullable = false)
    private Double precioCombustible;

    @NotNull
    @Column(name = "FECHA_DESDE", nullable = false)
    private LocalDateTime fechaDesde;

    @Column(name = "FECHA_HASTA")
    private LocalDateTime fechaHasta;

    public Double calcularCostoTotal(Double distancia) {
        Double costoDistancia = costoBaseXKm * distancia;
        Double costoCombustible = consumoXKm * distancia * precioCombustible;
        return costoDistancia + costoCombustible;
    }
}

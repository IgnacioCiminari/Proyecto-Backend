package utnfc.isi.back.trabajo.practico.microservicio_logistico.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "RUTAS")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RUTA_ID")
    private Long idRuta;

    @NotNull
    @Column(name = "ID_SOLICITUD", nullable = false)
    private Long idSolicitud;

    @NotNull
    @Column(name = "ORIGEN_LATITUD", nullable = false)
    @DecimalMin(value = "-90.0", inclusive = true)
    @DecimalMax(value = "90.0", inclusive = true)
    private double origenLatitud;

    @NotNull
    @Column(name = "ORIGEN_LONGITUD", nullable = false)
    @DecimalMin(value = "-180.0", inclusive = true)
    @DecimalMax(value = "180.0", inclusive = true)
    private double origenLongitud;

    @NotNull
    @Column(name = "DESTINO_LATITUD", nullable = false)
    @DecimalMin(value = "-90.0", inclusive = true)
    @DecimalMax(value = "90.0", inclusive = true)
    private double destinoLatitud;

    @NotNull
    @Column(name = "DESTINO_LONGITUD", nullable = false)
    @DecimalMin(value = "-180.0", inclusive = true)
    @DecimalMax(value = "180.0", inclusive = true)
    private double destinoLongitud;

    @NotNull
    @Positive
    @Column(name = "CANTIDAD_TRAMOS")
    private int cantidadTramos;

    @NotNull
    @PositiveOrZero
    @Column(name = "CANTIDAD_DEPOSITOS")
    private int cantidadDepositos;
}

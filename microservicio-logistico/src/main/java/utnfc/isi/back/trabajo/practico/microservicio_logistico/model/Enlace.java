package utnfc.isi.back.trabajo.practico.microservicio_logistico.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "ENLACES")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Enlace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ENLACE_ID")
    private Long idEnlace;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "NODO_ORIGEN_ID", nullable = false)
    @NotNull
    private Nodo origen;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "NODO_DESTINO_ID", nullable = false)
    @NotNull
    private Nodo destino;

    @Column(name = "DISTANCIA", nullable = false)
    @Positive
    private Double distancia;

    @Column(name = "TIEMPO_ESTIMADO", nullable = false)
    @Positive
    private Integer tiempoEstimado; // en segundos
}

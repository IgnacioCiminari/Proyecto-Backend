package utnfc.isi.back.trabajo.practico.microservicio_logistico.model;

import java.util.List;
import java.util.Objects;

import io.micrometer.common.lang.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "NODOS")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Nodo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NODO_ID")
    private Long idNodo;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_DEPOSITO")
    @Nullable
    private Deposito deposito;

    @Column(name = "LATITUD", nullable = false)
    @NotNull
    @DecimalMin(value = "-90.0", inclusive = true)
    @DecimalMax(value = "90.0", inclusive = true)
    private Double latitud;

    @Column(name = "LONGITUD", nullable = false)
    @NotNull
    @DecimalMin(value = "-180.0", inclusive = true)
    @DecimalMax(value = "180.0", inclusive = true)
    private Double longitud;

    @Transient
    private double g; // Costo desde el nodo inicio
    
    @Transient
    private double h; // Heurística al nodo destino
    
    @Transient
    private double f; // Costo total (g + h)

    @Transient
    private Nodo padre; // Para reconstruir el camino

    @Transient
    private List<Enlace> enlaces;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Nodo nodo = (Nodo) o;
        // Comparamos solo el ID o coordenadas si el ID no existe
        return Objects.equals(idNodo, nodo.idNodo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idNodo);
    }
}

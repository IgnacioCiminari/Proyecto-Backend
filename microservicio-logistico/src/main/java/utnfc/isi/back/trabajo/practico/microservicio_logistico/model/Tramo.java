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
import utnfc.isi.back.trabajo.practico.microservicio_logistico.excepciones.HttpException;

import java.time.LocalDateTime;

import io.micrometer.common.lang.Nullable;

import java.time.Duration;


@Entity
@Table(name = "TRAMO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tramo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TRAMO_ID")
    private Long idTramo;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_RUTA", nullable = false)
    private Ruta ruta;

    @Nullable
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "DOMINIO_CAMION")
    private Camion camion;

    @Nullable
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ORIGEN")
    private Deposito origen;

    @Nullable
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "DESTINO")
    private Deposito destino;
    
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_TARIFA")
    private Tarifa tarifa;

    public enum EstadoTramo {
        PENDIENTE {
            @Override
            public boolean puedeTransicionarA(EstadoTramo nuevoEstado) {
                return nuevoEstado == PLANIFICADO;
            }

            @Override
            public void validarTransicion(EstadoTramo nuevoEstado) {
                if (!puedeTransicionarA(nuevoEstado)) {
                    throw new HttpException(409, "Un tramo PENDIENTE solo puede pasar a PLANIFICADO");
                }
            }
        },
        PLANIFICADO {
            @Override
            public boolean puedeTransicionarA(EstadoTramo nuevoEstado) {
                return nuevoEstado == INICIADO || nuevoEstado == PENDIENTE;
            }

            @Override
            public void validarTransicion(EstadoTramo nuevoEstado) {
                if (!puedeTransicionarA(nuevoEstado)) {
                    throw new HttpException(409, "Un tramo PLANIFICADO solo puede pasar a PENDIENTE o INICIADO");
                }
            }
        },
        INICIADO {
            @Override
            public boolean puedeTransicionarA(EstadoTramo nuevoEstado) {
                return nuevoEstado == TERMINADO;
            }

            @Override
            public void validarTransicion(EstadoTramo nuevoEstado) {
                if (!puedeTransicionarA(nuevoEstado)) {
                    throw new HttpException(409, "Un tramo INICIADO solo puede pasar a TERMINADO");
                }
            }
        },
        TERMINADO {
            @Override
            public boolean puedeTransicionarA(EstadoTramo nuevoEstado) {
                return false; // Estado final
            }

            @Override
            public void validarTransicion(EstadoTramo nuevoEstado) {
                throw new HttpException(409, "Un tramo TERMINADO no puede cambiar de estado");
            }
        };

        public abstract boolean puedeTransicionarA(EstadoTramo nuevoEstado);
        public abstract void validarTransicion(EstadoTramo nuevoEstado);
    }

    @NotNull
    @Column(name = "ESTADO")
    private EstadoTramo estado;

    @NotNull
    @Positive
    @Column(name = "DISTANCIA")
    private double distancia;

    @NotNull
    @Positive
    @Column(name = "TIEMPO_ESTIMADO")
    private Integer tiempoEstimado;

    @NotNull
    @Positive
    @Column(name = "COSTO_APROXIMADO")
    private double costoAproximado;

    @Positive
    @Column(name = "COSTO_REAL")
    private double costoReal;

    @Column(name = "FECHA_HORA_INICIO")
    private LocalDateTime fechaHoraInicio;

    @Column(name = "FECHA_HORA_FIN")
    private LocalDateTime fechaHoraFin;

    public void calcularCostoReal(LocalDateTime fechaHoraFinAnterior, Double precioCombustible) {
        if (this.fechaHoraInicio != null && fechaHoraFinAnterior != null) {
            long diasEnteros = (long) Math.ceil(Duration.between(fechaHoraFinAnterior, this.fechaHoraInicio).toHours() / 24.0);
            this.costoReal = this.origen == null ? 0 : this.origen.calcularCostoEstadia(diasEnteros);
        }
        this.costoReal += this.camion.calcularCostoViaje(this.distancia, precioCombustible);
    }
}

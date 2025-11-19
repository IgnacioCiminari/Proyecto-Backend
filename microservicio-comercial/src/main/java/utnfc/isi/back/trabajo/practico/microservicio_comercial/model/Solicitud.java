package utnfc.isi.back.trabajo.practico.microservicio_comercial.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import utnfc.isi.back.trabajo.practico.microservicio_comercial.excepciones.HttpException;

@Entity
@Table(name = "SOLICITUDES")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Solicitud {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SOLI_ID")
    private Long idSolicitud;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_CLIENTE", nullable = false)
    private Cliente cliente;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_CONTENEDOR", nullable = false, unique = true)
    private Contenedor contenedor;

    public enum EstadoSolicitud {
        PENDIENTE {
            @Override
            public boolean puedeTransicionarA(EstadoSolicitud nuevoEstado) {
                return nuevoEstado == PLANIFICADA || nuevoEstado == CANCELADA;
            }
            
            @Override
            public void validarTransicion(EstadoSolicitud nuevoEstado) {
                if (!puedeTransicionarA(nuevoEstado)) {
                    throw new HttpException(409, 
                        "Una solicitud PENDIENTE solo puede pasar a PLANIFICADA o CANCELADA");
                }
            }
        },
        
        PLANIFICADA {
            @Override
            public boolean puedeTransicionarA(EstadoSolicitud nuevoEstado) {
                return nuevoEstado == EN_TRANSITO || nuevoEstado == PENDIENTE || nuevoEstado == CANCELADA;
            }
            
            @Override
            public void validarTransicion(EstadoSolicitud nuevoEstado) {
                if (!puedeTransicionarA(nuevoEstado)) {
                    throw new HttpException(409, 
                        "Una solicitud PLANIFICADA solo puede pasar a EN_TRANSITO o CANCELADA");
                }
            }
        },

        EN_TRANSITO {
            @Override
            public boolean puedeTransicionarA(EstadoSolicitud nuevoEstado) {
                return nuevoEstado == ENTREGADA || nuevoEstado == CANCELADA;
            }
            
            @Override
            public void validarTransicion(EstadoSolicitud nuevoEstado) {
                if (!puedeTransicionarA(nuevoEstado)) {
                    throw new HttpException(409,
                        "Una solicitud EN_TRANSITO solo puede pasar a ENTREGADA o CANCELADA");
                }
            }
        },

        ENTREGADA {
            @Override
            public boolean puedeTransicionarA(EstadoSolicitud nuevoEstado) {
                return false; // Estado final
            }
            
            @Override
            public void validarTransicion(EstadoSolicitud nuevoEstado) {
                throw new HttpException(409, 
                    "Una solicitud ENTREGADA no puede cambiar de estado");
            }
        },
        
        CANCELADA {
            @Override
            public boolean puedeTransicionarA(EstadoSolicitud nuevoEstado) {
                return false; // Estado final
            }
            
            @Override
            public void validarTransicion(EstadoSolicitud nuevoEstado) {
                throw new HttpException(409, 
                    "Una solicitud CANCELADA no puede cambiar de estado");
            }
        };
        
        public abstract boolean puedeTransicionarA(EstadoSolicitud nuevoEstado);
        public abstract void validarTransicion(EstadoSolicitud nuevoEstado);
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 50)
    private EstadoSolicitud estado;

    @Column(name = "COSTO_ESTIMADO")
    private Double costoEstimado;

    @Column(name = "TIEMPO_ESTIMADO")
    private Integer tiempoEstimado;

    @Column(name = "FECHA_HORA_SALIDA")
    private LocalDateTime fechaHoraSalida;

    @Column(name = "COSTO_FINAL")
    private Double costoFinal;

    @Column(name = "TIEMPO_REAL")
    private Integer tiempoReal;
}

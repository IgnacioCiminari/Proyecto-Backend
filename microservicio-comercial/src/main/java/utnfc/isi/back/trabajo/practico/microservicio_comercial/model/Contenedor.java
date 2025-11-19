package utnfc.isi.back.trabajo.practico.microservicio_comercial.model;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import utnfc.isi.back.trabajo.practico.microservicio_comercial.excepciones.HttpException;

@Entity
@Table(name = "CONTENEDORES")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contenedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CONT_ID")
    private Long idContenedor;

    @Column(name = "PESO", nullable = false)
    private Double peso;

    @Column(name = "VOLUMEN", nullable = false)
    private Double volumen;

    public enum EstadoContenedor {
        ASIGNADO {
            @Override
            public boolean puedeTransicionarA(EstadoContenedor nuevoEstado) {
                return nuevoEstado == EN_TRANSITO || nuevoEstado == CANCELADO;
            }
            
            @Override
            public void validarTransicion(EstadoContenedor nuevoEstado) {
                if (!puedeTransicionarA(nuevoEstado)) {
                    throw new HttpException(409, 
                        "Un contenedor ASIGNADO solo puede pasar a EN_TRANSITO o CANCELADO");
                }
            }
        },
        EN_TRANSITO{
            @Override
            public boolean puedeTransicionarA(EstadoContenedor nuevoEstado) {
                return nuevoEstado == ENTREGADO || nuevoEstado == CANCELADO;
            }

            @Override
            public void validarTransicion(EstadoContenedor nuevoEstado) {
                if (!puedeTransicionarA(nuevoEstado)) {
                    throw new HttpException(409,
                        "Un contenedor EN_TRANSITO solo puede pasar a ENTREGADO o CANCELADO");
                }
            }
        },
        ENTREGADO{
            @Override
            public boolean puedeTransicionarA(EstadoContenedor nuevoEstado) {
                return false; // Estado final
            }

            @Override
            public void validarTransicion(EstadoContenedor nuevoEstado) {
                throw new HttpException(409,
                    "Un contenedor ENTREGADO no puede cambiar de estado");
            }
        },
        CANCELADO{
            @Override
            public boolean puedeTransicionarA(EstadoContenedor nuevoEstado) {
                return false; // Estado final
            }

            @Override
            public void validarTransicion(EstadoContenedor nuevoEstado) {
                throw new HttpException(409,
                    "Un contenedor CANCELADO no puede cambiar de estado");
            }
        };

        public abstract boolean puedeTransicionarA(EstadoContenedor nuevoEstado);
        public abstract void validarTransicion(EstadoContenedor nuevoEstado);
    }
    
    @Column(name = "ESTADO", nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoContenedor estado;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CLIENTE_ID", nullable = false)
    private Cliente cliente;
}

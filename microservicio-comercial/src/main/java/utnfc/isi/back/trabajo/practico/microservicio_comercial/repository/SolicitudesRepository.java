package utnfc.isi.back.trabajo.practico.microservicio_comercial.repository;

import org.springframework.data.domain.Pageable;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import utnfc.isi.back.trabajo.practico.microservicio_comercial.model.Contenedor;
import utnfc.isi.back.trabajo.practico.microservicio_comercial.model.Solicitud;
import utnfc.isi.back.trabajo.practico.microservicio_comercial.model.Solicitud.EstadoSolicitud;

@Repository
public interface SolicitudesRepository extends JpaRepository<Solicitud, Long> {
    @Query("SELECT s FROM Solicitud s WHERE s.estado <> :entregada AND s.estado <> :cancelada")
    List<Solicitud> findNoFinalizados(
        @Param("entregada") Solicitud.EstadoSolicitud entregada,
        @Param("cancelada") Solicitud.EstadoSolicitud cancelada
    );

    public List<Solicitud> findByContenedor_Estado(Contenedor.EstadoContenedor estado);

    public Page<Solicitud> findByCliente_IdCliente(Long idCliente, Pageable pageable);

    public List<Solicitud> findByCliente_IdCliente(Long idCliente);

    public Page<Solicitud> findByCliente_IdClienteAndEstado(Long idCliente, EstadoSolicitud estado, Pageable pageable);
}

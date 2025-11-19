package utnfc.isi.back.trabajo.practico.microservicio_logistico.repository;

import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import utnfc.isi.back.trabajo.practico.microservicio_logistico.model.Tramo;

@Repository
public interface TramoRepository extends JpaRepository<Tramo, Long> {
    List<Tramo> findByRutaIdRuta(Long rutaId);

    List<Tramo> findByRutaIdRutaAndEstado(Long rutaId, Tramo.EstadoTramo estado);
    
    Page<Tramo> findByCamionDominio(String dominio, Pageable pageable);

    Page<Tramo> findByCamionDominioAndEstado(String dominio, Tramo.EstadoTramo estado, Pageable pageable);

    Tramo findByIdTramoAndRutaIdRuta(Long idTramo, Long idRuta);

    @Query("SELECT COUNT(t) FROM Tramo t WHERE t.ruta.id = :rutaId")
    long contarTramosPorRuta(@Param("rutaId") Long rutaId);
}

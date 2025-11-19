package utnfc.isi.back.trabajo.practico.microservicio_logistico.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.model.Camion;

import java.util.List;

@Repository
public interface CamionRepository extends JpaRepository<Camion, String> {
    Page<Camion> findByTransportista_IdTransportista(Long idTransportista, Pageable pageable);

    List<Camion> findByTransportista_IdTransportista(Long idTransportista);

    @Query("SELECT AVG(c.costoBaseXKm) FROM Camion c WHERE c.capacidadVolumen > :volumen")
    Double avgCostoBaseXKm(@Param("volumen") Double volumen);

    @Query("SELECT AVG(c.consumoXKm) FROM Camion c WHERE c.capacidadVolumen > :volumen")
    Double avgConsumoXKm(@Param("volumen") Double volumen);

    Page<Camion> findByCapacidadPesoGreaterThanEqualAndDisponibilidadTrue(Integer capacidad, Pageable pageable);
}

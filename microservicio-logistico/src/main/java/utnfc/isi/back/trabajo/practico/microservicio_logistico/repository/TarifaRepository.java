package utnfc.isi.back.trabajo.practico.microservicio_logistico.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import utnfc.isi.back.trabajo.practico.microservicio_logistico.model.Tarifa;

@Repository
public interface TarifaRepository extends JpaRepository<Tarifa, Long> {
    Tarifa findTop1ByFechaHastaIsNull();

    @Query("SELECT t FROM Tarifa t WHERE (:capacidadCarga BETWEEN t.volumenMin AND t.volumenMax) AND t.fechaHasta IS NULL")
    List<Tarifa> findTarifaVigentePorCapacidad(@Param("capacidadCarga") Double capacidadCarga);

    List<Tarifa> findByFechaHastaIsNull();
}
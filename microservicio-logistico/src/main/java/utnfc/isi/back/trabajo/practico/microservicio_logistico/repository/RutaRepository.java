package utnfc.isi.back.trabajo.practico.microservicio_logistico.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import utnfc.isi.back.trabajo.practico.microservicio_logistico.model.Ruta;

@Repository
public interface RutaRepository extends JpaRepository<Ruta, Long> {
    public Ruta findByIdSolicitud(Long idSolicitud);

    @Query("SELECT r FROM Ruta r WHERE r.cantidadTramos = 0 OR r.cantidadDepositos = 0")
    public List<Ruta> findRutasPendientes();
}

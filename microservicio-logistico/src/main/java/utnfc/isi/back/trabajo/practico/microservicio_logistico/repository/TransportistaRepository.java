package utnfc.isi.back.trabajo.practico.microservicio_logistico.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.model.Transportista;

@Repository
public interface TransportistaRepository extends JpaRepository<Transportista, Long> {
}

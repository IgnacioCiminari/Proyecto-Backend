package utnfc.isi.back.trabajo.practico.microservicio_logistico.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import utnfc.isi.back.trabajo.practico.microservicio_logistico.model.Enlace;
import utnfc.isi.back.trabajo.practico.microservicio_logistico.model.Nodo;

@Repository
public interface EnlacesRepository extends JpaRepository<Enlace, Long> {
    Enlace findByOrigenAndDestino(Nodo origen, Nodo destino);
}

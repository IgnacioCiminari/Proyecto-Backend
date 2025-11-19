package utnfc.isi.back.trabajo.practico.microservicio_comercial.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import utnfc.isi.back.trabajo.practico.microservicio_comercial.model.Cliente;

import java.util.List;

@Repository
public interface ClientesRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findByActivo(Integer activo);
}

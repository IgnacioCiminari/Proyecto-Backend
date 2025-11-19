package utnfc.isi.back.trabajo.practico.microservicio_comercial.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import utnfc.isi.back.trabajo.practico.microservicio_comercial.model.Contenedor;
import utnfc.isi.back.trabajo.practico.microservicio_comercial.model.Cliente;

import java.util.List;

@Repository
public interface ContenedoresRepository extends JpaRepository<Contenedor, Long> {
    List<Contenedor> findByCliente(Cliente cliente);

    Page<Contenedor> findByCliente_IdCliente(Long idCliente, Pageable pageable);
    
    List<Contenedor> findByCliente_IdCliente(Long idCliente);
}

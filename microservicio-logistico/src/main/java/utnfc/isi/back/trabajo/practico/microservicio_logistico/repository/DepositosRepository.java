package utnfc.isi.back.trabajo.practico.microservicio_logistico.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import utnfc.isi.back.trabajo.practico.microservicio_logistico.model.Deposito;

@Repository
public interface DepositosRepository extends JpaRepository<Deposito, Long> {}

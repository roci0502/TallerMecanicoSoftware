package com.taller.client;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByNombreCompletoIgnoreCase(String nombreCompleto);
    Optional<Client> findByEmailIgnoreCase(String email);
    Optional<Client> findByTelefonoPersonal(String telefonoPersonal);
    Optional<Client> findByEmailTrabajoIgnoreCase(String emailTrabajo);
    Optional<Client> findByTelefonoTrabajo(String telefonoTrabajo);
    Optional<Client> findByNombreCompletoIgnoreCaseAndIdNot(String nombreCompleto, Long id);
    Optional<Client> findByEmailIgnoreCaseAndIdNot(String email, Long id);
    Optional<Client> findByTelefonoPersonalAndIdNot(String telefonoPersonal, Long id);
    Optional<Client> findByEmailTrabajoIgnoreCaseAndIdNot(String emailTrabajo, Long id);
    Optional<Client> findByTelefonoTrabajoAndIdNot(String telefonoTrabajo, Long id);
}

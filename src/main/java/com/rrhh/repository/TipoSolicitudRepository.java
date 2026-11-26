package com.rrhh.repository;

import com.permisos.model.TipoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA genera la implementación: no hay clase ni consultas a mano.
 */
public interface TipoSolicitudRepository extends JpaRepository<TipoSolicitud, Integer> {

    List<TipoSolicitud> findByActivoTrueOrderByNombre();

    Optional<TipoSolicitud> findByNombre(String nombre);
}

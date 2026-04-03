package com.mentorcore.repository;

import com.mentorcore.model.ParametroSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad ParametroSistema.
 * RF12
 */
@Repository
public interface ParametroSistemaRepository extends JpaRepository<ParametroSistema, Long> {

    Optional<ParametroSistema> findByClave(String clave);

    boolean existsByClave(String clave);
}

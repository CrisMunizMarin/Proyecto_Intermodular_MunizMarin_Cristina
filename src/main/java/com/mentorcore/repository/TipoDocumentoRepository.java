package com.mentorcore.repository;

import com.mentorcore.model.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de acceso a datos para la entidad TipoDocumento.
 * RF12
 */
@Repository
public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, Long> {

    List<TipoDocumento> findByActivoTrue();

    List<TipoDocumento> findByEsObligatorioAndActivoTrue(boolean esObligatorio);
}

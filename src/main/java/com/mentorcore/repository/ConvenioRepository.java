package com.mentorcore.repository;

import com.mentorcore.model.Alumno;
import com.mentorcore.model.Convenio;
import com.mentorcore.model.enums.EstadoConvenioEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad Convenio.
 * RF3
 */
@Repository
public interface ConvenioRepository extends JpaRepository<Convenio, Long> {

    // Convenios de un alumno (RF3)
    List<Convenio> findByAlumno(Alumno alumno);

    // Convenio activo de un alumno (RF3)
    Optional<Convenio> findByAlumnoAndEstado(Alumno alumno, EstadoConvenioEnum estado);

    // Comprobar número duplicado (RF3)
    boolean existsByNumeroConvenio(String numeroConvenio);
}

package com.mentorcore.repository;

import com.mentorcore.model.Alumno;
import com.mentorcore.model.Convenio;
import com.mentorcore.model.enums.EstadoConvenioEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad Convenio.
 * RF3
 */
@Repository
public interface ConvenioRepository extends JpaRepository<Convenio, Long> {

    @Query("SELECT c FROM Convenio c " +
           "JOIN FETCH c.alumno a " +
           "LEFT JOIN FETCH a.tutorCentro " +
           "LEFT JOIN FETCH a.cursoAcademico " +
           "JOIN FETCH c.empresa " +
           "JOIN FETCH c.tutorCentro " +
           "WHERE c.id = :id")
    Optional<Convenio> findDetalleById(@Param("id") Long id);

    // Convenios de un alumno (RF3)
    @Query("SELECT c FROM Convenio c " +
           "JOIN FETCH c.alumno a " +
           "LEFT JOIN FETCH a.tutorCentro " +
           "LEFT JOIN FETCH a.cursoAcademico " +
           "JOIN FETCH c.empresa " +
           "JOIN FETCH c.tutorCentro " +
           "WHERE c.alumno = :alumno " +
           "ORDER BY c.fechaInicio DESC")
    List<Convenio> findByAlumno(@Param("alumno") Alumno alumno);

    // Convenio activo de un alumno (RF3)
    Optional<Convenio> findByAlumnoAndEstado(Alumno alumno, EstadoConvenioEnum estado);

    // Comprobar número duplicado (RF3)
    boolean existsByNumeroConvenio(String numeroConvenio);
}

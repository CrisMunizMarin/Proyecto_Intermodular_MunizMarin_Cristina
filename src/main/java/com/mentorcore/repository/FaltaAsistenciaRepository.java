package com.mentorcore.repository;

import com.mentorcore.model.Alumno;
import com.mentorcore.model.FaltaAsistencia;
import com.mentorcore.model.enums.EstadoFaltaEnum;
import com.mentorcore.model.enums.TipoFaltaEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad FaltaAsistencia.
 * RF19, RF22
 */
@Repository
public interface FaltaAsistenciaRepository extends JpaRepository<FaltaAsistencia, Long> {

    // Todas las faltas de un alumno (RF22)
    @Query("SELECT f FROM FaltaAsistencia f " +
           "LEFT JOIN FETCH f.justificante " +
           "WHERE f.alumno = :alumno " +
           "ORDER BY f.fechaFalta DESC")
    List<FaltaAsistencia> findByAlumnoOrderByFechaFaltaDesc(@Param("alumno") Alumno alumno);

    // Faltas por tipo (RF19)
    List<FaltaAsistencia> findByAlumnoAndTipo(Alumno alumno, TipoFaltaEnum tipo);

    // Faltas por estado (RF22)
    @Query("SELECT f FROM FaltaAsistencia f " +
           "LEFT JOIN FETCH f.justificante " +
           "WHERE f.alumno = :alumno AND f.estado = :estado " +
           "ORDER BY f.fechaFalta DESC")
    List<FaltaAsistencia> findByAlumnoAndEstado(@Param("alumno") Alumno alumno,
                                                @Param("estado") EstadoFaltaEnum estado);

    // Verificar falta duplicada en una fecha (RF19 - máximo 1 por día)
    boolean existsByAlumnoAndFechaFalta(Alumno alumno, LocalDate fecha);

    // Contar faltas injustificadas de un alumno (RF4 - dashboard)
    long countByAlumnoAndTipo(Alumno alumno, TipoFaltaEnum tipo);

    // Faltas de todos los alumnos de un tutor centro (RF4)
    @Query("SELECT f FROM FaltaAsistencia f " +
           "JOIN FETCH f.alumno a " +
           "LEFT JOIN FETCH a.tutorCentro " +
           "LEFT JOIN FETCH f.justificante " +
           "WHERE a.tutorCentro.id = :idTutor " +
           "ORDER BY f.fechaFalta DESC")
    List<FaltaAsistencia> findByTutorCentro(@Param("idTutor") Long idTutor);

    @Query("SELECT f FROM FaltaAsistencia f " +
           "JOIN FETCH f.alumno a " +
           "LEFT JOIN FETCH a.tutorCentro " +
           "LEFT JOIN FETCH f.justificante " +
           "WHERE f.id = :id")
    Optional<FaltaAsistencia> findDetalleById(@Param("id") Long id);
}

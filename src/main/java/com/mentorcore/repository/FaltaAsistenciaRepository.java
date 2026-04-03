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

/**
 * Repositorio de acceso a datos para la entidad FaltaAsistencia.
 * RF19, RF22
 */
@Repository
public interface FaltaAsistenciaRepository extends JpaRepository<FaltaAsistencia, Long> {

    // Todas las faltas de un alumno (RF22)
    List<FaltaAsistencia> findByAlumnoOrderByFechaFaltaDesc(Alumno alumno);

    // Faltas por tipo (RF19)
    List<FaltaAsistencia> findByAlumnoAndTipo(Alumno alumno, TipoFaltaEnum tipo);

    // Faltas por estado (RF22)
    List<FaltaAsistencia> findByAlumnoAndEstado(Alumno alumno, EstadoFaltaEnum estado);

    // Verificar falta duplicada en una fecha (RF19 - máximo 1 por día)
    boolean existsByAlumnoAndFechaFalta(Alumno alumno, LocalDate fecha);

    // Contar faltas injustificadas de un alumno (RF4 - dashboard)
    long countByAlumnoAndTipo(Alumno alumno, TipoFaltaEnum tipo);

    // Faltas de todos los alumnos de un tutor centro (RF4)
    @Query("SELECT f FROM FaltaAsistencia f WHERE " +
           "f.alumno.tutorCentro.id = :idTutor " +
           "ORDER BY f.fechaFalta DESC")
    List<FaltaAsistencia> findByTutorCentro(@Param("idTutor") Long idTutor);
}

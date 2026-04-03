package com.mentorcore.repository;

import com.mentorcore.model.Alumno;
import com.mentorcore.model.Tarea;
import com.mentorcore.model.enums.EstadoValidacionEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio de acceso a datos para la entidad Tarea.
 * RF2, RF5, RF9
 */
@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {

    // Tareas de un alumno ordenadas por fecha descendente (RF2)
    List<Tarea> findByAlumnoOrderByFechaRegistroDesc(Alumno alumno);

    // Tareas pendientes de revisión de un alumno (RF5)
    List<Tarea> findByAlumnoAndEstadoValidacion(
            Alumno alumno, EstadoValidacionEnum estado);

    // Todas las tareas pendientes de los alumnos de un tutor (RF5)
    @Query("SELECT t FROM Tarea t WHERE t.alumno.tutorCentro.id = :idTutor " +
           "AND t.estadoValidacion = 'PENDIENTE' " +
           "ORDER BY t.fechaCreacion ASC")
    List<Tarea> findPendientesByTutorCentro(@Param("idTutor") Long idTutor);

    // Tareas de un alumno en un rango de fechas (RF8 - informes)
    @Query("SELECT t FROM Tarea t WHERE t.alumno = :alumno " +
           "AND t.fechaRegistro BETWEEN :inicio AND :fin " +
           "ORDER BY t.fechaRegistro ASC")
    List<Tarea> findByAlumnoAndFechaBetween(
            @Param("alumno") Alumno alumno,
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin);

    // Contar tareas por estado de un alumno (RF4 - dashboard)
    long countByAlumnoAndEstadoValidacion(Alumno alumno, EstadoValidacionEnum estado);
}

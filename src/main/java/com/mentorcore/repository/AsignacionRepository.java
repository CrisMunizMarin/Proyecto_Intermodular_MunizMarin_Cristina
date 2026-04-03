package com.mentorcore.repository;

import com.mentorcore.model.Alumno;
import com.mentorcore.model.Asignacion;
import com.mentorcore.model.TutorEmpresa;
import com.mentorcore.model.enums.EstadoFeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad Asignacion.
 * RF13, RF21
 */
@Repository
public interface AsignacionRepository extends JpaRepository<Asignacion, Long> {

    // Asignación activa de un alumno — debe existir solo una (RF21)
    Optional<Asignacion> findByAlumnoAndEstado(Alumno alumno, EstadoFeEnum estado);

    // Historial completo de asignaciones de un alumno (RF21)
    List<Asignacion> findByAlumnoOrderByFechaCreacionDesc(Alumno alumno);

    // Alumnos activos asignados a un tutor empresa (RF9)
    List<Asignacion> findByTutorEmpresaAndEstado(TutorEmpresa tutor, EstadoFeEnum estado);

    // Verificar si un alumno tiene asignación activa (RF13)
    boolean existsByAlumnoAndEstado(Alumno alumno, EstadoFeEnum estado);

    // Asignaciones activas en un periodo (RF20)
    @Query("SELECT a FROM Asignacion a WHERE a.periodo.id = :idPeriodo " +
           "AND a.estado = 'EN_CURSO'")
    List<Asignacion> findActivasByPeriodo(@Param("idPeriodo") Long idPeriodo);
}

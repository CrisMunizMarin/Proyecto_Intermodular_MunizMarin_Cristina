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

    @Query("SELECT a FROM Asignacion a " +
           "JOIN FETCH a.alumno al " +
           "LEFT JOIN FETCH al.tutorCentro " +
           "LEFT JOIN FETCH al.cursoAcademico " +
           "JOIN FETCH a.empresa " +
           "JOIN FETCH a.tutorEmpresa te " +
           "LEFT JOIN FETCH te.empresa " +
           "LEFT JOIN FETCH a.periodo p " +
           "LEFT JOIN FETCH p.cursoAcademico " +
           "LEFT JOIN FETCH a.reasignadoPor " +
           "WHERE a.id = :id")
    Optional<Asignacion> findDetalleById(@Param("id") Long id);

    @Query("SELECT a FROM Asignacion a " +
           "JOIN FETCH a.alumno al " +
           "LEFT JOIN FETCH al.tutorCentro " +
           "LEFT JOIN FETCH al.cursoAcademico " +
           "JOIN FETCH a.empresa " +
           "JOIN FETCH a.tutorEmpresa te " +
           "LEFT JOIN FETCH te.empresa " +
           "LEFT JOIN FETCH a.periodo p " +
           "LEFT JOIN FETCH p.cursoAcademico " +
           "LEFT JOIN FETCH a.reasignadoPor " +
           "ORDER BY a.fechaCreacion DESC, a.id DESC")
    List<Asignacion> findAllDetalleOrderByFechaCreacionDesc();

    // Asignación activa de un alumno — debe existir solo una (RF21)
    @Query("SELECT a FROM Asignacion a " +
           "JOIN FETCH a.alumno al " +
           "LEFT JOIN FETCH al.tutorCentro " +
           "LEFT JOIN FETCH al.cursoAcademico " +
           "JOIN FETCH a.empresa " +
           "JOIN FETCH a.tutorEmpresa " +
           "LEFT JOIN FETCH a.periodo p " +
           "LEFT JOIN FETCH p.cursoAcademico " +
           "WHERE a.alumno = :alumno AND a.estado = :estado")
    Optional<Asignacion> findByAlumnoAndEstado(@Param("alumno") Alumno alumno,
                                               @Param("estado") EstadoFeEnum estado);

    // Historial completo de asignaciones de un alumno (RF21)
    @Query("SELECT a FROM Asignacion a " +
           "JOIN FETCH a.alumno al " +
           "LEFT JOIN FETCH al.tutorCentro " +
           "LEFT JOIN FETCH al.cursoAcademico " +
           "JOIN FETCH a.empresa " +
           "LEFT JOIN FETCH a.tutorEmpresa te " +
           "LEFT JOIN FETCH te.empresa " +
           "LEFT JOIN FETCH a.periodo p " +
           "LEFT JOIN FETCH p.cursoAcademico " +
           "LEFT JOIN FETCH a.reasignadoPor " +
           "WHERE a.alumno = :alumno " +
           "ORDER BY a.fechaCreacion DESC, a.id DESC")
    List<Asignacion> findByAlumnoOrderByFechaCreacionDesc(@Param("alumno") Alumno alumno);

    // Alumnos activos asignados a un tutor empresa (RF9)
    @Query("SELECT a FROM Asignacion a " +
           "JOIN FETCH a.alumno al " +
           "LEFT JOIN FETCH al.tutorCentro " +
           "LEFT JOIN FETCH al.cursoAcademico " +
           "JOIN FETCH a.empresa " +
           "JOIN FETCH a.tutorEmpresa te " +
           "LEFT JOIN FETCH a.periodo p " +
           "LEFT JOIN FETCH p.cursoAcademico " +
           "WHERE te = :tutor AND a.estado = :estado " +
           "ORDER BY al.apellidos ASC, al.nombre ASC")
    List<Asignacion> findByTutorEmpresaAndEstado(@Param("tutor") TutorEmpresa tutor,
                                                 @Param("estado") EstadoFeEnum estado);

    @Query("SELECT a FROM Asignacion a " +
           "JOIN FETCH a.alumno al " +
           "LEFT JOIN FETCH al.tutorCentro " +
           "LEFT JOIN FETCH al.cursoAcademico " +
           "JOIN FETCH a.empresa " +
           "JOIN FETCH a.tutorEmpresa te " +
           "LEFT JOIN FETCH a.periodo p " +
           "LEFT JOIN FETCH p.cursoAcademico " +
           "WHERE te.id = :idTutorEmpresa AND a.estado = :estado " +
           "ORDER BY al.apellidos ASC, al.nombre ASC")
    List<Asignacion> findByTutorEmpresaIdAndEstado(@Param("idTutorEmpresa") Long idTutorEmpresa,
                                                   @Param("estado") EstadoFeEnum estado);

    // Verificar si un alumno tiene asignación activa (RF13)
    boolean existsByAlumnoAndEstado(Alumno alumno, EstadoFeEnum estado);

    // Asignaciones activas en un periodo (RF20)
    @Query("SELECT a FROM Asignacion a WHERE a.periodo.id = :idPeriodo " +
           "AND a.estado = 'EN_CURSO'")
    List<Asignacion> findActivasByPeriodo(@Param("idPeriodo") Long idPeriodo);
}

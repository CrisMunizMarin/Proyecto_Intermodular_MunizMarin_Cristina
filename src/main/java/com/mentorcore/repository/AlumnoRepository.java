package com.mentorcore.repository;

import com.mentorcore.model.Alumno;
import com.mentorcore.model.TutorCentro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad Alumno.
 * RF2, RF3, RF4, RF13
 */
@Repository
public interface AlumnoRepository extends JpaRepository<Alumno, Long> {

    // Buscar por tutor centro asignado (RF4 - Dashboard del tutor)
    List<Alumno> findByTutorCentro(TutorCentro tutorCentro);

    // Buscar por curso académico (RF13)
    List<Alumno> findByCursoAcademico_Id(Long idCurso);

    // Buscar por grupo dentro de un curso (RF4)
    List<Alumno> findByGrupo(String grupo);

    // Buscar alumno por su usuario base (RF1)
    Optional<Alumno> findByIdUsuario(Long idUsuario);

    // Buscar por DNI (RF11 - gestión de usuarios)
    Optional<Alumno> findByDni(String dni);

    // Alumnos de un tutor centro en un curso concreto (RF4)
    @Query("SELECT a FROM Alumno a WHERE a.tutorCentro.id = :idTutor " +
           "AND a.cursoAcademico.anioAcademico = :anio")
    List<Alumno> findByTutorCentroAndAnioAcademico(
            @Param("idTutor") Long idTutor,
            @Param("anio") String anioAcademico);

    // Búsqueda por nombre o apellidos (RF15 - búsqueda avanzada)
    @Query("SELECT a FROM Alumno a WHERE " +
           "LOWER(a.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(a.apellidos) LIKE LOWER(CONCAT('%', :texto, '%'))")
    List<Alumno> buscarPorNombre(@Param("texto") String texto);
}

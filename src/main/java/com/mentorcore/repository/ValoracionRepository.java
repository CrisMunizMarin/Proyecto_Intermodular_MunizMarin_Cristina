package com.mentorcore.repository;

import com.mentorcore.model.Alumno;
import com.mentorcore.model.Valoracion;
import com.mentorcore.model.enums.TipoEvaluadorEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad Valoracion.
 * RF7
 */
@Repository
public interface ValoracionRepository extends JpaRepository<Valoracion, Long> {

    // Valoración de un alumno por tipo de evaluador (RF7)
    // La constraint UNIQUE garantiza máximo 1 por combinación
    Optional<Valoracion> findByAlumnoAndTipoEvaluador(
            Alumno alumno, TipoEvaluadorEnum tipoEvaluador);

    // Todas las valoraciones de un alumno (RF7)
    List<Valoracion> findByAlumno(Alumno alumno);

    // Verificar si ya existe valoración (RF7)
    boolean existsByAlumnoAndTipoEvaluador(
            Alumno alumno, TipoEvaluadorEnum tipoEvaluador);
}

package com.mentorcore.repository;

import com.mentorcore.model.TutorCentro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad TutorCentro.
 * RF4, RF5, RF8, RF13
 */
@Repository
public interface TutorCentroRepository extends JpaRepository<TutorCentro, Long> {

    // Buscar por usuario base (RF1)
    Optional<TutorCentro> findByIdUsuario(Long idUsuario);

    // Buscar por departamento (RF13)
    List<TutorCentro> findByDepartamento(String departamento);

    // Tutores de centro activos (RF13)
    @Query("SELECT t FROM TutorCentro t WHERE t.estado = 'ACTIVO'")
    List<TutorCentro> findAllActivos();

    // Buscar por nombre o apellidos (RF15)
    @Query("SELECT t FROM TutorCentro t WHERE " +
           "LOWER(t.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(t.apellidos) LIKE LOWER(CONCAT('%', :texto, '%'))")
    List<TutorCentro> buscarPorNombre(@Param("texto") String texto);
}

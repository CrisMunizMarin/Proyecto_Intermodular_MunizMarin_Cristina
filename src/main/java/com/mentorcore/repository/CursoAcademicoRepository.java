package com.mentorcore.repository;

import com.mentorcore.model.CursoAcademico;
import com.mentorcore.model.enums.NivelEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad CursoAcademico.
 * RF12, RF20
 */
@Repository
public interface CursoAcademicoRepository extends JpaRepository<CursoAcademico, Long> {

    Optional<CursoAcademico> findByCodigoCurso(String codigoCurso);

    List<CursoAcademico> findByAnioAcademico(String anioAcademico);

    List<CursoAcademico> findByActivoTrue();

    List<CursoAcademico> findByNivelAndAnioAcademico(NivelEnum nivel, String anioAcademico);

    boolean existsByCodigoCurso(String codigoCurso);
}

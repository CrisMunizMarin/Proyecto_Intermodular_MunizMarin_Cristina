package com.mentorcore.repository;

import com.mentorcore.model.CursoAcademico;
import com.mentorcore.model.PeriodoFormacion;
import com.mentorcore.model.enums.EstadoPeriodoEnum;
import com.mentorcore.model.enums.TipoPeriodoEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad PeriodoFormacion.
 * RF20
 */
@Repository
public interface PeriodoFormacionRepository extends JpaRepository<PeriodoFormacion, Long> {

    List<PeriodoFormacion> findByCursoAcademico(CursoAcademico curso);

    List<PeriodoFormacion> findByEstado(EstadoPeriodoEnum estado);

    Optional<PeriodoFormacion> findByCursoAcademicoAndTipoAndAnioAcademico(
            CursoAcademico curso, TipoPeriodoEnum tipo, String anioAcademico);

    // Detectar solapamiento de fechas para un mismo curso (RF20)
    @Query("SELECT p FROM PeriodoFormacion p WHERE p.cursoAcademico = :curso " +
           "AND p.id <> :idExcluir " +
           "AND p.fechaInicio <= :fin AND p.fechaFin >= :inicio")
    List<PeriodoFormacion> findSolapados(
            @Param("curso") CursoAcademico curso,
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin,
            @Param("idExcluir") Long idExcluir);
}

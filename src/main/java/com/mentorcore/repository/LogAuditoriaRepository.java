package com.mentorcore.repository;

import com.mentorcore.model.LogAuditoria;
import com.mentorcore.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio de acceso a datos para la entidad LogAuditoria.
 * Solo lectura en producción: los logs son inmutables.
 * Objetivo 7, RGPD
 */
@Repository
public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, Long> {

    List<LogAuditoria> findByUsuarioOrderByFechaHoraDesc(Usuario usuario);

    List<LogAuditoria> findByEntidadAfectadaAndIdEntidad(
            String entidadAfectada, Long idEntidad);

    List<LogAuditoria> findByFechaHoraBetweenOrderByFechaHoraDesc(
            LocalDateTime inicio, LocalDateTime fin);
}

package com.mentorcore.repository;

import com.mentorcore.model.Alumno;
import com.mentorcore.model.Documento;
import com.mentorcore.model.enums.ContextoDocumentoEnum;
import com.mentorcore.model.enums.EstadoDocumentoEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad Documento.
 * RF3, RF6, RF22
 */
@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    // Todos los documentos del expediente de un alumno (RF3)
    @Query("SELECT d FROM Documento d " +
           "LEFT JOIN FETCH d.tipoDocumento " +
           "WHERE d.alumno = :alumno " +
           "ORDER BY d.fechaSubida DESC")
    List<Documento> findByAlumnoOrderByFechaSubidaDesc(@Param("alumno") Alumno alumno);

    // Documentos por contexto: expediente o justificante (RF22)
    @Query("SELECT d FROM Documento d " +
           "LEFT JOIN FETCH d.tipoDocumento " +
           "WHERE d.alumno = :alumno AND d.contexto = :contexto " +
           "ORDER BY d.fechaSubida DESC")
    List<Documento> findByAlumnoAndContexto(@Param("alumno") Alumno alumno,
                                            @Param("contexto") ContextoDocumentoEnum contexto);

    // Documentos pendientes de revisión de un alumno (RF6)
    @Query("SELECT d FROM Documento d " +
           "JOIN FETCH d.alumno a " +
           "LEFT JOIN FETCH a.tutorCentro " +
           "LEFT JOIN FETCH d.tipoDocumento " +
           "WHERE d.alumno = :alumno AND d.estado = :estado " +
           "ORDER BY d.fechaSubida DESC")
    List<Documento> findByAlumnoAndEstado(@Param("alumno") Alumno alumno,
                                          @Param("estado") EstadoDocumentoEnum estado);

    @Query("SELECT d FROM Documento d " +
           "JOIN FETCH d.alumno a " +
           "LEFT JOIN FETCH a.tutorCentro " +
           "LEFT JOIN FETCH d.tipoDocumento " +
           "WHERE d.id = :id")
    Optional<Documento> findDetalleById(@Param("id") Long id);

    // Contar documentos obligatorios pendientes (RF4 - dashboard)
    long countByAlumnoAndEsObligatorioAndEstado(
            Alumno alumno, boolean esObligatorio, EstadoDocumentoEnum estado);
}

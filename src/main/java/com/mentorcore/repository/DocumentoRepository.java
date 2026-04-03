package com.mentorcore.repository;

import com.mentorcore.model.Alumno;
import com.mentorcore.model.Documento;
import com.mentorcore.model.enums.ContextoDocumentoEnum;
import com.mentorcore.model.enums.EstadoDocumentoEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de acceso a datos para la entidad Documento.
 * RF3, RF6, RF22
 */
@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    // Todos los documentos del expediente de un alumno (RF3)
    List<Documento> findByAlumnoOrderByFechaSubidaDesc(Alumno alumno);

    // Documentos por contexto: expediente o justificante (RF22)
    List<Documento> findByAlumnoAndContexto(Alumno alumno, ContextoDocumentoEnum contexto);

    // Documentos pendientes de revisión de un alumno (RF6)
    List<Documento> findByAlumnoAndEstado(Alumno alumno, EstadoDocumentoEnum estado);

    // Contar documentos obligatorios pendientes (RF4 - dashboard)
    long countByAlumnoAndEsObligatorioAndEstado(
            Alumno alumno, boolean esObligatorio, EstadoDocumentoEnum estado);
}

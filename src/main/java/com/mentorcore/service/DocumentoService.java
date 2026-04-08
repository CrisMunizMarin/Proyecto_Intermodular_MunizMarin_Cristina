package com.mentorcore.service;

import com.mentorcore.model.Alumno;
import com.mentorcore.model.Documento;
import com.mentorcore.model.TipoDocumento;
import com.mentorcore.model.Usuario;
import com.mentorcore.model.enums.ContextoDocumentoEnum;
import com.mentorcore.model.enums.EstadoDocumentoEnum;
import com.mentorcore.repository.DocumentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión del expediente digital del alumno.
 * Gestiona subida, revisión y descarga de documentos.
 * RF3, RF6, RF22
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentoService {

    private final DocumentoRepository documentoRepository;


    // BÚSQUEDAS

    @Transactional(readOnly = true)
    public Optional<Documento> findById(Long id) {
        return documentoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Documento> findByAlumno(Alumno alumno) {
        return documentoRepository.findByAlumnoOrderByFechaSubidaDesc(alumno);
    }

    @Transactional(readOnly = true)
    public List<Documento> findByAlumnoAndContexto(Alumno alumno,
                                                   ContextoDocumentoEnum contexto) {
        return documentoRepository.findByAlumnoAndContexto(alumno, contexto);
    }

    @Transactional(readOnly = true)
    public List<Documento> findPendientesByAlumno(Alumno alumno) {
        return documentoRepository.findByAlumnoAndEstado(
                alumno, EstadoDocumentoEnum.PENDIENTE);
    }

    @Transactional(readOnly = true)
    public long contarObligatoriosPendientes(Alumno alumno) {
        return documentoRepository.countByAlumnoAndEsObligatorioAndEstado(
                alumno, true, EstadoDocumentoEnum.PENDIENTE);
    }


    // SUBIDA DE DOCUMENTOS

    /**
     * Registra un nuevo documento en el expediente del alumno. RF3
     */
    @Transactional
    public Documento subirDocumento(Alumno alumno, TipoDocumento tipoDocumento,
                                    Usuario subidoPor, String nombreArchivo,
                                    String rutaAlmacenamiento, String mimeType,
                                    Long tamanoBytes, boolean esObligatorio,
                                    ContextoDocumentoEnum contexto) {

        Documento documento = new Documento();
        documento.setAlumno(alumno);
        documento.setTipoDocumento(tipoDocumento);
        documento.setSubidoPor(subidoPor);
        documento.setNombreArchivo(nombreArchivo);
        documento.setRutaAlmacenamiento(rutaAlmacenamiento);
        documento.setMimeType(mimeType);
        documento.setTamanoBytes(tamanoBytes);
        documento.setEsObligatorio(esObligatorio);
        documento.setContexto(contexto);
        documento.setEstado(EstadoDocumentoEnum.PENDIENTE);

        Documento guardado = documentoRepository.save(documento);
        log.info("Documento '{}' subido por '{}' para alumno id={} [{}]",
                nombreArchivo, subidoPor.getNombreUsuario(),
                alumno.getId(), contexto);
        return guardado;
    }


    // REVISIÓN DE DOCUMENTOS

    @Transactional
    public void validar(Long idDocumento, String comentario) {
        Documento documento = getOrThrow(idDocumento);

        if (documento.getEstado() != EstadoDocumentoEnum.PENDIENTE) {
            throw new IllegalStateException(
                    "Solo se pueden validar documentos en estado PENDIENTE. " +
                            "Estado actual: " + documento.getEstado());
        }

        documento.validar(comentario);
        documentoRepository.save(documento);

        log.info("Documento id={} ('{}') VALIDADO", idDocumento,
                documento.getNombreArchivo());
    }

    @Transactional
    public void rechazar(Long idDocumento, String motivo) {
        Documento documento = getOrThrow(idDocumento);

        if (documento.getEstado() != EstadoDocumentoEnum.PENDIENTE) {
            throw new IllegalStateException(
                    "Solo se pueden rechazar documentos en estado PENDIENTE. " +
                            "Estado actual: " + documento.getEstado());
        }

        documento.rechazar(motivo);
        documentoRepository.save(documento);

        log.info("Documento id={} ('{}') RECHAZADO. Motivo: {}",
                idDocumento, documento.getNombreArchivo(), motivo);
    }


    // JUSTIFICANTES DE FALTA

    @Transactional(readOnly = true)
    public List<Documento> findJustificantesByAlumno(Alumno alumno) {
        return documentoRepository.findByAlumnoAndContexto(
                alumno, ContextoDocumentoEnum.JUSTIFICANTE_FALTA);
    }


    // ELIMINACIÓN

    @Transactional
    public void eliminar(Long idDocumento) {
        Documento documento = getOrThrow(idDocumento);

        if (documento.getEstado() == EstadoDocumentoEnum.VALIDADO) {
            throw new IllegalStateException(
                    "No se puede eliminar un documento ya VALIDADO");
        }

        documentoRepository.deleteById(idDocumento);
        log.info("Documento id={} ('{}') eliminado",
                idDocumento, documento.getNombreArchivo());
    }


    // HELPERS PRIVADOS

    private Documento getOrThrow(Long id) {
        return documentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Documento no encontrado con id: " + id));
    }
}


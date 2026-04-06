package com.mentorcore.service;

import com.mentorcore.model.Alumno;
import com.mentorcore.model.Documento;
import com.mentorcore.model.Usuario;
import com.mentorcore.model.enums.ContextoDocumentoEnum;
import com.mentorcore.model.enums.EstadoDocumentoEnum;
import com.mentorcore.repository.DocumentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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


    //BÚSQUEDAS

    @Transactional(readOnly = true)
    public Optional<Documento> findById(Long id) {
        return documentoRepository.findById(id);
    }

    /**
     * Devuelve todos los documentos del expediente de un alumno
     * ordenados por fecha de subida descendente. RF3
     */
    @Transactional(readOnly = true)
    public List<Documento> findByAlumno(Alumno alumno) {
        return documentoRepository.findByAlumnoOrderByFechaSubidaDesc(alumno);
    }

    /**
     * Devuelve los documentos de un alumno filtrados por contexto.
     * EXPEDIENTE → documentos del ciclo formativo.
     * JUSTIFICANTE_FALTA → justificantes de asistencia (RF22).
     */
    @Transactional(readOnly = true)
    public List<Documento> findByAlumnoAndContexto(Alumno alumno,
                                                    ContextoDocumentoEnum contexto) {
        return documentoRepository.findByAlumnoAndContexto(alumno, contexto);
    }

    /**
     * Devuelve los documentos pendientes de revisión de un alumno. RF6
     */
    @Transactional(readOnly = true)
    public List<Documento> findPendientesByAlumno(Alumno alumno) {
        return documentoRepository.findByAlumnoAndEstado(
                alumno, EstadoDocumentoEnum.PENDIENTE);
    }

    /**
     * Cuenta los documentos obligatorios que aún no han sido validados. RF4
     * Usado en el dashboard del tutor centro.
     */
    @Transactional(readOnly = true)
    public long contarObligatoriosPendientes(Alumno alumno) {
        return documentoRepository.countByAlumnoAndEsObligatorioAndEstado(
                alumno, true, EstadoDocumentoEnum.PENDIENTE);
    }


    //SUBIDA DE DOCUMENTOS

    /**
     * Registra un nuevo documento en el expediente del alumno. RF3
     * El archivo ya ha sido guardado en disco por FileUploadUtil.
     *
     * @param alumno           alumno propietario del documento
     * @param subidoPor        usuario que realiza la subida
     * @param nombreArchivo    nombre original del archivo
     * @param rutaAlmacenamiento ruta en el servidor donde se guardó
     * @param mimeType         tipo MIME del archivo
     * @param tamanoByte       tamaño en bytes
     * @param esObligatorio    si el documento es obligatorio para la FE
     * @param contexto         EXPEDIENTE o JUSTIFICANTE_FALTA
     */
    @Transactional
    public Documento subirDocumento(Alumno alumno, Usuario subidoPor,
                                    String nombreArchivo, String rutaAlmacenamiento,
                                    String mimeType, Long tamanoByte,
                                    boolean esObligatorio,
                                    ContextoDocumentoEnum contexto) {

        Documento documento = new Documento();
        documento.setAlumno(alumno);
        documento.setSubidoPor(subidoPor);
        documento.setNombreArchivo(nombreArchivo);
        documento.setRutaAlmacenamiento(rutaAlmacenamiento);
        documento.setMimeType(mimeType);
        documento.setTamanoBytes(tamanoByte);
        documento.setEsObligatorio(esObligatorio);
        documento.setContexto(contexto);
        documento.setEstado(EstadoDocumentoEnum.PENDIENTE);
        documento.setFechaSubida(LocalDateTime.now());

        Documento guardado = documentoRepository.save(documento);
        log.info("Documento '{}' subido por '{}' para alumno id={} [{}]",
                nombreArchivo, subidoPor.getNombreUsuario(),
                alumno.getId(), contexto);
        return guardado;
    }


    //REVISIÓN DE DOCUMENTOS (RF6)

    /**
     * Valida un documento del expediente. RF6
     * Solo puede hacerlo el Tutor Centro o el Tutor Empresa.
     */
    @Transactional
    public void validar(Long idDocumento, String comentario) {
        Documento documento = getOrThrow(idDocumento);

        if (documento.getEstado() != EstadoDocumentoEnum.PENDIENTE) {
            throw new IllegalStateException(
                    "Solo se pueden validar documentos en estado PENDIENTE. " +
                    "Estado actual: " + documento.getEstado());
        }
        documento.setEstado(EstadoDocumentoEnum.VALIDADO);
        documento.setComentarioRevision(comentario);
        documento.setFechaRevision(LocalDateTime.now());
        documentoRepository.save(documento);

        log.info("Documento id={} ('{}') VALIDADO", idDocumento,
                documento.getNombreArchivo());
    }

    /**
     * Rechaza un documento del expediente. RF6
     * El alumno recibirá una notificación para corregirlo.
     */
    @Transactional
    public void rechazar(Long idDocumento, String motivo) {
        Documento documento = getOrThrow(idDocumento);

        if (documento.getEstado() != EstadoDocumentoEnum.PENDIENTE) {
            throw new IllegalStateException(
                    "Solo se pueden rechazar documentos en estado PENDIENTE. " +
                    "Estado actual: " + documento.getEstado());
        }
        documento.setEstado(EstadoDocumentoEnum.RECHAZADO);
        documento.setComentarioRevision(motivo);
        documento.setFechaRevision(LocalDateTime.now());
        documentoRepository.save(documento);

        log.info("Documento id={} ('{}') RECHAZADO. Motivo: {}",
                idDocumento, documento.getNombreArchivo(), motivo);
    }


    //JUSTIFICANTES DE FALTA (RF22)

    /**
     * Devuelve todos los justificantes de falta de un alumno. RF22
     */
    @Transactional(readOnly = true)
    public List<Documento> findJustificantesByAlumno(Alumno alumno) {
        return documentoRepository.findByAlumnoAndContexto(
                alumno, ContextoDocumentoEnum.JUSTIFICANTE_FALTA);
    }


    //ELIMINACIÓN

    /**
     * Elimina un documento del sistema. RF3
     * Solo debe eliminarse si está en estado RECHAZADO o PENDIENTE.
     * El archivo físico en disco debe borrarse desde el controlador
     * usando FileUploadUtil antes de llamar a este método.
     */
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


    //HELPERS PRIVADOS

    private Documento getOrThrow(Long id) {
        return documentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Documento no encontrado con id: " + id));
    }
}

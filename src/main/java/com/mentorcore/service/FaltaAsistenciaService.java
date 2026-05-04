package com.mentorcore.service;

import com.mentorcore.model.Alumno;
import com.mentorcore.model.Asignacion;
import com.mentorcore.model.Documento;
import com.mentorcore.model.FaltaAsistencia;
import com.mentorcore.model.TutorCentro;
import com.mentorcore.model.TutorEmpresa;
import com.mentorcore.model.enums.EstadoFaltaEnum;
import com.mentorcore.model.enums.TipoFaltaEnum;
import com.mentorcore.repository.FaltaAsistenciaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de faltas de asistencia.
 * Gestiona el ciclo de vida: INJUSTIFICADA → PENDIENTE_REVISION → JUSTIFICADA
 * RF18, RF22
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FaltaAsistenciaService {

    private final FaltaAsistenciaRepository faltaAsistenciaRepository;


    // BÚSQUEDAS

    @Transactional(readOnly = true)
    public Optional<FaltaAsistencia> findById(Long id) {
        return faltaAsistenciaRepository.findDetalleById(id);
    }

    /**
     * Devuelve todas las faltas de un alumno ordenadas por fecha desc. RF22
     */
    @Transactional(readOnly = true)
    public List<FaltaAsistencia> findByAlumno(Alumno alumno) {
        return faltaAsistenciaRepository.findByAlumnoOrderByFechaFaltaDesc(alumno);
    }

    /**
     * Devuelve las faltas de un alumno filtradas por tipo. RF19
     */
    @Transactional(readOnly = true)
    public List<FaltaAsistencia> findByAlumnoAndTipo(Alumno alumno, TipoFaltaEnum tipo) {
        return faltaAsistenciaRepository.findByAlumnoAndTipo(alumno, tipo);
    }

    /**
     * Devuelve las faltas de un alumno filtradas por estado. RF22
     */
    @Transactional(readOnly = true)
    public List<FaltaAsistencia> findByAlumnoAndEstado(Alumno alumno,
                                                       EstadoFaltaEnum estado) {
        return faltaAsistenciaRepository.findByAlumnoAndEstado(alumno, estado);
    }

    /**
     * Devuelve las faltas pendientes de revisión de un alumno. RF22
     */
    @Transactional(readOnly = true)
    public List<FaltaAsistencia> findPendientesByAlumno(Alumno alumno) {
        return findByAlumno(alumno).stream()
                .filter(this::requiereVerificacionEmpresa)
                .toList();
    }

    /**
     * Devuelve todas las faltas de los alumnos de un tutor centro. RF4
     */
    @Transactional(readOnly = true)
    public List<FaltaAsistencia> findByTutorCentro(Long idTutor) {
        return faltaAsistenciaRepository.findByTutorCentro(idTutor);
    }

    /**
     * Cuenta las faltas injustificadas de un alumno. RF4
     */
    @Transactional(readOnly = true)
    public long contarInjustificadas(Alumno alumno) {
        return faltaAsistenciaRepository.countByAlumnoAndTipo(
                alumno, TipoFaltaEnum.INJUSTIFICADA);
    }

    /**
     * Cuenta las faltas justificadas de un alumno. RF4
     */
    @Transactional(readOnly = true)
    public long contarJustificadas(Alumno alumno) {
        return faltaAsistenciaRepository.countByAlumnoAndTipo(
                alumno, TipoFaltaEnum.JUSTIFICADA);
    }


    // REGISTRO DE FALTA

    /**
     * Registra una nueva falta de asistencia. RF18
     * Solo puede registrarla el TutorEmpresa asignado al alumno.
     */
    @Transactional
    public FaltaAsistencia registrar(Alumno alumno, Asignacion asignacion,
                                     TutorEmpresa registradoPor,
                                     LocalDate fechaFalta, TipoFaltaEnum tipo,
                                     String observacion) {

        if (faltaAsistenciaRepository.existsByAlumnoAndFechaFalta(alumno, fechaFalta)) {
            throw new IllegalStateException(
                    "Ya existe una falta registrada para el alumno '"
                            + alumno.getNombreUsuario() + "' el " + fechaFalta);
        }

        FaltaAsistencia falta = new FaltaAsistencia();
        falta.setAlumno(alumno);
        falta.setAsignacion(asignacion);
        falta.setRegistradoPor(registradoPor);
        falta.setFechaFalta(fechaFalta);
        falta.setTipo(tipo);
        falta.setObservacion(observacion);
        falta.setEstado(tipo == TipoFaltaEnum.JUSTIFICADA
                ? EstadoFaltaEnum.JUSTIFICADA
                : EstadoFaltaEnum.INJUSTIFICADA);

        FaltaAsistencia guardada = faltaAsistenciaRepository.save(falta);
        log.info("Falta {} registrada para alumno '{}' el {} por '{}'",
                tipo, alumno.getNombreUsuario(), fechaFalta,
                registradoPor.getNombreUsuario());
        return guardada;
    }

    /**
     * El alumno avisa de una futura falta sin adjuntar todavía justificante.
     */
    @Transactional
    public FaltaAsistencia registrarAvisoAlumno(Alumno alumno, Asignacion asignacion,
                                                LocalDate fechaFalta, String observacion) {
        if (asignacion.getTutorEmpresa() == null) {
            throw new IllegalStateException(
                    "No se puede registrar el aviso porque el alumno no tiene tutor de empresa asignado");
        }

        String observacionNormalizada = (observacion != null && !observacion.isBlank())
                ? "[Aviso del alumno] " + observacion.trim()
                : "[Aviso del alumno] Ausencia comunicada por el alumno";

        return registrar(
                alumno,
                asignacion,
                asignacion.getTutorEmpresa(),
                fechaFalta,
                TipoFaltaEnum.INJUSTIFICADA,
                observacionNormalizada
        );
    }


    // GESTIÓN DE JUSTIFICANTES

    /**
     * Adjunta un justificante a una falta injustificada. RF22
     */
    @Transactional
    public void adjuntarJustificante(Long idFalta, Documento documento,
                                     String motivoJustificacion,
                                     BigDecimal horasAusencia) {
        FaltaAsistencia falta = getOrThrow(idFalta);

        if (falta.getEstado() != EstadoFaltaEnum.INJUSTIFICADA) {
            throw new IllegalStateException(
                    "Solo se puede adjuntar justificante a faltas INJUSTIFICADAS. " +
                            "Estado actual: " + falta.getEstado());
        }

        if (motivoJustificacion == null || motivoJustificacion.isBlank()) {
            throw new IllegalStateException(
                    "Debes indicar el motivo de la falta para adjuntar el justificante");
        }

        if (horasAusencia == null || horasAusencia.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException(
                    "Debes indicar cuántas horas faltaste a la empresa");
        }

        falta.adjuntarJustificante(documento, motivoJustificacion.trim(), horasAusencia);
        faltaAsistenciaRepository.save(falta);

        log.info("Justificante adjuntado a falta id={} (alumno '{}', fecha {})",
                idFalta, falta.getAlumno().getNombreUsuario(), falta.getFechaFalta());
    }

    /**
     * El TutorEmpresa verifica el justificante antes de la revisión final del centro.
     */
    @Transactional
    public void verificarJustificanteEmpresa(Long idFalta, TutorEmpresa tutorEmpresa, String comentario) {
        FaltaAsistencia falta = getOrThrow(idFalta);

        if (!requiereVerificacionEmpresa(falta)) {
            throw new IllegalStateException(
                    "Solo se pueden verificar faltas con justificante pendiente de revisión. " +
                            "Estado actual: " + falta.getEstado());
        }

        if (!falta.getRegistradoPor().getId().equals(tutorEmpresa.getId())) {
            throw new IllegalStateException(
                    "Solo el tutor de empresa asignado puede verificar este justificante");
        }

        falta.verificarPorEmpresa(comentario != null && !comentario.isBlank() ? comentario.trim() : null);
        faltaAsistenciaRepository.save(falta);

        log.info("Justificante VERIFICADO por empresa — falta id={} (alumno '{}', fecha {}) por '{}'",
                idFalta, falta.getAlumno().getNombreUsuario(),
                falta.getFechaFalta(), tutorEmpresa.getNombreUsuario());
    }

    /**
     * El TutorCentro aprueba el justificante. RF22
     */
    @Transactional
    public void aprobarJustificante(Long idFalta, TutorCentro validadoPor, String comentarioRevisionCentro) {
        FaltaAsistencia falta = getOrThrow(idFalta);

        if (falta.getEstado() != EstadoFaltaEnum.VERIFICADA_EMPRESA) {
            throw new IllegalStateException(
                    "Solo se pueden aprobar faltas verificadas por empresa. " +
                            "Estado actual: " + falta.getEstado());
        }

        falta.setTipo(TipoFaltaEnum.JUSTIFICADA);
        falta.aprobarJustificante(
                validadoPor,
                comentarioRevisionCentro != null && !comentarioRevisionCentro.isBlank()
                        ? comentarioRevisionCentro.trim()
                        : null
        );
        faltaAsistenciaRepository.save(falta);

        log.info("Justificante APROBADO — falta id={} (alumno '{}', fecha {}) por '{}'",
                idFalta, falta.getAlumno().getNombreUsuario(),
                falta.getFechaFalta(), validadoPor.getNombreUsuario());
    }

    /**
     * El TutorCentro deniega el justificante. RF22
     */
    @Transactional
    public void denegarJustificante(Long idFalta, TutorCentro validadoPor, String motivo) {
        FaltaAsistencia falta = getOrThrow(idFalta);

        if (falta.getEstado() != EstadoFaltaEnum.PENDIENTE_REVISION &&
            falta.getEstado() != EstadoFaltaEnum.VERIFICADA_EMPRESA) {
            throw new IllegalStateException(
                    "Solo se pueden denegar faltas en revisión o verificadas por empresa. " +
                            "Estado actual: " + falta.getEstado());
        }

        falta.denegarJustificante(validadoPor, motivo);
        faltaAsistenciaRepository.save(falta);

        log.warn("Justificante DENEGADO — falta id={} (alumno '{}', fecha {}) por '{}'. Motivo: {}",
                idFalta, falta.getAlumno().getNombreUsuario(),
                falta.getFechaFalta(), validadoPor.getNombreUsuario(), motivo);
    }


    // VALIDACIONES

    @Transactional(readOnly = true)
    public boolean existeFaltaEnFecha(Alumno alumno, LocalDate fecha) {
        return faltaAsistenciaRepository.existsByAlumnoAndFechaFalta(alumno, fecha);
    }


    // HELPERS PRIVADOS

    private FaltaAsistencia getOrThrow(Long id) {
        return faltaAsistenciaRepository.findDetalleById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Falta de asistencia no encontrada con id: " + id));
    }

    @Transactional(readOnly = true)
    public boolean requiereVerificacionEmpresa(FaltaAsistencia falta) {
        return falta.getJustificante() != null
                && falta.getComentarioVerificacionEmpresa() == null
                && falta.getEstado() != EstadoFaltaEnum.VERIFICADA_EMPRESA
                && falta.getEstado() != EstadoFaltaEnum.JUSTIFICADA;
    }
}

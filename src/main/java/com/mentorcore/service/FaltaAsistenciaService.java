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
        return faltaAsistenciaRepository.findById(id);
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
        return faltaAsistenciaRepository.findByAlumnoAndEstado(
                alumno, EstadoFaltaEnum.PENDIENTE_REVISION);
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


    // GESTIÓN DE JUSTIFICANTES

    /**
     * Adjunta un justificante a una falta injustificada. RF22
     */
    @Transactional
    public void adjuntarJustificante(Long idFalta, Documento documento) {
        FaltaAsistencia falta = getOrThrow(idFalta);

        if (falta.getEstado() != EstadoFaltaEnum.INJUSTIFICADA) {
            throw new IllegalStateException(
                    "Solo se puede adjuntar justificante a faltas INJUSTIFICADAS. " +
                            "Estado actual: " + falta.getEstado());
        }

        falta.adjuntarJustificante(documento);
        faltaAsistenciaRepository.save(falta);

        log.info("Justificante adjuntado a falta id={} (alumno '{}', fecha {})",
                idFalta, falta.getAlumno().getNombreUsuario(), falta.getFechaFalta());
    }

    /**
     * El TutorCentro aprueba el justificante. RF22
     */
    @Transactional
    public void aprobarJustificante(Long idFalta, TutorCentro validadoPor) {
        FaltaAsistencia falta = getOrThrow(idFalta);

        if (falta.getEstado() != EstadoFaltaEnum.PENDIENTE_REVISION) {
            throw new IllegalStateException(
                    "Solo se pueden aprobar faltas en estado PENDIENTE_REVISION. " +
                            "Estado actual: " + falta.getEstado());
        }

        falta.setTipo(TipoFaltaEnum.JUSTIFICADA);
        falta.aprobarJustificante(validadoPor);
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

        if (falta.getEstado() != EstadoFaltaEnum.PENDIENTE_REVISION) {
            throw new IllegalStateException(
                    "Solo se pueden denegar faltas en estado PENDIENTE_REVISION. " +
                            "Estado actual: " + falta.getEstado());
        }

        falta.denegarJustificante(validadoPor);
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
        return faltaAsistenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Falta de asistencia no encontrada con id: " + id));
    }
}


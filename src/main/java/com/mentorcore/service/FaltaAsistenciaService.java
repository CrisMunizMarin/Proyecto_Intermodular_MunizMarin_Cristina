package com.mentorcore.service;

import com.mentorcore.model.Alumno;
import com.mentorcore.model.Documento;
import com.mentorcore.model.FaltaAsistencia;
import com.mentorcore.model.Usuario;
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
     * Cuenta las faltas injustificadas de un alumno. RF4 (dashboard)
     */
    @Transactional(readOnly = true)
    public long contarInjustificadas(Alumno alumno) {
        return faltaAsistenciaRepository.countByAlumnoAndTipo(
                alumno, TipoFaltaEnum.INJUSTIFICADA);
    }

    /**
     * Cuenta las faltas justificadas de un alumno. RF4 (dashboard)
     */
    @Transactional(readOnly = true)
    public long contarJustificadas(Alumno alumno) {
        return faltaAsistenciaRepository.countByAlumnoAndTipo(
                alumno, TipoFaltaEnum.JUSTIFICADA);
    }


    // REGISTRO DE FALTA (RF18)

    /**
     * Registra una nueva falta de asistencia. RF18
     * Solo puede registrarla el TutorEmpresa asignado al alumno.
     * Máximo 1 falta por alumno por día.
     *
     * @param alumno       alumno al que se registra la falta
     * @param registradoPor usuario (TutorEmpresa) que la registra
     * @param fechaFalta   fecha de la falta
     * @param tipo         JUSTIFICADA o INJUSTIFICADA
     * @param observacion  nota opcional del tutor empresa
     */
    @Transactional
    public FaltaAsistencia registrar(Alumno alumno, Usuario registradoPor,
                                      LocalDate fechaFalta, TipoFaltaEnum tipo,
                                      String observacion) {

        // Verificar que no existe ya una falta ese día (RF19)
        if (faltaAsistenciaRepository.existsByAlumnoAndFechaFalta(alumno, fechaFalta)) {
            throw new IllegalStateException(
                    "Ya existe una falta registrada para el alumno '"
                    + alumno.getNombreUsuario() + "' el " + fechaFalta);
        }

        FaltaAsistencia falta = new FaltaAsistencia();
        falta.setAlumno(alumno);
        falta.setRegistradoPor(null);
        falta.setFechaFalta(fechaFalta);
        falta.setTipo(tipo);
        falta.setObservacion(observacion);

        // Estado inicial según el tipo registrado
        falta.setEstado(tipo == TipoFaltaEnum.JUSTIFICADA
                ? EstadoFaltaEnum.JUSTIFICADA
                : EstadoFaltaEnum.INJUSTIFICADA);

        FaltaAsistencia guardada = faltaAsistenciaRepository.save(falta);
        log.info("Falta {} registrada para alumno '{}' el {} por '{}'",
                tipo, alumno.getNombreUsuario(), fechaFalta,
                registradoPor.getNombreUsuario());
        return guardada;
    }


    //GESTIÓN DE JUSTIFICANTES (RF22)

    /**
     * Adjunta un justificante a una falta injustificada. RF22
     * Cambia el estado a PENDIENTE_REVISION para que el TutorCentro lo revise.
     *
     * @param idFalta    ID de la falta a justificar
     * @param documento  documento justificante ya subido mediante DocumentoService
     */
    @Transactional
    public void adjuntarJustificante(Long idFalta, Documento documento) {
        FaltaAsistencia falta = getOrThrow(idFalta);

        if (falta.getEstado() != EstadoFaltaEnum.INJUSTIFICADA) {
            throw new IllegalStateException(
                    "Solo se puede adjuntar justificante a faltas INJUSTIFICADAS. " +
                    "Estado actual: " + falta.getEstado());
        }

        falta.setJustificante(documento);
        falta.setEstado(EstadoFaltaEnum.PENDIENTE_REVISION);
        faltaAsistenciaRepository.save(falta);

        log.info("Justificante adjuntado a falta id={} (alumno '{}', fecha {})",
                idFalta, falta.getAlumno().getNombreUsuario(), falta.getFechaFalta());
    }

    /**
     * El TutorCentro aprueba el justificante → estado JUSTIFICADA. RF22
     *
     * @param idFalta    ID de la falta
     * @param validadoPor usuario (TutorCentro) que valida
     */
    @Transactional
    public void aprobarJustificante(Long idFalta, Usuario validadoPor) {
        FaltaAsistencia falta = getOrThrow(idFalta);

        if (falta.getEstado() != EstadoFaltaEnum.PENDIENTE_REVISION) {
            throw new IllegalStateException(
                    "Solo se pueden aprobar faltas en estado PENDIENTE_REVISION. " +
                    "Estado actual: " + falta.getEstado());
        }

        falta.setEstado(EstadoFaltaEnum.JUSTIFICADA);
        falta.setTipo(TipoFaltaEnum.JUSTIFICADA);
        falta.setValidadoPor(null);
        falta.setFechaValidacion(LocalDate.now().atStartOfDay());
        faltaAsistenciaRepository.save(falta);

        log.info("Justificante APROBADO — falta id={} (alumno '{}', fecha {})",
                idFalta, falta.getAlumno().getNombreUsuario(), falta.getFechaFalta());
    }

    /**
     * El TutorCentro deniega el justificante → vuelve a INJUSTIFICADA. RF22
     *
     * @param idFalta ID de la falta
     * @param motivo  razón del rechazo
     */
    @Transactional
    public void denegarJustificante(Long idFalta, String motivo) {
        FaltaAsistencia falta = getOrThrow(idFalta);

        if (falta.getEstado() != EstadoFaltaEnum.PENDIENTE_REVISION) {
            throw new IllegalStateException(
                    "Solo se pueden denegar faltas en estado PENDIENTE_REVISION. " +
                    "Estado actual: " + falta.getEstado());
        }

        falta.setEstado(EstadoFaltaEnum.INJUSTIFICADA);
        falta.setJustificante(null);
        faltaAsistenciaRepository.save(falta);

        log.warn("Justificante DENEGADO — falta id={} (alumno '{}', fecha {}). Motivo: {}",
                idFalta, falta.getAlumno().getNombreUsuario(),
                falta.getFechaFalta(), motivo);
    }


    // VALIDACIONES

    /**
     * Comprueba si ya existe una falta para ese alumno en esa fecha. RF18
     */
    @Transactional(readOnly = true)
    public boolean existeFaltaEnFecha(Alumno alumno, LocalDate fecha) {
        return faltaAsistenciaRepository.existsByAlumnoAndFechaFalta(alumno, fecha);
    }


    //HELPERS PRIVADOS

    private FaltaAsistencia getOrThrow(Long id) {
        return faltaAsistenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Falta de asistencia no encontrada con id: " + id));
    }
}

package com.mentorcore.service;

import com.mentorcore.model.Alumno;
import com.mentorcore.model.Tarea;
import com.mentorcore.model.Usuario;
import com.mentorcore.model.enums.EstadoValidacionEnum;
import com.mentorcore.repository.TareaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de tareas diarias del alumno.
 * Gestiona el registro, revisión y validación de actividades.
 * RF2, RF5, RF9
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TareaService {

    private final TareaRepository tareaRepository;
    private final AlumnoService alumnoService;


    // BÚSQUEDAS

    @Transactional(readOnly = true)
    public Optional<Tarea> findById(Long id) {
        return tareaRepository.findById(id);
    }

    /**
     * Devuelve todas las tareas de un alumno ordenadas por fecha desc. RF2
     */
    @Transactional(readOnly = true)
    public List<Tarea> findByAlumno(Alumno alumno) {
        return tareaRepository.findByAlumnoOrderByFechaRegistroDesc(alumno);
    }

    /**
     * Devuelve las tareas de un alumno filtradas por estado. RF5
     */
    @Transactional(readOnly = true)
    public List<Tarea> findByAlumnoAndEstado(Alumno alumno, EstadoValidacionEnum estado) {
        return tareaRepository.findByAlumnoAndEstadoValidacion(alumno, estado);
    }

    /**
     * Devuelve las tareas pendientes de revisión de un alumno. RF5
     */
    @Transactional(readOnly = true)
    public List<Tarea> findPendientesByAlumno(Alumno alumno) {
        return tareaRepository.findByAlumnoAndEstadoValidacion(
                alumno, EstadoValidacionEnum.PENDIENTE);
    }

    /**
     * Devuelve todas las tareas pendientes de los alumnos de un tutor centro. RF5
     */
    @Transactional(readOnly = true)
    public List<Tarea> findPendientesByTutorCentro(Long idTutor) {
        return tareaRepository.findPendientesByTutorCentro(idTutor);
    }

    /**
     * Devuelve las tareas de un alumno dentro de un rango de fechas. RF8
     */
    @Transactional(readOnly = true)
    public List<Tarea> findByAlumnoAndFechaBetween(Alumno alumno,
                                                   LocalDate inicio,
                                                   LocalDate fin) {
        return tareaRepository.findByAlumnoAndFechaBetween(alumno, inicio, fin);
    }


    // REGISTRO Y EDICIÓN

    /**
     * Registra una nueva tarea diaria del alumno. RF2
     * Se crea siempre en estado PENDIENTE para revisión posterior.
     */
    @Transactional
    public Tarea registrar(Tarea tarea) {
        tarea.setId(null);
        tarea.setEstadoValidacion(EstadoValidacionEnum.PENDIENTE);
        tarea.setComentarioTutor(null);
        tarea.setValidador(null);
        tarea.setFechaValidacion(null);

        Tarea guardada = tareaRepository.save(tarea);
        log.info("Tarea registrada para alumno '{}' en fecha {} ({} h)",
                guardada.getAlumno().getNombreUsuario(),
                guardada.getFechaRegistro(),
                guardada.getHorasDedicadas());
        return guardada;
    }

    /**
     * Actualiza una tarea existente. RF2
     * Solo se permite editar tareas en estado PENDIENTE o REQUIERE_REVISION.
     */
    @Transactional
    public Tarea actualizar(Tarea tarea) {
        Tarea existente = getOrThrow(tarea.getId());

        if (existente.getEstadoValidacion() == EstadoValidacionEnum.VALIDADO ||
            existente.getEstadoValidacion() == EstadoValidacionEnum.RECHAZADO) {
            throw new IllegalStateException(
                    "No se puede editar una tarea ya revisada. " +
                    "Estado actual: " + existente.getEstadoValidacion());
        }

        existente.setFechaRegistro(tarea.getFechaRegistro());
        existente.setDescripcion(tarea.getDescripcion());
        existente.setHorasDedicadas(tarea.getHorasDedicadas());
        existente.setAreaActividad(tarea.getAreaActividad());

        // Al editarla vuelve a quedar pendiente de revisión
        existente.setEstadoValidacion(EstadoValidacionEnum.PENDIENTE);
        existente.setComentarioTutor(null);
        existente.setValidador(null);
        existente.setFechaValidacion(null);

        Tarea guardada = tareaRepository.save(existente);
        log.info("Tarea id={} actualizada para alumno '{}'",
                guardada.getId(), guardada.getAlumno().getNombreUsuario());
        return guardada;
    }


    // REVISIÓN Y VALIDACIÓN

    /**
     * Valida una tarea registrada por el alumno. RF5
     * Al validarla, se acumulan sus horas al progreso del alumno.
     */
    @Transactional
    public void validar(Long idTarea, Usuario tutor, String comentario) {
        Tarea tarea = getOrThrow(idTarea);

        if (tarea.getEstadoValidacion() == EstadoValidacionEnum.VALIDADO) {
            throw new IllegalStateException(
                    "La tarea ya está VALIDADA");
        }

        tarea.validar(tutor, comentario);
        tareaRepository.save(tarea);

        // Persistimos explícitamente el cambio de horas del alumno
        alumnoService.actualizar(tarea.getAlumno());

        log.info("Tarea id={} VALIDADA por '{}' para alumno '{}'",
                tarea.getId(),
                tutor.getNombreUsuario(),
                tarea.getAlumno().getNombreUsuario());
    }

    /**
     * Rechaza una tarea registrada por el alumno. RF5
     * Si la tarea estaba validada previamente, se descuentan sus horas.
     */
    @Transactional
    public void rechazar(Long idTarea, Usuario tutor, String motivo) {
        Tarea tarea = getOrThrow(idTarea);

        if (tarea.getEstadoValidacion() == EstadoValidacionEnum.RECHAZADO) {
            throw new IllegalStateException(
                    "La tarea ya está RECHAZADA");
        }

        if (tarea.getEstadoValidacion() == EstadoValidacionEnum.VALIDADO) {
            alumnoService.descontarHoras(
                    tarea.getAlumno().getId(),
                    tarea.getHorasDedicadas());
        }

        tarea.rechazar(tutor, motivo);
        tareaRepository.save(tarea);

        log.info("Tarea id={} RECHAZADA por '{}' para alumno '{}'",
                tarea.getId(),
                tutor.getNombreUsuario(),
                tarea.getAlumno().getNombreUsuario());
    }

    /**
     * Marca una tarea como REQUIERE_REVISION para que el alumno la corrija. RF5
     */
    @Transactional
    public void marcarRequiereRevision(Long idTarea, Usuario tutor, String comentario) {
        Tarea tarea = getOrThrow(idTarea);

        if (tarea.getEstadoValidacion() == EstadoValidacionEnum.VALIDADO) {
            throw new IllegalStateException(
                    "No se puede pedir revisión sobre una tarea ya VALIDADA");
        }

        tarea.setEstadoValidacion(EstadoValidacionEnum.REQUIERE_REVISION);
        tarea.setValidador(tutor);
        tarea.setComentarioTutor(comentario);
        tarea.setFechaValidacion(java.time.LocalDateTime.now());

        tareaRepository.save(tarea);

        log.info("Tarea id={} marcada como REQUIERE_REVISION por '{}' para alumno '{}'",
                tarea.getId(),
                tutor.getNombreUsuario(),
                tarea.getAlumno().getNombreUsuario());
    }


    // MÉTRICAS Y RESUMEN

    /**
     * Cuenta las tareas validadas de un alumno. RF4, RF8
     */
    @Transactional(readOnly = true)
    public long contarValidadas(Alumno alumno) {
        return tareaRepository.countByAlumnoAndEstadoValidacion(
                alumno, EstadoValidacionEnum.VALIDADO);
    }

    /**
     * Cuenta las tareas pendientes de un alumno. RF4, RF8
     */
    @Transactional(readOnly = true)
    public long contarPendientes(Alumno alumno) {
        return tareaRepository.countByAlumnoAndEstadoValidacion(
                alumno, EstadoValidacionEnum.PENDIENTE)
                + tareaRepository.countByAlumnoAndEstadoValidacion(
                alumno, EstadoValidacionEnum.REQUIERE_REVISION);
    }


    /**
     * Cuenta las tareas rechazadas de un alumno. RF4, RF8
     */
    @Transactional(readOnly = true)
    public long contarRechazadas(Alumno alumno) {
        return tareaRepository.countByAlumnoAndEstadoValidacion(
                alumno, EstadoValidacionEnum.RECHAZADO);
    }


    // ELIMINACIÓN

    /**
     * Elimina una tarea del sistema. RF2
     * Solo se permite eliminar tareas no validadas.
     */
    @Transactional
    public void eliminar(Long idTarea) {
        Tarea tarea = getOrThrow(idTarea);

        if (tarea.getEstadoValidacion() == EstadoValidacionEnum.VALIDADO) {
            throw new IllegalStateException(
                    "No se puede eliminar una tarea ya VALIDADA");
        }

        tareaRepository.deleteById(idTarea);
        log.info("Tarea id={} eliminada para alumno '{}'",
                idTarea, tarea.getAlumno().getNombreUsuario());
    }


    // HELPERS PRIVADOS

    private Tarea getOrThrow(Long id) {
        return tareaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Tarea no encontrada con id: " + id));
    }
}


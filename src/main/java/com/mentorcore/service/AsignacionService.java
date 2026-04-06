package com.mentorcore.service;

import com.mentorcore.model.Alumno;
import com.mentorcore.model.Asignacion;
import com.mentorcore.model.Empresa;
import com.mentorcore.model.PeriodoFormacion;
import com.mentorcore.model.TutorEmpresa;
import com.mentorcore.model.enums.EstadoFeEnum;
import com.mentorcore.repository.AsignacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de asignaciones alumno-empresa-tutorEmpresa.
 * Gestiona el historial completo de asignaciones y las reasignaciones.
 * RF13, RF21
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AsignacionService {

    private final AsignacionRepository asignacionRepository;
    private final AlumnoService alumnoService;


    // ── BÚSQUEDAS ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Optional<Asignacion> findById(Long id) {
        return asignacionRepository.findById(id);
    }

    /**
     * Devuelve la asignación EN_CURSO de un alumno. RF13
     * Solo puede haber 1 activa por alumno en cada momento.
     */
    @Transactional(readOnly = true)
    public Optional<Asignacion> findAsignacionActiva(Alumno alumno) {
        return asignacionRepository.findByAlumnoAndEstado(alumno, EstadoFeEnum.EN_CURSO);
    }

    /**
     * Devuelve todo el historial de asignaciones de un alumno. RF21
     */
    @Transactional(readOnly = true)
    public List<Asignacion> findHistorialByAlumno(Alumno alumno) {
        return asignacionRepository.findByAlumnoOrderByFechaCreacionDesc(alumno);
    }

    /**
     * Devuelve todas las asignaciones activas de un tutor empresa. RF9
     */
    @Transactional(readOnly = true)
    public List<Asignacion> findActivasByTutorEmpresa(TutorEmpresa tutorEmpresa) {
        return asignacionRepository
                .findByTutorEmpresaAndEstado(tutorEmpresa, EstadoFeEnum.EN_CURSO);
    }

    /**
     * Devuelve todas las asignaciones de un periodo FE. RF20
     */
    @Transactional(readOnly = true)
    public List<Asignacion> findByPeriodo(Long idPeriodo) {
        return asignacionRepository.findActivasByPeriodo(idPeriodo);
    }


    // ── CREACIÓN ─────────────────────────────────────────────────────────────

    /**
     * Crea una nueva asignación alumno-empresa-tutorEmpresa. RF13
     * Verifica que el alumno no tenga ya una asignación EN_CURSO.
     */
    @Transactional
    public Asignacion crear(Alumno alumno, Empresa empresa,
                            TutorEmpresa tutorEmpresa, PeriodoFormacion periodo,
                            LocalDate fechaInicio) {

        // Verificar que no existe ya una asignación activa
        if (asignacionRepository.existsByAlumnoAndEstado(alumno, EstadoFeEnum.EN_CURSO)) {
            throw new IllegalStateException(
                    "El alumno '" + alumno.getNombreUsuario() +
                    "' ya tiene una asignación EN_CURSO. " +
                    "Usa reasignar() para cambiar de empresa.");
        }

        Asignacion asignacion = new Asignacion();
        asignacion.setAlumno(alumno);
        asignacion.setEmpresa(empresa);
        asignacion.setTutorEmpresa(tutorEmpresa);
        asignacion.setPeriodo(periodo);
        asignacion.setFechaInicio(fechaInicio);
        asignacion.setEstado(EstadoFeEnum.EN_CURSO);

        Asignacion guardada = asignacionRepository.save(asignacion);
        log.info("Asignación creada: alumno='{}' → empresa='{}' (periodo id={})",
                alumno.getNombreUsuario(), empresa.getNombre(), periodo.getId());
        return guardada;
    }


    // ── REASIGNACIÓN (RF21) ───────────────────────────────────────────────────

    /**
     * Reasigna un alumno a una nueva empresa conservando el historial. RF21
     *
     * Pasos:
     * 1. Marca la asignación actual como FINALIZADO.
     * 2. Crea una nueva asignación EN_CURSO con la nueva empresa.
     *
     * Las horas acumuladas NO se reinician (siguen en Alumno.horasCompletadas).
     */
    @Transactional
    public Asignacion reasignar(Alumno alumno, Empresa nuevaEmpresa,
                                TutorEmpresa nuevoTutorEmpresa,
                                PeriodoFormacion periodo,
                                LocalDate nuevaFechaInicio,
                                String motivoCambio) {

        // 1. Cerrar la asignación activa actual
        Asignacion actual = findAsignacionActiva(alumno)
                .orElseThrow(() -> new IllegalStateException(
                        "No existe asignación EN_CURSO para el alumno '"
                        + alumno.getNombreUsuario() + "'"));

        actual.setEstado(EstadoFeEnum.FINALIZADO);
        actual.setFechaFin(nuevaFechaInicio.minusDays(1));
        actual.setMotivoCambio(motivoCambio);
        asignacionRepository.save(actual);

        log.info("Asignación id={} finalizada — alumno='{}' deja empresa='{}'",
                actual.getId(), alumno.getNombreUsuario(),
                actual.getEmpresa().getNombre());

        // 2. Crear nueva asignación EN_CURSO
        Asignacion nueva = new Asignacion();
        nueva.setAlumno(alumno);
        nueva.setEmpresa(nuevaEmpresa);
        nueva.setTutorEmpresa(nuevoTutorEmpresa);
        nueva.setPeriodo(periodo);
        nueva.setFechaInicio(nuevaFechaInicio);
        nueva.setEstado(EstadoFeEnum.EN_CURSO);

        Asignacion guardada = asignacionRepository.save(nueva);
        log.info("Nueva asignación id={} creada — alumno='{}' → empresa='{}'",
                guardada.getId(), alumno.getNombreUsuario(), nuevaEmpresa.getNombre());

        return guardada;
    }

    /**
     * Finaliza una asignación manualmente. RF21
     */
    @Transactional
    public void finalizar(Asignacion asignacion, LocalDate fechaFin, String motivo) {
        asignacion.setEstado(EstadoFeEnum.FINALIZADO);
        asignacion.setFechaFin(fechaFin);
        asignacion.setMotivoCambio(motivo);
        asignacionRepository.save(asignacion);
        log.info("Asignación id={} finalizada manualmente", asignacion.getId());
    }


    // ── VALIDACIONES ──────────────────────────────────────────────────────────

    /**
     * Comprueba si un alumno tiene asignación activa. RF13
     */
    @Transactional(readOnly = true)
    public boolean tieneAsignacionActiva(Alumno alumno) {
        return asignacionRepository.existsByAlumnoAndEstado(alumno, EstadoFeEnum.EN_CURSO);
    }

    /**
     * Verifica que un tutor empresa tiene asignado a ese alumno. RF9, RF18
     * Necesario para control de acceso: el tutor solo ve sus alumnos.
     */
    @Transactional(readOnly = true)
    public boolean tutorTieneAlumnoAsignado(TutorEmpresa tutorEmpresa, Alumno alumno) {
        return asignacionRepository
                .findByAlumnoAndEstado(alumno, EstadoFeEnum.EN_CURSO)
                .map(a -> a.getTutorEmpresa().getId().equals(tutorEmpresa.getId()))
                .orElse(false);
    }
}

package com.mentorcore.service;

import com.mentorcore.model.Alumno;
import com.mentorcore.model.Asignacion;
import com.mentorcore.model.Empresa;
import com.mentorcore.model.PeriodoFormacion;
import com.mentorcore.model.TutorEmpresa;
import com.mentorcore.model.Usuario;
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


    // BÚSQUEDAS

    @Transactional(readOnly = true)
    public Optional<Asignacion> findById(Long id) {
        return asignacionRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Asignacion> findAsignacionActiva(Alumno alumno) {
        return asignacionRepository.findByAlumnoAndEstado(alumno, EstadoFeEnum.EN_CURSO);
    }

    @Transactional(readOnly = true)
    public List<Asignacion> findHistorialByAlumno(Alumno alumno) {
        return asignacionRepository.findByAlumnoOrderByFechaCreacionDesc(alumno);
    }

    @Transactional(readOnly = true)
    public List<Asignacion> findActivasByTutorEmpresa(TutorEmpresa tutorEmpresa) {
        return asignacionRepository
                .findByTutorEmpresaAndEstado(tutorEmpresa, EstadoFeEnum.EN_CURSO);
    }

    @Transactional(readOnly = true)
    public List<Asignacion> findByPeriodo(Long idPeriodo) {
        return asignacionRepository.findActivasByPeriodo(idPeriodo);
    }


    // CREACIÓN

    @Transactional
    public Asignacion crear(Alumno alumno, Empresa empresa,
                            TutorEmpresa tutorEmpresa, PeriodoFormacion periodo,
                            LocalDate fechaInicio) {

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


    // REASIGNACIÓN

    @Transactional
    public Asignacion reasignar(Alumno alumno, Empresa nuevaEmpresa,
                                TutorEmpresa nuevoTutorEmpresa,
                                PeriodoFormacion periodo,
                                LocalDate nuevaFechaInicio,
                                String motivoCambio,
                                Usuario reasignadoPor) {

        Asignacion actual = findAsignacionActiva(alumno)
                .orElseThrow(() -> new IllegalStateException(
                        "No existe asignación EN_CURSO para el alumno '"
                                + alumno.getNombreUsuario() + "'"));

        actual.setEstado(EstadoFeEnum.FINALIZADO);
        actual.setFechaFin(nuevaFechaInicio.minusDays(1));
        actual.setMotivoCambio(motivoCambio);
        actual.setReasignadoPor(reasignadoPor);
        asignacionRepository.save(actual);

        log.info("Asignación id={} finalizada — alumno='{}' deja empresa='{}'",
                actual.getId(), alumno.getNombreUsuario(),
                actual.getEmpresa().getNombre());

        Asignacion nueva = new Asignacion();
        nueva.setAlumno(alumno);
        nueva.setEmpresa(nuevaEmpresa);
        nueva.setTutorEmpresa(nuevoTutorEmpresa);
        nueva.setPeriodo(periodo);
        nueva.setFechaInicio(nuevaFechaInicio);
        nueva.setEstado(EstadoFeEnum.EN_CURSO);
        nueva.setReasignadoPor(reasignadoPor);

        Asignacion guardada = asignacionRepository.save(nueva);
        log.info("Nueva asignación id={} creada — alumno='{}' → empresa='{}' por '{}'",
                guardada.getId(), alumno.getNombreUsuario(),
                nuevaEmpresa.getNombre(),
                reasignadoPor != null ? reasignadoPor.getNombreUsuario() : "sistema");

        return guardada;
    }

    @Transactional
    public void finalizar(Asignacion asignacion, LocalDate fechaFin, String motivo) {
        asignacion.setEstado(EstadoFeEnum.FINALIZADO);
        asignacion.setFechaFin(fechaFin);
        asignacion.setMotivoCambio(motivo);
        asignacionRepository.save(asignacion);
        log.info("Asignación id={} finalizada manualmente", asignacion.getId());
    }


    // VALIDACIONES

    @Transactional(readOnly = true)
    public boolean tieneAsignacionActiva(Alumno alumno) {
        return asignacionRepository.existsByAlumnoAndEstado(alumno, EstadoFeEnum.EN_CURSO);
    }

    @Transactional(readOnly = true)
    public boolean tutorTieneAlumnoAsignado(TutorEmpresa tutorEmpresa, Alumno alumno) {
        return asignacionRepository
                .findByAlumnoAndEstado(alumno, EstadoFeEnum.EN_CURSO)
                .map(a -> a.getTutorEmpresa().getId().equals(tutorEmpresa.getId()))
                .orElse(false);
    }
}


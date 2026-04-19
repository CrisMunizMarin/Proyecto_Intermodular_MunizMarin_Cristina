package com.mentorcore.service;

import com.mentorcore.model.Alumno;
import com.mentorcore.model.CursoAcademico;
import com.mentorcore.model.TutorCentro;
import com.mentorcore.repository.AlumnoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de alumnos.
 * Gestiona el perfil extendido del actor Alumno, sus horas
 * de FE y las consultas de progreso.
 * RF2, RF3, RF13, RF22, RNF5
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlumnoService {

    private final AlumnoRepository alumnoRepository;


    //BÚSQUEDAS

    /**
     * Busca un alumno por su ID de usuario (PK compartida con Usuario).
     */
    @Transactional(readOnly = true)
    public Optional<Alumno> findById(Long id) {
        return alumnoRepository.findDetalleById(id);
    }

    /**
     * Devuelve todos los alumnos del sistema. RF10
     */
    @Transactional(readOnly = true)
    public List<Alumno> findAll() {
        return alumnoRepository.findAll();
    }

    /**
     * Devuelve todos los alumnos asignados a un tutor de centro. RF4, RF13
     */
    @Transactional(readOnly = true)
    public List<Alumno> findByTutorCentro(TutorCentro tutorCentro) {
        return alumnoRepository.findByTutorCentro(tutorCentro);
    }

    /**
     * Devuelve todos los alumnos de un curso académico. RF13
     */
    @Transactional(readOnly = true)
    public List<Alumno> findByCursoAcademico(CursoAcademico cursoAcademico) {
        return alumnoRepository.findByCursoAcademico(cursoAcademico);
    }

    /**
     * Busca un alumno por su DNI. RNF5 (dato sensible RGPD)
     */
    @Transactional(readOnly = true)
    public Optional<Alumno> findByDni(String dni) {
        return alumnoRepository.findByDni(dni);
    }

    /**
     * Busca un alumno por nombre de usuario (heredado de Usuario). RF1
     */
    @Transactional(readOnly = true)
    public Optional<Alumno> findByNombreUsuario(String nombreUsuario) {
        return alumnoRepository.findByNombreUsuario(nombreUsuario);
    }


    // CREACIÓN Y MODIFICACIÓN 

    /**
     * Guarda un alumno nuevo en el sistema. RF13
     * La contraseña ya viene cifrada desde UsuarioService.
     */
    @Transactional
    public Alumno guardar(Alumno alumno) {
        log.info("Creando alumno: '{}'", alumno.getNombreUsuario());
        return alumnoRepository.save(alumno);
    }

    /**
     * Actualiza los datos de un alumno existente. RF11
     */
    @Transactional
    public Alumno actualizar(Alumno alumno) {
        log.info("Actualizando alumno id={}", alumno.getId());
        return alumnoRepository.save(alumno);
    }

    /**
     * Asigna o reasigna el tutor de centro a un alumno. RF13
     */
    @Transactional
    public void asignarTutorCentro(Long idAlumno, TutorCentro tutorCentro) {
        alumnoRepository.findById(idAlumno).ifPresent(alumno -> {
            alumno.setTutorCentro(tutorCentro);
            alumnoRepository.save(alumno);
            log.info("Alumno id={} asignado al tutor centro id={}",
                    idAlumno, tutorCentro.getId());
        });
    }

    /**
     * Asigna o reasigna el curso académico a un alumno. RF13
     */
    @Transactional
    public void asignarCurso(Long idAlumno, CursoAcademico cursoAcademico) {
        alumnoRepository.findById(idAlumno).ifPresent(alumno -> {
            alumno.setCursoAcademico(cursoAcademico);
            alumnoRepository.save(alumno);
            log.info("Alumno id={} asignado al curso id={}",
                    idAlumno, cursoAcademico.getId());
        });
    }


    // GESTIÓN DE HORAS FE (RF2)

    /**
     * Acumula horas al contador del alumno tras validar una tarea.
     * Solo debe llamarse desde TareaService cuando el tutor valida. RF5
     *
     * @param idAlumno ID del alumno
     * @param horas    horas a sumar (deben ser > 0)
     */
    @Transactional
    public void acumularHoras(Long idAlumno, BigDecimal horas) {
        if (horas == null || horas.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Intento de acumular horas inválidas ({}) para alumno id={}",
                    horas, idAlumno);
            return;
        }
        alumnoRepository.findById(idAlumno).ifPresent(alumno -> {
            alumno.acumularHoras(horas);
            alumnoRepository.save(alumno);
            log.info("Alumno id={} — acumuladas {} h (total: {} h)",
                    idAlumno, horas, alumno.getHorasCompletadas());
        });
    }

    /**
     * Descuenta horas del contador cuando se rechaza una tarea validada. RF5
     *
     * @param idAlumno ID del alumno
     * @param horas    horas a restar (deben ser > 0)
     */
    @Transactional
    public void descontarHoras(Long idAlumno, BigDecimal horas) {
        if (horas == null || horas.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        alumnoRepository.findById(idAlumno).ifPresent(alumno -> {
            BigDecimal nuevasHoras = alumno.getHorasCompletadas().subtract(horas);
            alumno.setHorasCompletadas(
                    nuevasHoras.compareTo(BigDecimal.ZERO) < 0
                            ? BigDecimal.ZERO
                            : nuevasHoras);
            alumnoRepository.save(alumno);
            log.info("Alumno id={} — descontadas {} h (total: {} h)",
                    idAlumno, horas, alumno.getHorasCompletadas());
        });
    }

    /**
     * Configura las horas totales de FE de un alumno.
     * Por defecto son 400h, pero el administrador puede ajustarlo. RF12
     */
    @Transactional
    public void configurarHorasTotales(Long idAlumno, int horasTotales) {
        alumnoRepository.findById(idAlumno).ifPresent(alumno -> {
            alumno.setHorasTotalesFe(horasTotales);
            alumnoRepository.save(alumno);
            log.info("Alumno id={} — horas totales FE configuradas a {}",
                    idAlumno, horasTotales);
        });
    }


    //CONSULTAS DE PROGRESO (RF2)

    /**
     * Devuelve el porcentaje de horas completadas (0.0 – 100.0).
     */
    @Transactional(readOnly = true)
    public double getPorcentajeCompletado(Long idAlumno) {
        return alumnoRepository.findById(idAlumno)
                .map(Alumno::calcularPorcentajeCompletado)
                .orElse(0.0);
    }

    /**
     * Comprueba si el alumno ha completado todas las horas requeridas.
     */
    @Transactional(readOnly = true)
    public boolean haCompletadoHoras(Long idAlumno) {
        return alumnoRepository.findById(idAlumno)
                .map(Alumno::haCompletadoHoras)
                .orElse(false);
    }


    //VALIDACIONES

    /**
     * Comprueba si ya existe un alumno con ese DNI. RNF5
     */
    public boolean existeDni(String dni) {
        return alumnoRepository.existsByDni(dni);
    }
}

package com.mentorcore.service;

import com.mentorcore.model.TutorCentro;
import com.mentorcore.repository.TutorCentroRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de tutores del centro.
 * Gestiona búsquedas, creación y mantenimiento de su perfil docente.
 * RF4, RF5, RF8, RF13, RF15
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TutorCentroService {

    private final TutorCentroRepository tutorCentroRepository;


    // BÚSQUEDAS

    @Transactional(readOnly = true)
    public Optional<TutorCentro> findById(Long id) {
        return tutorCentroRepository.findById(id);
    }

    /**
     * Devuelve todos los tutores del centro. RF13
     */
    @Transactional(readOnly = true)
    public List<TutorCentro> findAll() {
        return tutorCentroRepository.findAll();
    }

    /**
     * Devuelve solo los tutores del centro activos. RF13
     */
    @Transactional(readOnly = true)
    public List<TutorCentro> findActivos() {
        return tutorCentroRepository.findAllActivos();
    }

    /**
     * Busca un tutor de centro por email. RF13
     */
    @Transactional(readOnly = true)
    public Optional<TutorCentro> findByEmail(String email) {
        return tutorCentroRepository.findByEmail(email);
    }

    /**
     * Busca tutores por departamento. RF13
     */
    @Transactional(readOnly = true)
    public List<TutorCentro> findByDepartamento(String departamento) {
        return tutorCentroRepository.findByDepartamento(departamento);
    }

    /**
     * Búsqueda libre por nombre o apellidos. RF15
     */
    @Transactional(readOnly = true)
    public List<TutorCentro> buscar(String texto) {
        if (texto == null || texto.isBlank()) {
            return findActivos();
        }
        return tutorCentroRepository.buscarPorNombre(texto.trim());
    }


    // CREACIÓN Y MODIFICACIÓN

    /**
     * Guarda un nuevo tutor de centro en el sistema. RF13
     */
    @Transactional
    public TutorCentro guardar(TutorCentro tutorCentro) {
        log.info("Creando tutor de centro: '{}'", tutorCentro.getNombreUsuario());
        return tutorCentroRepository.save(tutorCentro);
    }

    /**
     * Actualiza los datos de un tutor de centro existente. RF13
     */
    @Transactional
    public TutorCentro actualizar(TutorCentro tutorCentro) {
        log.info("Actualizando tutor de centro id={}", tutorCentro.getId());
        return tutorCentroRepository.save(tutorCentro);
    }

    /**
     * Actualiza únicamente los datos profesionales del tutor. RF13
     */
    @Transactional
    public TutorCentro actualizarDatosProfesionales(Long idTutor,
                                                    String departamento,
                                                    String especialidad,
                                                    String numExpedienteDocente) {
        TutorCentro tutor = getOrThrow(idTutor);

        tutor.setDepartamento(departamento);
        tutor.setEspecialidad(especialidad);
        tutor.setNumExpedienteDocente(numExpedienteDocente);

        TutorCentro guardado = tutorCentroRepository.save(tutor);
        log.info("Datos profesionales actualizados para tutor de centro id={}",
                guardado.getId());
        return guardado;
    }


    // VALIDACIONES

    /**
     * Comprueba si existe un tutor de centro con ese email. RF13
     */
    @Transactional(readOnly = true)
    public boolean existeEmail(String email) {
        return tutorCentroRepository.findByEmail(email).isPresent();
    }


    // HELPERS PRIVADOS

    private TutorCentro getOrThrow(Long id) {
        return tutorCentroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Tutor de centro no encontrado con id: " + id));
    }
}


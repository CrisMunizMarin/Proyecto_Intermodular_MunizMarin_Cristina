package com.mentorcore.service;

import com.mentorcore.model.Empresa;
import com.mentorcore.model.TutorEmpresa;
import com.mentorcore.repository.TutorEmpresaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de tutores de empresa.
 * Gestiona búsquedas, creación y mantenimiento del perfil profesional.
 * RF6, RF7, RF9, RF13, RF15, RF19
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TutorEmpresaService {

    private final TutorEmpresaRepository tutorEmpresaRepository;


    // BÚSQUEDAS

    @Transactional(readOnly = true)
    public Optional<TutorEmpresa> findById(Long id) {
        return tutorEmpresaRepository.findById(id);
    }

    /**
     * Devuelve todos los tutores de empresa del sistema. RF13
     */
    @Transactional(readOnly = true)
    public List<TutorEmpresa> findAll() {
        return tutorEmpresaRepository.findAll();
    }

    /**
     * Busca un tutor de empresa por email. RF13
     */
    @Transactional(readOnly = true)
    public Optional<TutorEmpresa> findByEmail(String email) {
        return tutorEmpresaRepository.findByEmail(email);
    }

    /**
     * Devuelve todos los tutores pertenecientes a una empresa. RF18
     */
    @Transactional(readOnly = true)
    public List<TutorEmpresa> findByEmpresa(Empresa empresa) {
        return tutorEmpresaRepository.findByEmpresa(empresa);
    }

    /**
     * Devuelve todos los tutores de una empresa por su id. RF18
     */
    @Transactional(readOnly = true)
    public List<TutorEmpresa> findByEmpresaId(Long idEmpresa) {
        return tutorEmpresaRepository.findByEmpresa_Id(idEmpresa);
    }

    /**
     * Búsqueda libre por nombre o apellidos. RF15
     */
    @Transactional(readOnly = true)
    public List<TutorEmpresa> buscar(String texto) {
        if (texto == null || texto.isBlank()) {
            return findAll();
        }
        return tutorEmpresaRepository.buscarPorNombre(texto.trim());
    }


    // CREACIÓN Y MODIFICACIÓN

    /**
     * Guarda un nuevo tutor de empresa en el sistema. RF13
     */
    @Transactional
    public TutorEmpresa guardar(TutorEmpresa tutorEmpresa) {
        log.info("Creando tutor de empresa: '{}'", tutorEmpresa.getNombreUsuario());
        return tutorEmpresaRepository.save(tutorEmpresa);
    }

    /**
     * Actualiza los datos de un tutor de empresa existente. RF13
     */
    @Transactional
    public TutorEmpresa actualizar(TutorEmpresa tutorEmpresa) {
        log.info("Actualizando tutor de empresa id={}", tutorEmpresa.getId());
        return tutorEmpresaRepository.save(tutorEmpresa);
    }

    /**
     * Actualiza únicamente los datos profesionales del tutor de empresa. RF19
     */
    @Transactional
    public TutorEmpresa actualizarDatosProfesionales(Long idTutor,
                                                     Empresa empresa,
                                                     String cargo,
                                                     String departamentoEmpresa) {
        TutorEmpresa tutor = getOrThrow(idTutor);

        tutor.setEmpresa(empresa);
        tutor.setCargo(cargo);
        tutor.setDepartamentoEmpresa(departamentoEmpresa);

        TutorEmpresa guardado = tutorEmpresaRepository.save(tutor);
        log.info("Datos profesionales actualizados para tutor de empresa id={}",
                guardado.getId());
        return guardado;
    }


    // VALIDACIONES

    /**
     * Comprueba si existe un tutor de empresa con ese email. RF13
     */
    @Transactional(readOnly = true)
    public boolean existeEmail(String email) {
        return tutorEmpresaRepository.findByEmail(email).isPresent();
    }


    // HELPERS PRIVADOS

    private TutorEmpresa getOrThrow(Long id) {
        return tutorEmpresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Tutor de empresa no encontrado con id: " + id));
    }
}


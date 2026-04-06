package com.mentorcore.service;

import com.mentorcore.model.CursoAcademico;
import com.mentorcore.model.enums.NivelEnum;
import com.mentorcore.repository.CursoAcademicoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de cursos académicos.
 * Gestiona los cursos formativos a los que pertenecen los alumnos.
 * RF12, RF13, RF20
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CursoAcademicoService {

    private final CursoAcademicoRepository cursoAcademicoRepository;


    //BÚSQUEDAS

    @Transactional(readOnly = true)
    public Optional<CursoAcademico> findById(Long id) {
        return cursoAcademicoRepository.findById(id);
    }

    /**
     * Devuelve todos los cursos del sistema. RF12
     */
    @Transactional(readOnly = true)
    public List<CursoAcademico> findAll() {
        return cursoAcademicoRepository.findAll();
    }

    /**
     * Devuelve solo los cursos activos. RF12
     */
    @Transactional(readOnly = true)
    public List<CursoAcademico> findActivos() {
        return cursoAcademicoRepository.findByActivoTrue();
    }

    /**
     * Busca un curso por su código oficial (ej: 2VIFC303). RF12
     */
    @Transactional(readOnly = true)
    public Optional<CursoAcademico> findByCodigo(String codigoCurso) {
        return cursoAcademicoRepository.findByCodigoCurso(codigoCurso);
    }

    /**
     * Devuelve todos los cursos de un año académico (ej: 2025-2026). RF12
     */
    @Transactional(readOnly = true)
    public List<CursoAcademico> findByAnioAcademico(String anioAcademico) {
        return cursoAcademicoRepository.findByAnioAcademico(anioAcademico);
    }

    /**
     * Devuelve cursos filtrados por nivel y año académico. RF12
     * Útil para mostrar solo los cursos de 1º o 2º en un año concreto.
     */
    @Transactional(readOnly = true)
    public List<CursoAcademico> findByNivelAndAnio(NivelEnum nivel, String anioAcademico) {
        return cursoAcademicoRepository.findByNivelAndAnioAcademico(nivel, anioAcademico);
    }


    //CREACIÓN Y MODIFICACIÓN

    /**
     * Crea un nuevo curso académico. RF12
     * Verifica que el código no esté duplicado.
     */
    @Transactional
    public CursoAcademico crear(CursoAcademico curso) {
        if (cursoAcademicoRepository.existsByCodigoCurso(curso.getCodigoCurso())) {
            throw new IllegalArgumentException(
                    "Ya existe un curso con el código: " + curso.getCodigoCurso());
        }
        curso.setActivo(true);
        CursoAcademico guardado = cursoAcademicoRepository.save(curso);
        log.info("Curso creado: '{}' ({})", guardado.getCodigoCurso(), guardado.getAnioAcademico());
        return guardado;
    }

    /**
     * Actualiza los datos de un curso existente. RF12
     */
    @Transactional
    public CursoAcademico actualizar(CursoAcademico curso) {
        CursoAcademico guardado = cursoAcademicoRepository.save(curso);
        log.info("Curso actualizado: '{}'", guardado.getCodigoCurso());
        return guardado;
    }

    /**
     * Activa un curso desactivado. RF12
     */
    @Transactional
    public void activar(Long id) {
        cursoAcademicoRepository.findById(id).ifPresent(curso -> {
            curso.setActivo(true);
            cursoAcademicoRepository.save(curso);
            log.info("Curso '{}' activado", curso.getCodigoCurso());
        });
    }

    /**
     * Desactiva un curso sin eliminar sus datos históricos. RF12
     * Los alumnos y periodos vinculados se conservan.
     */
    @Transactional
    public void desactivar(Long id) {
        cursoAcademicoRepository.findById(id).ifPresent(curso -> {
            curso.setActivo(false);
            cursoAcademicoRepository.save(curso);
            log.info("Curso '{}' desactivado", curso.getCodigoCurso());
        });
    }

    /**
     * Elimina un curso. RF12
     * Solo debe usarse si no tiene alumnos ni periodos vinculados.
     */
    @Transactional
    public void eliminar(Long id) {
        cursoAcademicoRepository.deleteById(id);
        log.info("Curso id={} eliminado", id);
    }


    //VALIDACIONES

    /**
     * Comprueba si ya existe un curso con ese código. RF12
     */
    public boolean existeCodigo(String codigoCurso) {
        return cursoAcademicoRepository.existsByCodigoCurso(codigoCurso);
    }


    //HELPERS PRIVADOS

    private CursoAcademico getOrThrow(Long id) {
        return cursoAcademicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Curso académico no encontrado con id: " + id));
    }
}

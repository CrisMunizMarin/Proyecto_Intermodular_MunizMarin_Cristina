package com.mentorcore.service;

import com.mentorcore.model.Alumno;
import com.mentorcore.model.Usuario;
import com.mentorcore.model.Valoracion;
import com.mentorcore.model.enums.ResultadoEnum;
import com.mentorcore.model.enums.TipoEvaluadorEnum;
import com.mentorcore.repository.ValoracionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de valoraciones finales del alumno.
 * Gestiona la emisión, actualización y bloqueo de evaluaciones.
 * RF7, RF14
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ValoracionService {

    private final ValoracionRepository valoracionRepository;


    // BÚSQUEDAS

    @Transactional(readOnly = true)
    public Optional<Valoracion> findById(Long id) {
        return valoracionRepository.findById(id);
    }

    /**
     * Devuelve todas las valoraciones de un alumno. RF7
     */
    @Transactional(readOnly = true)
    public List<Valoracion> findByAlumno(Alumno alumno) {
        return valoracionRepository.findByAlumno(alumno);
    }

    /**
     * Devuelve la valoración de un alumno para un tipo de evaluador concreto. RF7
     */
    @Transactional(readOnly = true)
    public Optional<Valoracion> findByAlumnoYTipo(Alumno alumno, TipoEvaluadorEnum tipoEvaluador) {
        return valoracionRepository.findByAlumnoAndTipoEvaluador(alumno, tipoEvaluador);
    }


    // CREACIÓN Y MODIFICACIÓN

    /**
     * Crea una nueva valoración para un alumno. RF7
     * Solo puede existir una valoración por alumno y tipo de evaluador.
     */
    @Transactional
    public Valoracion crear(Alumno alumno, Usuario evaluador, TipoEvaluadorEnum tipoEvaluador) {

        if (valoracionRepository.existsByAlumnoAndTipoEvaluador(alumno, tipoEvaluador)) {
            throw new IllegalStateException(
                    "Ya existe una valoración para el alumno '"
                            + alumno.getNombreUsuario() + "' y el tipo de evaluador "
                            + tipoEvaluador);
        }

        Valoracion valoracion = new Valoracion(alumno, evaluador, tipoEvaluador);
        valoracion.setResultado(ResultadoEnum.PENDIENTE);
        valoracion.setBloqueada(false);

        Valoracion guardada = valoracionRepository.save(valoracion);
        log.info("Valoración creada para alumno '{}' por tipo '{}'",
                alumno.getNombreUsuario(), tipoEvaluador);
        return guardada;
    }

    /**
     * Actualiza los datos de una valoración existente. RF7
     * No se puede modificar si está bloqueada.
     */
    @Transactional
    public Valoracion actualizar(Valoracion valoracion) {
        Valoracion existente = getOrThrow(valoracion.getId());

        if (existente.isBloqueada()) {
            throw new IllegalStateException(
                    "No se puede modificar una valoración bloqueada");
        }

        existente.setPuntacionActitud(valoracion.getPuntacionActitud());
        existente.setPuntacionCompetencias(valoracion.getPuntacionCompetencias());
        existente.setPuntacionIntegracion(valoracion.getPuntacionIntegracion());
        existente.setPuntacionIniciativa(valoracion.getPuntacionIniciativa());
        existente.setObservaciones(valoracion.getObservaciones());

        BigDecimal notaMedia = existente.calcularNotaMedia();
        existente.setNotaGlobal(notaMedia);

        Valoracion guardada = valoracionRepository.save(existente);
        log.info("Valoración id={} actualizada para alumno '{}'",
                guardada.getId(), guardada.getAlumno().getNombreUsuario());
        return guardada;
    }



    // EMISIÓN Y CIERRE

    /**
     * Emite una valoración completa con puntuaciones, observaciones y resultado. RF7
     * Si no se indica resultado, se calcula automáticamente según la nota media.
     */
    @Transactional
    public Valoracion emitir(Long idValoracion,
                             Integer actitud,
                             Integer competencias,
                             Integer integracion,
                             Integer iniciativa,
                             String observaciones,
                             ResultadoEnum resultado) {

        Valoracion valoracion = getOrThrow(idValoracion);

        if (valoracion.isBloqueada()) {
            throw new IllegalStateException(
                    "No se puede emitir una valoración ya bloqueada");
        }

        valoracion.setPuntacionActitud(actitud);
        valoracion.setPuntacionCompetencias(competencias);
        valoracion.setPuntacionIntegracion(integracion);
        valoracion.setPuntacionIniciativa(iniciativa);
        valoracion.setObservaciones(observaciones);

        BigDecimal notaMedia = valoracion.calcularNotaMedia();
        valoracion.setNotaGlobal(notaMedia);

        if (resultado != null) {
            valoracion.setResultado(resultado);
        } else {
            valoracion.setResultado(
                    notaMedia.compareTo(BigDecimal.valueOf(5)) >= 0
                            ? ResultadoEnum.APTO
                            : ResultadoEnum.NO_APTO
            );
        }

        Valoracion guardada = valoracionRepository.save(valoracion);
        log.info("Valoración id={} emitida para alumno '{}' con resultado '{}'",
                guardada.getId(),
                guardada.getAlumno().getNombreUsuario(),
                guardada.getResultado());
        return guardada;
    }

    /**
     * Bloquea una valoración para impedir cambios posteriores. RF7
     */
    @Transactional
    public void bloquear(Long idValoracion) {
        Valoracion valoracion = getOrThrow(idValoracion);

        if (valoracion.isBloqueada()) {
            throw new IllegalStateException(
                    "La valoración ya está bloqueada");
        }

        valoracion.bloquear();

        if (valoracion.getResultado() == null ||
                valoracion.getResultado() == ResultadoEnum.PENDIENTE) {

            valoracion.setResultado(
                    valoracion.getNotaGlobal().compareTo(BigDecimal.valueOf(5)) >= 0
                            ? ResultadoEnum.APTO
                            : ResultadoEnum.NO_APTO
            );
        }

        valoracionRepository.save(valoracion);

        log.info("Valoración id={} bloqueada para alumno '{}'",
                valoracion.getId(),
                valoracion.getAlumno().getNombreUsuario());
    }


    // VALIDACIONES

    /**
     * Comprueba si ya existe una valoración para ese alumno y tipo. RF7
     */
    @Transactional(readOnly = true)
    public boolean existeValoracion(Alumno alumno, TipoEvaluadorEnum tipoEvaluador) {
        return valoracionRepository.existsByAlumnoAndTipoEvaluador(alumno, tipoEvaluador);
    }


    // HELPERS PRIVADOS

    private Valoracion getOrThrow(Long id) {
        return valoracionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Valoración no encontrada con id: " + id));
    }
}



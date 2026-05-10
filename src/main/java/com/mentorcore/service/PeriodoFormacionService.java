package com.mentorcore.service;

import com.mentorcore.model.CursoAcademico;
import com.mentorcore.model.PeriodoFormacion;
import com.mentorcore.model.enums.EstadoPeriodoEnum;
import com.mentorcore.repository.PeriodoFormacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de periodos de formación.
 * RF20
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PeriodoFormacionService {

    private final PeriodoFormacionRepository periodoFormacionRepository;

    @Transactional(readOnly = true)
    public Optional<PeriodoFormacion> findById(Long id) {
        return periodoFormacionRepository.findDetalleById(id);
    }

    @Transactional(readOnly = true)
    public List<PeriodoFormacion> findAll() {
        return periodoFormacionRepository.findAllDetalle();
    }

    @Transactional(readOnly = true)
    public List<PeriodoFormacion> findByCurso(CursoAcademico curso) {
        return periodoFormacionRepository.findByCursoAcademico(curso);
    }

    @Transactional(readOnly = true)
    public List<PeriodoFormacion> findByEstado(EstadoPeriodoEnum estado) {
        return periodoFormacionRepository.findByEstado(estado);
    }

    @Transactional
    public PeriodoFormacion crear(PeriodoFormacion periodo) {
        validarFechas(periodo);

        List<PeriodoFormacion> solapados = periodoFormacionRepository.findSolapados(
                periodo.getCursoAcademico(),
                periodo.getFechaInicio(),
                periodo.getFechaFin(),
                -1L
        );

        if (!solapados.isEmpty()) {
            throw new IllegalStateException(
                    "Ya existe un periodo solapado para ese curso académico");
        }

        PeriodoFormacion guardado = periodoFormacionRepository.save(periodo);
        log.info("Periodo creado id={} para curso '{}'",
                guardado.getId(),
                guardado.getCursoAcademico().getCodigoCurso());
        return guardado;
    }

    @Transactional
    public PeriodoFormacion actualizar(PeriodoFormacion periodo) {
        validarFechas(periodo);

        List<PeriodoFormacion> solapados = periodoFormacionRepository.findSolapados(
                periodo.getCursoAcademico(),
                periodo.getFechaInicio(),
                periodo.getFechaFin(),
                periodo.getId()
        );

        if (!solapados.isEmpty()) {
            throw new IllegalStateException(
                    "Ya existe un periodo solapado para ese curso académico");
        }

        PeriodoFormacion guardado = periodoFormacionRepository.save(periodo);
        log.info("Periodo actualizado id={}", guardado.getId());
        return guardado;
    }

    @Transactional
    public void activar(Long id) {
        PeriodoFormacion periodo = getOrThrow(id);
        periodo.setEstado(EstadoPeriodoEnum.ACTIVO);
        periodoFormacionRepository.save(periodo);
        log.info("Periodo id={} activado", id);
    }

    @Transactional
    public void cerrar(Long id) {
        PeriodoFormacion periodo = getOrThrow(id);
        periodo.setEstado(EstadoPeriodoEnum.CERRADO);
        periodoFormacionRepository.save(periodo);
        log.info("Periodo id={} cerrado", id);
    }

    private PeriodoFormacion getOrThrow(Long id) {
        return periodoFormacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Periodo de formación no encontrado con id: " + id));
    }

    private void validarFechas(PeriodoFormacion periodo) {
        if (periodo.getFechaInicio() == null || periodo.getFechaFin() == null) {
            throw new IllegalArgumentException("Debes indicar fecha de inicio y de fin.");
        }

        if (periodo.getFechaFin().isBefore(periodo.getFechaInicio())) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }
    }
}

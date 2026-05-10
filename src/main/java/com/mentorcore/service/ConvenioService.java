package com.mentorcore.service;

import com.mentorcore.model.Alumno;
import com.mentorcore.model.Convenio;
import com.mentorcore.model.enums.EstadoConvenioEnum;
import com.mentorcore.repository.ConvenioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de convenios de formación en empresa.
 * Gestiona el ciclo de vida: BORRADOR → FIRMADO → VIGENTE → FINALIZADO.
 * RF3, RF6
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConvenioService {

    private final ConvenioRepository convenioRepository;


    // ── BÚSQUEDAS ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Optional<Convenio> findById(Long id) {
        return convenioRepository.findDetalleById(id);
    }

    /**
     * Devuelve todos los convenios de un alumno (historial). RF3
     */
    @Transactional(readOnly = true)
    public List<Convenio> findByAlumno(Alumno alumno) {
        return convenioRepository.findByAlumno(alumno);
    }

    /**
     * Devuelve el convenio VIGENTE de un alumno. RF3
     */
    @Transactional(readOnly = true)
    public Optional<Convenio> findVigenteByAlumno(Alumno alumno) {
        return convenioRepository.findByAlumnoAndEstado(
                alumno, EstadoConvenioEnum.VIGENTE);
    }

    /**
     * Devuelve el convenio FIRMADO de un alumno. RF3
     */
    @Transactional(readOnly = true)
    public Optional<Convenio> findFirmadoByAlumno(Alumno alumno) {
        return convenioRepository.findByAlumnoAndEstado(
                alumno, EstadoConvenioEnum.FIRMADO);
    }


    // ── CREACIÓN Y MODIFICACIÓN ───────────────────────────────────────────────

    /**
     * Crea un nuevo convenio en estado BORRADOR. RF3
     * Verifica que el número de convenio no esté duplicado.
     */
    @Transactional
    public Convenio crear(Convenio convenio) {
        if (convenioRepository.existsByNumeroConvenio(convenio.getNumeroConvenio())) {
            throw new IllegalArgumentException(
                    "Ya existe un convenio con el número: "
                    + convenio.getNumeroConvenio());
        }
        convenio.setEstado(EstadoConvenioEnum.BORRADOR);
        Convenio guardado = convenioRepository.save(convenio);
        log.info("Convenio '{}' creado en estado BORRADOR para alumno id={}",
                guardado.getNumeroConvenio(), guardado.getAlumno().getId());
        return guardado;
    }

    /**
     * Actualiza los datos de un convenio en estado BORRADOR. RF3
     * No se puede modificar un convenio ya firmado o vigente.
     */
    @Transactional
    public Convenio actualizar(Convenio convenio) {
        if (convenio.getEstado() != EstadoConvenioEnum.BORRADOR) {
            throw new IllegalStateException(
                    "Solo se puede editar un convenio en estado BORRADOR. " +
                    "Estado actual: " + convenio.getEstado());
        }
        Convenio guardado = convenioRepository.save(convenio);
        log.info("Convenio '{}' actualizado", guardado.getNumeroConvenio());
        return guardado;
    }


    // ── CICLO DE VIDA ─────────────────────────────────────────────────────────

    /**
     * Firma el convenio: BORRADOR → FIRMADO. RF3
     * Registra la fecha de firma.
     */
    @Transactional
    public void firmar(Long idConvenio) {
        Convenio convenio = getOrThrow(idConvenio);

        if (convenio.getEstado() != EstadoConvenioEnum.BORRADOR) {
            throw new IllegalStateException(
                    "Solo se puede firmar un convenio en BORRADOR. " +
                    "Estado actual: " + convenio.getEstado());
        }
        convenio.setEstado(EstadoConvenioEnum.FIRMADO);
        convenio.setFechaFirma(LocalDate.now());
        convenioRepository.save(convenio);
        log.info("Convenio '{}' firmado el {}", convenio.getNumeroConvenio(), LocalDate.now());
    }

    /**
     * Activa el convenio: FIRMADO → VIGENTE. RF3
     * Se llama cuando comienzan las prácticas.
     */
    @Transactional
    public void activar(Long idConvenio) {
        Convenio convenio = getOrThrow(idConvenio);

        if (convenio.getEstado() != EstadoConvenioEnum.FIRMADO) {
            throw new IllegalStateException(
                    "Solo se puede activar un convenio FIRMADO. " +
                    "Estado actual: " + convenio.getEstado());
        }
        convenio.setEstado(EstadoConvenioEnum.VIGENTE);
        convenioRepository.save(convenio);
        log.info("Convenio '{}' activado → VIGENTE", convenio.getNumeroConvenio());
    }

    /**
     * Finaliza el convenio: VIGENTE → FINALIZADO. RF3
     * Se llama al terminar el periodo de prácticas.
     */
    @Transactional
    public void finalizar(Long idConvenio) {
        Convenio convenio = getOrThrow(idConvenio);

        if (convenio.getEstado() != EstadoConvenioEnum.VIGENTE) {
            throw new IllegalStateException(
                    "Solo se puede finalizar un convenio VIGENTE. " +
                    "Estado actual: " + convenio.getEstado());
        }
        convenio.setEstado(EstadoConvenioEnum.FINALIZADO);
        convenioRepository.save(convenio);
        log.info("Convenio '{}' finalizado", convenio.getNumeroConvenio());
    }

    /**
     * Anula el convenio desde cualquier estado activo. RF3
     */
    @Transactional
    public void anular(Long idConvenio, String motivo) {
        Convenio convenio = getOrThrow(idConvenio);

        if (convenio.getEstado() == EstadoConvenioEnum.FINALIZADO ||
            convenio.getEstado() == EstadoConvenioEnum.ANULADO) {
            throw new IllegalStateException(
                    "No se puede anular un convenio ya FINALIZADO o ANULADO");
        }
        convenio.setEstado(EstadoConvenioEnum.ANULADO);
        convenioRepository.save(convenio);
        log.warn("Convenio '{}' ANULADO. Motivo: {}",
                convenio.getNumeroConvenio(), motivo);
    }

    /**
     * Guarda la URL del PDF firmado en el servidor. RF6
     */
    @Transactional
    public void guardarPdf(Long idConvenio, String pdfUrl) {
        Convenio convenio = getOrThrow(idConvenio);
        convenio.setArchivoPdfUrl(pdfUrl);
        convenioRepository.save(convenio);
        log.info("PDF del convenio '{}' almacenado en: {}",
                convenio.getNumeroConvenio(), pdfUrl);
    }


    // ── VALIDACIONES ──────────────────────────────────────────────────────────

    /**
     * Comprueba si ya existe un convenio con ese número. RF3
     */
    public boolean existeNumeroConvenio(String numeroConvenio) {
        return convenioRepository.existsByNumeroConvenio(numeroConvenio);
    }


    // ── HELPERS PRIVADOS ──────────────────────────────────────────────────────

    private Convenio getOrThrow(Long id) {
        return convenioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Convenio no encontrado con id: " + id));
    }
}

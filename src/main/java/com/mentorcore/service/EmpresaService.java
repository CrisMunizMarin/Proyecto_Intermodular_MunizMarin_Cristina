package com.mentorcore.service;

import com.mentorcore.model.Empresa;
import com.mentorcore.model.enums.EstadoEmpresaEnum;
import com.mentorcore.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de empresas colaboradoras.
 * RF18, RF19
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmpresaService {

    private final EmpresaRepository empresaRepository;


    //BÚSQUEDAS

    @Transactional(readOnly = true)
    public Optional<Empresa> findById(Long id) {
        return empresaRepository.findById(id);
    }

    /**
     * Devuelve todas las empresas del sistema. RF18
     */
    @Transactional(readOnly = true)
    public List<Empresa> findAll() {
        return empresaRepository.findAll();
    }

    /**
     * Devuelve solo las empresas activas ordenadas por nombre. RF18
     */
    @Transactional(readOnly = true)
    public List<Empresa> findActivas() {
        return empresaRepository.findByEstadoOrderByNombreAsc(EstadoEmpresaEnum.ACTIVA);
    }

    /**
     * Busca una empresa por su CIF. RF18
     */
    @Transactional(readOnly = true)
    public Optional<Empresa> findByCif(String cif) {
        return empresaRepository.findByCif(cif);
    }

    /**
     * Devuelve empresas filtradas por estado. RF18
     */
    @Transactional(readOnly = true)
    public List<Empresa> findByEstado(EstadoEmpresaEnum estado) {
        return empresaRepository.findByEstado(estado);
    }

    /**
     * Búsqueda libre por nombre o municipio. RF15
     */
    @Transactional(readOnly = true)
    public List<Empresa> buscar(String texto) {
        if (texto == null || texto.isBlank()) {
            return findActivas();
        }
        return empresaRepository.buscarPorNombreOMunicipio(texto.trim());
    }


    //CREACIÓN Y MODIFICACIÓN

    /**
     * Crea una nueva empresa colaboradora. RF18
     * Verifica que el CIF no esté duplicado.
     */
    @Transactional
    public Empresa crear(Empresa empresa) {
        if (empresaRepository.existsByCif(empresa.getCif())) {
            throw new IllegalArgumentException(
                    "Ya existe una empresa con el CIF: " + empresa.getCif());
        }
        empresa.setEstado(EstadoEmpresaEnum.ACTIVA);
        Empresa guardada = empresaRepository.save(empresa);
        log.info("Empresa creada: '{}' (CIF: {})",
                guardada.getNombre(), guardada.getCif());
        return guardada;
    }

    /**
     * Actualiza los datos de una empresa existente. RF18
     */
    @Transactional
    public Empresa actualizar(Empresa empresa) {
        Empresa existente = getOrThrow(empresa.getId());

        empresaRepository.findByCif(empresa.getCif())
                .filter(otra -> !otra.getId().equals(empresa.getId()))
                .ifPresent(otra -> {
                    throw new IllegalArgumentException(
                            "Ya existe otra empresa con el CIF: " + empresa.getCif());
                });

        existente.setNombre(empresa.getNombre());
        existente.setCif(empresa.getCif());
        existente.setSector(empresa.getSector());
        existente.setDireccion(empresa.getDireccion());
        existente.setMunicipio(empresa.getMunicipio());
        existente.setProvincia(empresa.getProvincia());
        existente.setCodigoPostal(empresa.getCodigoPostal());
        existente.setTelefono(empresa.getTelefono());
        existente.setEmailContacto(empresa.getEmailContacto());
        existente.setWeb(empresa.getWeb());
        existente.setNotas(empresa.getNotas());

        Empresa guardada = empresaRepository.save(existente);
        log.info("Empresa actualizada: '{}' (id={})",
                guardada.getNombre(), guardada.getId());
        return guardada;
    }

    /**
     * Desactiva una empresa sin eliminarla. RF18
     * No se puede desactivar si tiene alumnos activos asignados.
     * Esa validación se hace en el controlador consultando AsignacionService.
     */
    @Transactional
    public void desactivar(Long id) {
        empresaRepository.findById(id).ifPresent(empresa -> {
            empresa.setEstado(EstadoEmpresaEnum.INACTIVA);
            empresaRepository.save(empresa);
            log.info("Empresa '{}' desactivada", empresa.getNombre());
        });
    }

    /**
     * Reactiva una empresa inactiva. RF18
     */
    @Transactional
    public void reactivar(Long id) {
        empresaRepository.findById(id).ifPresent(empresa -> {
            empresa.setEstado(EstadoEmpresaEnum.ACTIVA);
            empresaRepository.save(empresa);
            log.info("Empresa '{}' reactivada", empresa.getNombre());
        });
    }

    /**
     * Elimina una empresa del sistema. RF18
     * Solo debe usarse si no tiene alumnos ni tutores vinculados.
     * La validación previa se realiza en el controlador.
     */
    @Transactional
    public void eliminar(Long id) {
        empresaRepository.deleteById(id);
        log.info("Empresa id={} eliminada", id);
    }


    //VALIDACIONES

    /**
     * Comprueba si ya existe una empresa con ese CIF. RF18
     */
    public boolean existeCif(String cif) {
        return empresaRepository.existsByCif(cif);
    }


    //HELPERS PRIVADOS

    private Empresa getOrThrow(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Empresa no encontrada con id: " + id));
    }
}

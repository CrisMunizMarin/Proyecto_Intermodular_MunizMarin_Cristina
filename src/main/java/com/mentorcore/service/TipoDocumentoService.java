package com.mentorcore.service;

import com.mentorcore.model.TipoDocumento;
import com.mentorcore.repository.TipoDocumentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de tipos de documento.
 * Permite consultar el catálogo configurable de documentos del sistema.
 * RF12
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TipoDocumentoService {

    private final TipoDocumentoRepository tipoDocumentoRepository;


    // BÚSQUEDAS

    @Transactional(readOnly = true)
    public Optional<TipoDocumento> findById(Long id) {
        return tipoDocumentoRepository.findById(id);
    }

    /**
     * Devuelve todos los tipos activos. RF12
     */
    @Transactional(readOnly = true)
    public List<TipoDocumento> findActivos() {
        return tipoDocumentoRepository.findByActivoTrue();
    }

    /**
     * Devuelve los tipos obligatorios activos. RF12
     */
    @Transactional(readOnly = true)
    public List<TipoDocumento> findObligatoriosActivos() {
        return tipoDocumentoRepository.findByEsObligatorioAndActivoTrue(true);
    }
    
    /**
     * Busca un tipo de documento activo por nombre. RF12
     */
    @Transactional(readOnly = true)
    public Optional<TipoDocumento> findByNombreActivo(String nombre) {
        return tipoDocumentoRepository.findByNombreAndActivoTrue(nombre);
    }

    @Transactional(readOnly = true)
    public List<TipoDocumento> findActivosPorRoles(String... rolesResponsables) {
        return tipoDocumentoRepository.findByActivoTrueAndRolResponsableIn(Arrays.asList(rolesResponsables));
    }

    @Transactional(readOnly = true)
    public List<TipoDocumento> findActivosPorNombres(String... nombres) {
        return tipoDocumentoRepository.findByActivoTrueAndNombreIn(Arrays.asList(nombres));
    }

}

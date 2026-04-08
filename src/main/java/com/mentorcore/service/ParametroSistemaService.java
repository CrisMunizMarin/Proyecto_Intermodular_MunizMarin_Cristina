package com.mentorcore.service;

import com.mentorcore.model.ParametroSistema;
import com.mentorcore.model.Usuario;
import com.mentorcore.repository.ParametroSistemaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de parámetros globales del sistema.
 * Permite consultar y modificar configuración sin recompilar la aplicación.
 * RF12
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ParametroSistemaService {

    private final ParametroSistemaRepository parametroSistemaRepository;


    // BÚSQUEDAS

    @Transactional(readOnly = true)
    public Optional<ParametroSistema> findById(Long id) {
        return parametroSistemaRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<ParametroSistema> findAll() {
        return parametroSistemaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<ParametroSistema> findByClave(String clave) {
        return parametroSistemaRepository.findByClave(clave);
    }


    // CREACIÓN Y MODIFICACIÓN

    @Transactional
    public ParametroSistema crear(ParametroSistema parametro) {
        if (parametroSistemaRepository.existsByClave(parametro.getClave())) {
            throw new IllegalArgumentException(
                    "Ya existe un parámetro con la clave: " + parametro.getClave());
        }

        ParametroSistema guardado = parametroSistemaRepository.save(parametro);
        log.info("Parámetro del sistema creado: clave='{}'",
                guardado.getClave());
        return guardado;
    }

    /**
     * Actualiza un parámetro existente y registra quién lo modificó.
     */
    @Transactional
    public ParametroSistema actualizar(ParametroSistema parametro, Usuario admin) {
        ParametroSistema existente = getOrThrow(parametro.getId());

        if (!existente.getClave().equals(parametro.getClave())
                && parametroSistemaRepository.existsByClave(parametro.getClave())) {
            throw new IllegalArgumentException(
                    "Ya existe un parámetro con la clave: " + parametro.getClave());
        }

        existente.setClave(parametro.getClave());
        existente.setDescripcion(parametro.getDescripcion());
        existente.setTipoDato(parametro.getTipoDato());
        existente.actualizar(parametro.getValor(), admin);

        ParametroSistema guardado = parametroSistemaRepository.save(existente);
        log.info("Parámetro del sistema actualizado: clave='{}' por '{}'",
                guardado.getClave(),
                admin != null ? admin.getNombreUsuario() : "sistema");
        return guardado;
    }

    @Transactional
    public ParametroSistema actualizarValor(String clave, String nuevoValor, Usuario admin) {
        ParametroSistema parametro = parametroSistemaRepository.findByClave(clave)
                .orElseThrow(() -> new RuntimeException(
                        "Parámetro del sistema no encontrado con clave: " + clave));

        parametro.actualizar(nuevoValor, admin);

        ParametroSistema guardado = parametroSistemaRepository.save(parametro);
        log.info("Valor del parámetro '{}' actualizado por '{}'",
                guardado.getClave(),
                admin != null ? admin.getNombreUsuario() : "sistema");
        return guardado;
    }


    // LECTURA TIPADA

    @Transactional(readOnly = true)
    public String getValor(String clave) {
        return parametroSistemaRepository.findByClave(clave)
                .map(ParametroSistema::getValor)
                .orElseThrow(() -> new RuntimeException(
                        "Parámetro del sistema no encontrado con clave: " + clave));
    }

    @Transactional(readOnly = true)
    public int getValorAsInt(String clave) {
        return parametroSistemaRepository.findByClave(clave)
                .map(ParametroSistema::getValorAsInt)
                .orElseThrow(() -> new RuntimeException(
                        "Parámetro del sistema no encontrado con clave: " + clave));
    }

    @Transactional(readOnly = true)
    public boolean getValorAsBoolean(String clave) {
        return parametroSistemaRepository.findByClave(clave)
                .map(ParametroSistema::getValorAsBoolean)
                .orElseThrow(() -> new RuntimeException(
                        "Parámetro del sistema no encontrado con clave: " + clave));
    }


    // ELIMINACIÓN

    @Transactional
    public void eliminar(Long id) {
        ParametroSistema parametro = getOrThrow(id);
        parametroSistemaRepository.deleteById(id);
        log.info("Parámetro del sistema eliminado: clave='{}'",
                parametro.getClave());
    }


    // VALIDACIONES

    @Transactional(readOnly = true)
    public boolean existeClave(String clave) {
        return parametroSistemaRepository.existsByClave(clave);
    }


    // HELPERS PRIVADOS

    private ParametroSistema getOrThrow(Long id) {
        return parametroSistemaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Parámetro del sistema no encontrado con id: " + id));
    }
}



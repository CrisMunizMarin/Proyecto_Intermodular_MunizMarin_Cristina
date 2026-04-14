package com.mentorcore.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Utilidad para guardar y eliminar archivos subidos al servidor.
 * RF3, RF22
 */
@Component
@Slf4j
public class FileUploadUtil {

    /**
     * Guarda un archivo dentro de la ruta base indicada y devuelve la ruta final.
     *
     * @param rutaBase   carpeta base configurada en application.properties
     * @param subcarpeta subcarpeta lógica, por ejemplo "alumnos/1/documentos"
     * @param archivo    archivo recibido por el formulario
     * @return ruta relativa o física donde se ha guardado
     */
    public String guardarArchivo(String rutaBase, String subcarpeta, MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        try {
            String nombreOriginal = archivo.getOriginalFilename();
            String extension = obtenerExtension(nombreOriginal);
            String nombreSeguro = UUID.randomUUID() + extension;

            Path directorio = Paths.get(rutaBase, subcarpeta).normalize();
            Files.createDirectories(directorio);

            Path rutaDestino = directorio.resolve(nombreSeguro);
            Files.copy(archivo.getInputStream(), rutaDestino, StandardCopyOption.REPLACE_EXISTING);

            log.info("Archivo guardado en {}", rutaDestino);
            return rutaDestino.toString();

        } catch (IOException e) {
            throw new RuntimeException("Error guardando el archivo en disco", e);
        }
    }

    /**
     * Elimina un archivo del disco si existe.
     */
    public void eliminarArchivo(String rutaArchivo) {
        if (rutaArchivo == null || rutaArchivo.isBlank()) {
            return;
        }

        try {
            Path ruta = Paths.get(rutaArchivo).normalize();
            Files.deleteIfExists(ruta);
            log.info("Archivo eliminado: {}", rutaArchivo);
        } catch (IOException e) {
            throw new RuntimeException("Error eliminando el archivo del disco", e);
        }
    }

    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) {
            return "";
        }
        return nombreArchivo.substring(nombreArchivo.lastIndexOf("."));
    }
}


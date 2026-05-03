package com.mentorcore.repository;

import com.mentorcore.model.Empresa;
import com.mentorcore.model.TutorEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad TutorEmpresa.
 * RF6, RF7, RF9, RF13, RF19
 */
@Repository
public interface TutorEmpresaRepository extends JpaRepository<TutorEmpresa, Long> {

    // findById(Long id) ya viene en JpaRepository

    // Tutores de una empresa concreta (RF18)
    List<TutorEmpresa> findByEmpresa(Empresa empresa);
    List<TutorEmpresa> findByEmpresa_Id(Long idEmpresa);

    // Buscar tutor empresa por email
    Optional<TutorEmpresa> findByEmail(String email);

    @Query("SELECT t FROM TutorEmpresa t " +
           "LEFT JOIN FETCH t.empresa " +
           "WHERE t.nombreUsuario = :nombreUsuario")
    Optional<TutorEmpresa> findByNombreUsuario(@Param("nombreUsuario") String nombreUsuario);

    // Búsqueda por nombre o apellidos (RF15)
    @Query("SELECT t FROM TutorEmpresa t WHERE " +
           "LOWER(t.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(t.apellidos) LIKE LOWER(CONCAT('%', :texto, '%'))")
    List<TutorEmpresa> buscarPorNombre(@Param("texto") String texto);
}

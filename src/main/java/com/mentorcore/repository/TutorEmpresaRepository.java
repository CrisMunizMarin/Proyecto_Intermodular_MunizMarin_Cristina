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

    // Buscar por usuario base (RF1)
    Optional<TutorEmpresa> findByIdUsuario(Long idUsuario);

    // Tutores de una empresa concreta (RF18)
    List<TutorEmpresa> findByEmpresa(Empresa empresa);
    List<TutorEmpresa> findByEmpresa_Id(Long idEmpresa);

    // Buscar por nombre o apellidos (RF15)
    @Query("SELECT t FROM TutorEmpresa t WHERE " +
           "LOWER(t.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(t.apellidos) LIKE LOWER(CONCAT('%', :texto, '%'))")
    List<TutorEmpresa> buscarPorNombre(@Param("texto") String texto);
}

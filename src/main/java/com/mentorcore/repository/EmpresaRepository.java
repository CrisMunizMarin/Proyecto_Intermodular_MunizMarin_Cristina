package com.mentorcore.repository;

import com.mentorcore.model.Empresa;
import com.mentorcore.model.enums.EstadoEmpresaEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad Empresa.
 * RF18
 */
@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    // Buscar por CIF único (RF18)
    Optional<Empresa> findByCif(String cif);

    // Comprobar duplicado al crear empresa (RF18)
    boolean existsByCif(String cif);

    // Listar por estado (RF18)
    List<Empresa> findByEstado(EstadoEmpresaEnum estado);

    // Empresas activas ordenadas por nombre (RF18)
    List<Empresa> findByEstadoOrderByNombreAsc(EstadoEmpresaEnum estado);

    // Búsqueda por nombre o municipio (RF15)
    @Query("SELECT e FROM Empresa e WHERE " +
           "LOWER(e.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(e.municipio) LIKE LOWER(CONCAT('%', :texto, '%'))")
    List<Empresa> buscarPorNombreOMunicipio(@Param("texto") String texto);
}

package br.com.estudofacil.estudo_facil_api.repository;

import br.com.estudofacil.estudo_facil_api.entity.Materia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MateriaRepository extends JpaRepository<Materia, Long> {

    Page<Materia> findByUsuarioId(Long usuarioId, Pageable pageable);

    Page<Materia> findByUsuarioIdAndSemestre(Long usuarioId, String semestre, Pageable pageable);

    Optional<Materia> findByIdAndUsuarioId(Long id, Long usuarioId);

    long countByUsuarioId(Long usuarioId);
}

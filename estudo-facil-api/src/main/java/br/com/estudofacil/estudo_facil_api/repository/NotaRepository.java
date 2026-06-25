package br.com.estudofacil.estudo_facil_api.repository;

import br.com.estudofacil.estudo_facil_api.entity.Nota;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotaRepository extends JpaRepository<Nota, Long> {

    @EntityGraph(attributePaths = "materia")
    Page<Nota> findByMateriaId(Long materiaId, Pageable pageable);

    @EntityGraph(attributePaths = "materia")
    Optional<Nota> findByIdAndMateriaUsuarioId(Long id, Long usuarioId);

    @EntityGraph(attributePaths = "materia")
    @Query("SELECT n FROM Nota n WHERE n.materia.usuario.id = :usuarioId")
    Page<Nota> findByUsuarioId(@Param("usuarioId") Long usuarioId, Pageable pageable);

    long countByMateriaId(Long materiaId);

    @Query("SELECT COALESCE(SUM(n.peso), 0) FROM Nota n WHERE n.materia.id = :materiaId")
    Double somarPesoPorMateria(@Param("materiaId") Long materiaId);

    @Query("SELECT COALESCE(SUM(n.peso), 0) FROM Nota n WHERE n.materia.id = :materiaId AND n.id <> :notaId")
    Double somarPesoPorMateriaExcluindo(@Param("materiaId") Long materiaId, @Param("notaId") Long notaId);

    @Query("SELECT SUM(n.valor * n.peso) / NULLIF(SUM(n.peso), 0) FROM Nota n WHERE n.materia.id = :materiaId")
    Double calcularMediaPonderadaPorMateria(@Param("materiaId") Long materiaId);

    @Query("SELECT SUM(n.valor * n.peso) / NULLIF(SUM(n.peso), 0) FROM Nota n WHERE n.materia.usuario.id = :usuarioId")
    Double calcularMediaPonderadaPorUsuario(@Param("usuarioId") Long usuarioId);
}

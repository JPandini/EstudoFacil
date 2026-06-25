package br.com.estudofacil.estudo_facil_api.repository;

import br.com.estudofacil.estudo_facil_api.entity.Tarefa;
import br.com.estudofacil.estudo_facil_api.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    @EntityGraph(attributePaths = "materia")
    Page<Tarefa> findByMateriaId(Long materiaId, Pageable pageable);

    @EntityGraph(attributePaths = "materia")
    Page<Tarefa> findByMateriaIdAndStatus(Long materiaId, TaskStatus status, Pageable pageable);

    @EntityGraph(attributePaths = "materia")
    @Query("SELECT t FROM Tarefa t WHERE t.materia.usuario.id = :usuarioId")
    Page<Tarefa> findByUsuarioId(@Param("usuarioId") Long usuarioId, Pageable pageable);

    @EntityGraph(attributePaths = "materia")
    @Query("SELECT t FROM Tarefa t WHERE t.materia.usuario.id = :usuarioId AND t.status = :status")
    Page<Tarefa> findByUsuarioIdAndStatus(@Param("usuarioId") Long usuarioId, @Param("status") TaskStatus status, Pageable pageable);

    @Query("SELECT COUNT(t) FROM Tarefa t WHERE t.materia.usuario.id = :usuarioId AND t.status = :status")
    long countByUsuarioIdAndStatus(@Param("usuarioId") Long usuarioId, @Param("status") TaskStatus status);

    @EntityGraph(attributePaths = "materia")
    @Query("SELECT t FROM Tarefa t WHERE t.materia.usuario.id = :usuarioId AND t.status <> :status " +
           "ORDER BY CASE WHEN t.dataEntrega IS NULL THEN 1 ELSE 0 END, t.dataEntrega ASC")
    List<Tarefa> findProximasTarefas(@Param("usuarioId") Long usuarioId,
                                     @Param("status") TaskStatus status,
                                     Pageable pageable);
}

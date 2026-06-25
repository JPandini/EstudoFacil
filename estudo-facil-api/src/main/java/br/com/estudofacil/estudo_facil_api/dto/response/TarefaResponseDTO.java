package br.com.estudofacil.estudo_facil_api.dto.response;

import br.com.estudofacil.estudo_facil_api.entity.Tarefa;
import br.com.estudofacil.estudo_facil_api.enums.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TarefaResponseDTO(
        Long id,
        String titulo,
        String descricao,
        LocalDate dataEntrega,
        TaskStatus status,
        Long materiaId,
        String nomeMateria,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
    public static TarefaResponseDTO from(Tarefa tarefa) {
        return new TarefaResponseDTO(
                tarefa.getId(),
                tarefa.getTitulo(),
                tarefa.getDescricao(),
                tarefa.getDataEntrega(),
                tarefa.getStatus(),
                tarefa.getMateria().getId(),
                tarefa.getMateria().getNome(),
                tarefa.getCriadoEm(),
                tarefa.getAtualizadoEm()
        );
    }
}

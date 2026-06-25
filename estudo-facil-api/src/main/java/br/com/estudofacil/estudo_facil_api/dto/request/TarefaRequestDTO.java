package br.com.estudofacil.estudo_facil_api.dto.request;

import br.com.estudofacil.estudo_facil_api.enums.TaskStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TarefaRequestDTO(

        @NotBlank(message = "Título da tarefa é obrigatório")
        @Size(max = 200, message = "Título deve ter no máximo 200 caracteres")
        String titulo,

        String descricao,

        @FutureOrPresent(message = "Data de entrega não pode ser no passado")
        LocalDate dataEntrega,

        TaskStatus status,

        @NotNull(message = "ID da matéria é obrigatório")
        Long materiaId
) {}

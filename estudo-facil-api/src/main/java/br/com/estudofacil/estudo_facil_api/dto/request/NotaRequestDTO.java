package br.com.estudofacil.estudo_facil_api.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record NotaRequestDTO(

        @NotNull(message = "Valor da nota é obrigatório")
        @DecimalMin(value = "0.0", message = "Nota deve ser no mínimo 0")
        @DecimalMax(value = "10.0", message = "Nota deve ser no máximo 10")
        Double valor,

        @NotNull(message = "Peso da nota é obrigatório")
        @Positive(message = "Peso deve ser positivo")
        Double peso,

        @Size(max = 500, message = "Observação deve ter no máximo 500 caracteres")
        String observacao,

        @NotNull(message = "ID da matéria é obrigatório")
        Long materiaId
) {}

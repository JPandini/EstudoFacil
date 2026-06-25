package br.com.estudofacil.estudo_facil_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MateriaRequestDTO(

        @NotBlank(message = "Nome da matéria é obrigatório")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        String nome,

        @Size(max = 100, message = "Professor deve ter no máximo 100 caracteres")
        String professor,

        @Pattern(regexp = "^$|^\\d{4}-0?[12]$", message = "Semestre deve estar no formato AAAA-1 ou AAAA-2 (ex: 2026-1 ou 2026-01)")
        String semestre,

        String descricao
) {}

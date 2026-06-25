package br.com.estudofacil.estudo_facil_api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ExcluirContaRequestDTO(
        @NotBlank(message = "Senha atual é obrigatória")
        String senhaAtual
) {}

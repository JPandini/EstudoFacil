package br.com.estudofacil.estudo_facil_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AtualizarPerfilRequestDTO(
        @Size(max = 100) String nome,
        @Email @Size(max = 255) String email,
        String senhaAtual,
        @Size(min = 6, max = 255) String novaSenha
) {}

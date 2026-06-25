package br.com.estudofacil.estudo_facil_api.dto.response;

import br.com.estudofacil.estudo_facil_api.entity.Nota;

import java.time.LocalDateTime;

public record NotaResponseDTO(
        Long id,
        Double valor,
        Double peso,
        String observacao,
        Long materiaId,
        String nomeMateria,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
    public static NotaResponseDTO from(Nota nota) {
        return new NotaResponseDTO(
                nota.getId(),
                nota.getValor(),
                nota.getPeso(),
                nota.getObservacao(),
                nota.getMateria().getId(),
                nota.getMateria().getNome(),
                nota.getCriadoEm(),
                nota.getAtualizadoEm()
        );
    }
}

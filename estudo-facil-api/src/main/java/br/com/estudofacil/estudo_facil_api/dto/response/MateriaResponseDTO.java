package br.com.estudofacil.estudo_facil_api.dto.response;

import br.com.estudofacil.estudo_facil_api.entity.Materia;

import java.time.LocalDateTime;

public record MateriaResponseDTO(
        Long id,
        String nome,
        String professor,
        String semestre,
        String descricao,
        Long usuarioId,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
    public static MateriaResponseDTO from(Materia materia) {
        return new MateriaResponseDTO(
                materia.getId(),
                materia.getNome(),
                materia.getProfessor(),
                materia.getSemestre(),
                materia.getDescricao(),
                materia.getUsuario().getId(),
                materia.getCriadoEm(),
                materia.getAtualizadoEm()
        );
    }
}

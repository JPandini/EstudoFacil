package br.com.estudofacil.estudo_facil_api.dto.response;

public record MateriaResumoMediaDTO(
        Long materiaId,
        String nome,
        Double media
) {}

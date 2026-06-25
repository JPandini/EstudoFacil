package br.com.estudofacil.estudo_facil_api.dto.response;

public record MateriaMediaResponseDTO(
        Long materiaId,
        String nomeMateria,
        Double mediaPonderada,
        long totalNotas
) {}

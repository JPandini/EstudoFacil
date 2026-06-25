package br.com.estudofacil.estudo_facil_api.dto.response;

import java.util.List;

public record DashboardResponseDTO(
        long totalMaterias,
        long tarefasPendentes,
        long tarefasConcluidas,
        long tarefasEmAndamento,
        Double mediaGeral,
        List<TarefaResponseDTO> proximasTarefas,
        List<MateriaResumoMediaDTO> materiasComMedia
) {}

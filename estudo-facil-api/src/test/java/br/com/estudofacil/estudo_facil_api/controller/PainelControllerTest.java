package br.com.estudofacil.estudo_facil_api.controller;

import br.com.estudofacil.estudo_facil_api.AbstractIntegrationTest;
import br.com.estudofacil.estudo_facil_api.dto.request.MateriaRequestDTO;
import br.com.estudofacil.estudo_facil_api.dto.request.NotaRequestDTO;
import br.com.estudofacil.estudo_facil_api.dto.request.TarefaRequestDTO;
import br.com.estudofacil.estudo_facil_api.enums.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PainelControllerTest extends AbstractIntegrationTest {

    @Test
    void buscarPainel_deveRetornarDadosAgregados() throws Exception {
        String token = registrarEObterToken("Painel User", "painel@test.com", "senha123");

        Long materiaId = criarMateriaViaApi(token, "Programação");

        TarefaRequestDTO tarefaPendente = new TarefaRequestDTO(
                "Trabalho", null, null, TaskStatus.PENDENTE, materiaId);
        TarefaRequestDTO tarefaConcluida = new TarefaRequestDTO(
                "Lista", null, null, TaskStatus.CONCLUIDA, materiaId);

        mockMvc.perform(post("/tarefas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tarefaPendente)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/tarefas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tarefaConcluida)))
                .andExpect(status().isCreated());

        NotaRequestDTO nota = new NotaRequestDTO(8.0, 5.0, "Prova", materiaId);
        mockMvc.perform(post("/notas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nota)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/painel")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMaterias").value(1))
                .andExpect(jsonPath("$.tarefasPendentes").value(1))
                .andExpect(jsonPath("$.tarefasConcluidas").value(1))
                .andExpect(jsonPath("$.mediaGeral").value(8.0))
                .andExpect(jsonPath("$.materiasComMedia").isArray())
                .andExpect(jsonPath("$.proximasTarefas").isArray());
    }

    @Test
    void buscarPainel_semToken_deveRetornar401() throws Exception {
        mockMvc.perform(get("/painel"))
                .andExpect(status().isUnauthorized());
    }

    private Long criarMateriaViaApi(String token, String nome) throws Exception {
        MateriaRequestDTO materiaDto = new MateriaRequestDTO(nome, null, "2026-1", null);
        MvcResult result = mockMvc.perform(post("/materias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(materiaDto)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }
}

package br.com.estudofacil.estudo_facil_api.controller;

import br.com.estudofacil.estudo_facil_api.AbstractIntegrationTest;
import br.com.estudofacil.estudo_facil_api.dto.request.MateriaRequestDTO;
import br.com.estudofacil.estudo_facil_api.dto.request.TarefaRequestDTO;
import br.com.estudofacil.estudo_facil_api.enums.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TarefaControllerTest extends AbstractIntegrationTest {

    @Test
    void crudTarefa_deveFuncionarComOwnership() throws Exception {
        String tokenA = registrarEObterToken("Tarefa User A", "tarefaa@test.com", "senha123");
        String tokenB = registrarEObterToken("Tarefa User B", "tarefab@test.com", "senha123");

        Long materiaId = criarMateriaViaApi(tokenA, "Algoritmos");

        TarefaRequestDTO dto = new TarefaRequestDTO(
                "Lista 1", "Exercícios cap. 1", LocalDate.now().plusDays(7),
                TaskStatus.PENDENTE, materiaId);

        MvcResult createResult = mockMvc.perform(post("/tarefas")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Lista 1"))
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andReturn();

        Long tarefaId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/tarefas/" + tarefaId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk());

        mockMvc.perform(get("/tarefas/" + tarefaId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        TarefaRequestDTO updateDto = new TarefaRequestDTO(
                "Lista 1 revisada", null, LocalDate.now().plusDays(14),
                TaskStatus.EM_ANDAMENTO, materiaId);

        mockMvc.perform(put("/tarefas/" + tarefaId)
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Lista 1 revisada"))
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"));

        mockMvc.perform(delete("/tarefas/" + tarefaId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());
    }

    @Test
    void listarTarefas_deveFiltrarPorStatusEMateria() throws Exception {
        String token = registrarEObterToken("Filtro User", "filtrotarefa@test.com", "senha123");
        Long materiaId = criarMateriaViaApi(token, "Banco de Dados");

        TarefaRequestDTO pendente = new TarefaRequestDTO(
                "Tarefa pendente", null, null, TaskStatus.PENDENTE, materiaId);
        TarefaRequestDTO concluida = new TarefaRequestDTO(
                "Tarefa concluída", null, null, TaskStatus.CONCLUIDA, materiaId);

        mockMvc.perform(post("/tarefas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pendente)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/tarefas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(concluida)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/tarefas")
                        .param("status", "PENDENTE")
                        .param("materiaId", materiaId.toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].status").value("PENDENTE"));
    }

    @Test
    void listarTarefas_semToken_deveRetornar401() throws Exception {
        mockMvc.perform(get("/tarefas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void buscarTarefa_inexistente_deveRetornar404() throws Exception {
        String token = registrarEObterToken("404 User", "tarefa404@test.com", "senha123");

        mockMvc.perform(get("/tarefas/99999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
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

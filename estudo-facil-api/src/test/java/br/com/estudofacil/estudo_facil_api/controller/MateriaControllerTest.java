package br.com.estudofacil.estudo_facil_api.controller;

import br.com.estudofacil.estudo_facil_api.AbstractIntegrationTest;
import br.com.estudofacil.estudo_facil_api.dto.request.MateriaRequestDTO;
import br.com.estudofacil.estudo_facil_api.dto.request.NotaRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MateriaControllerTest extends AbstractIntegrationTest {

    @Test
    void crudMateria_deveFuncionarComOwnership() throws Exception {
        String tokenA = registrarEObterToken("User A", "usera@test.com", "senha123");
        String tokenB = registrarEObterToken("User B", "userb@test.com", "senha123");

        MateriaRequestDTO dto = new MateriaRequestDTO("Cálculo", "Prof. Silva", "2026-1", "Desc");
        MvcResult createResult = mockMvc.perform(post("/materias")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Cálculo"))
                .andReturn();

        Long materiaId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/materias/" + materiaId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk());

        mockMvc.perform(get("/materias/" + materiaId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        MateriaRequestDTO updateDto = new MateriaRequestDTO("Cálculo II", "Prof. Silva", "2026-1", "Desc");
        mockMvc.perform(put("/materias/" + materiaId)
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Cálculo II"));

        mockMvc.perform(delete("/materias/" + materiaId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());
    }

    @Test
    void buscarMedia_deveRetornarMediaPonderada() throws Exception {
        String token = registrarEObterToken("User Media", "media@test.com", "senha123");

        MateriaRequestDTO materiaDto = new MateriaRequestDTO("Física", "Prof.", "2026-1", null);
        MvcResult materiaResult = mockMvc.perform(post("/materias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(materiaDto)))
                .andExpect(status().isCreated())
                .andReturn();

        Long materiaId = objectMapper.readTree(materiaResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/materias/" + materiaId + "/media")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materiaId").value(materiaId))
                .andExpect(jsonPath("$.totalNotas").value(0));
    }

    @Test
    void buscarMedia_comNotas_deveCalcularMediaPonderada() throws Exception {
        String token = registrarEObterToken("Media Ponderada", "mediapon@test.com", "senha123");

        MateriaRequestDTO materiaDto = new MateriaRequestDTO("Matemática", "Prof.", "2026-1", null);
        MvcResult materiaResult = mockMvc.perform(post("/materias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(materiaDto)))
                .andExpect(status().isCreated())
                .andReturn();

        Long materiaId = objectMapper.readTree(materiaResult.getResponse().getContentAsString()).get("id").asLong();

        NotaRequestDTO nota1 = new NotaRequestDTO(8.0, 2.0, "P1", materiaId);
        NotaRequestDTO nota2 = new NotaRequestDTO(6.0, 3.0, "P2", materiaId);

        mockMvc.perform(post("/notas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nota1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/notas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nota2)))
                .andExpect(status().isCreated());

        // (8*2 + 6*3) / (2+3) = 34/5 = 6.8
        mockMvc.perform(get("/materias/" + materiaId + "/media")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaPonderada").value(6.8))
                .andExpect(jsonPath("$.totalNotas").value(2));
    }
}

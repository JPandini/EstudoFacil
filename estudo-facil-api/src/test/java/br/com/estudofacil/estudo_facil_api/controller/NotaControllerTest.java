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

class NotaControllerTest extends AbstractIntegrationTest {

    @Test
    void crudNota_deveFuncionarComOwnership() throws Exception {
        String tokenA = registrarEObterToken("Nota User A", "notactrla@test.com", "senha123");
        String tokenB = registrarEObterToken("Nota User B", "notactrlb@test.com", "senha123");

        Long materiaId = criarMateriaViaApi(tokenA, "Estatística");

        NotaRequestDTO dto = new NotaRequestDTO(8.5, 3.0, "Prova 1", materiaId);

        MvcResult createResult = mockMvc.perform(post("/notas")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valor").value(8.5))
                .andExpect(jsonPath("$.peso").value(3.0))
                .andReturn();

        Long notaId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/notas/" + notaId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk());

        mockMvc.perform(get("/notas/" + notaId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        NotaRequestDTO updateDto = new NotaRequestDTO(9.0, 3.0, "Prova 1 corrigida", materiaId);
        mockMvc.perform(put("/notas/" + notaId)
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(9.0));

        mockMvc.perform(delete("/notas/" + notaId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());
    }

    @Test
    void listarNotas_deveFiltrarPorMateria() throws Exception {
        String token = registrarEObterToken("Filtro Nota", "filtronota@test.com", "senha123");
        Long materiaA = criarMateriaViaApi(token, "Mat A");
        Long materiaB = criarMateriaViaApi(token, "Mat B");

        NotaRequestDTO notaA = new NotaRequestDTO(7.0, 2.0, null, materiaA);
        NotaRequestDTO notaB = new NotaRequestDTO(8.0, 2.0, null, materiaB);

        mockMvc.perform(post("/notas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notaA)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/notas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notaB)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/notas")
                        .param("materiaId", materiaA.toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].valor").value(7.0));
    }

    @Test
    void criarNota_pesoExcedeLimite_deveRetornar400() throws Exception {
        String token = registrarEObterToken("Peso User", "pesonota@test.com", "senha123");
        Long materiaId = criarMateriaViaApi(token, "Redes");

        NotaRequestDTO primeira = new NotaRequestDTO(8.0, 7.0, "Prova 1", materiaId);
        mockMvc.perform(post("/notas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(primeira)))
                .andExpect(status().isCreated());

        NotaRequestDTO segunda = new NotaRequestDTO(6.0, 5.0, "Prova 2", materiaId);
        mockMvc.perform(post("/notas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(segunda)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listarNotas_semToken_deveRetornar401() throws Exception {
        mockMvc.perform(get("/notas"))
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

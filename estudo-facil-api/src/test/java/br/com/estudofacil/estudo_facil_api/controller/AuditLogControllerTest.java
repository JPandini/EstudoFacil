package br.com.estudofacil.estudo_facil_api.controller;

import br.com.estudofacil.estudo_facil_api.AbstractIntegrationTest;
import br.com.estudofacil.estudo_facil_api.dto.request.MateriaRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuditLogControllerTest extends AbstractIntegrationTest {

    @Test
    void criarMateria_deveGerarLogCreate() throws Exception {
        String token = registrarEObterToken("Audit User", "audit@test.com", "senha123");
        MateriaRequestDTO dto = new MateriaRequestDTO("Física", "Prof.", "2026-1", null);

        mockMvc.perform(post("/materias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/log")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].entity").value("MATERIA"))
                .andExpect(jsonPath("$.content[0].action").value("CREATE"))
                .andExpect(jsonPath("$.content[0].userEmail").value("audit@test.com"))
                .andExpect(jsonPath("$.content[0].entityId").isNumber());
    }

    @Test
    void atualizarMateria_deveGerarLogUpdate() throws Exception {
        String token = registrarEObterToken("Update User", "update@test.com", "senha123");
        MateriaRequestDTO dto = new MateriaRequestDTO("Química", null, "2026-1", null);

        MvcResult createResult = mockMvc.perform(post("/materias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        Long materiaId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();
        MateriaRequestDTO updateDto = new MateriaRequestDTO("Química II", null, "2026-1", null);

        mockMvc.perform(put("/materias/" + materiaId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/log?action=UPDATE")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].action").value("UPDATE"))
                .andExpect(jsonPath("$.content[0].entityId").value(materiaId));
    }

    @Test
    void deletarMateria_deveGerarLogDelete() throws Exception {
        String token = registrarEObterToken("Delete User", "delete@test.com", "senha123");
        MateriaRequestDTO dto = new MateriaRequestDTO("Biologia", null, "2026-1", null);

        MvcResult createResult = mockMvc.perform(post("/materias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        Long materiaId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/materias/" + materiaId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/log?action=DELETE")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].action").value("DELETE"))
                .andExpect(jsonPath("$.content[0].entityId").value(materiaId));
    }

    @Test
    void filtroPorAction_deveRetornarApenasUpdates() throws Exception {
        String token = registrarEObterToken("Filter User", "filter@test.com", "senha123");
        MateriaRequestDTO dto = new MateriaRequestDTO("História", null, "2026-1", null);

        MvcResult createResult = mockMvc.perform(post("/materias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        Long materiaId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();
        MateriaRequestDTO updateDto = new MateriaRequestDTO("História II", null, "2026-1", null);

        mockMvc.perform(put("/materias/" + materiaId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/log?action=CREATE")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].action").value("CREATE"));

        mockMvc.perform(get("/log?action=UPDATE")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].action").value("UPDATE"));
    }

    @Test
    void listarLogs_usuarioB_naoVeLogsDoUsuarioA() throws Exception {
        String tokenA = registrarEObterToken("User A Log", "usera-log@test.com", "senha123");
        String tokenB = registrarEObterToken("User B Log", "userb-log@test.com", "senha123");

        MateriaRequestDTO dto = new MateriaRequestDTO("Geografia", null, "2026-1", null);
        mockMvc.perform(post("/materias")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/log")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        mockMvc.perform(get("/log")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void listarLogs_semToken_deveRetornar401() throws Exception {
        mockMvc.perform(get("/log"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}

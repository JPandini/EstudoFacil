package br.com.estudofacil.estudo_facil_api.controller;

import br.com.estudofacil.estudo_facil_api.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UsuarioControllerTest extends AbstractIntegrationTest {

    @Test
    void listarUsuarios_deveRetornarPaginado() throws Exception {
        String email = "userlistunico@test.com";
        String token = registrarEObterToken("User Lista", email, "senha123");

        MvcResult result = mockMvc.perform(get("/usuarios")
                        .param("page", "0")
                        .param("size", "100")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andReturn();

        JsonNode content = objectMapper.readTree(result.getResponse().getContentAsString()).get("content");
        boolean encontrado = false;
        for (JsonNode usuario : content) {
            if (email.equals(usuario.get("email").asText())) {
                encontrado = true;
                break;
            }
        }
        assertThat(encontrado).isTrue();
    }

    @Test
    void buscarUsuario_porId_deveRetornarDados() throws Exception {
        MvcResult registerResult = registrarViaApi("Busca User", "buscauser@test.com", "senha123");
        Long userId = objectMapper.readTree(registerResult.getResponse().getContentAsString()).get("id").asLong();
        String token = loginViaApi("buscauser@test.com", "senha123");

        mockMvc.perform(get("/usuarios/" + userId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value("buscauser@test.com"))
                .andExpect(jsonPath("$.nome").value("Busca User"));
    }

    @Test
    void buscarUsuario_inexistente_deveRetornar404() throws Exception {
        String token = registrarEObterToken("404 User", "user404@test.com", "senha123");

        mockMvc.perform(get("/usuarios/99999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarUsuarios_semToken_deveRetornar401() throws Exception {
        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isUnauthorized());
    }
}

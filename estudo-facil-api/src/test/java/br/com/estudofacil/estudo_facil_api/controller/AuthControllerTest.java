package br.com.estudofacil.estudo_facil_api.controller;

import br.com.estudofacil.estudo_facil_api.AbstractIntegrationTest;
import br.com.estudofacil.estudo_facil_api.dto.request.AtualizarPerfilRequestDTO;
import br.com.estudofacil.estudo_facil_api.dto.request.ExcluirContaRequestDTO;
import br.com.estudofacil.estudo_facil_api.dto.request.LoginRequestDTO;
import br.com.estudofacil.estudo_facil_api.dto.request.RegisterRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends AbstractIntegrationTest {

    @Test
    void registrar_deveRetornar201() throws Exception {
        RegisterRequestDTO dto = new RegisterRequestDTO("João", "joao@test.com", "senha123");
        mockMvc.perform(post("/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("joao@test.com"));
    }

    @Test
    void login_deveRetornarToken() throws Exception {
        registrarViaApi("Maria", "maria@test.com", "senha123");

        LoginRequestDTO dto = new LoginRequestDTO("maria@test.com", "senha123");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario.email").value("maria@test.com"));
    }

    @Test
    void perfil_deveRetornarUsuarioAutenticado() throws Exception {
        String token = registrarEObterToken("Pedro", "pedro@test.com", "senha123");

        mockMvc.perform(get("/auth/perfil")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Pedro"));
    }

    @Test
    void perfil_semToken_deveRetornar401() throws Exception {
        mockMvc.perform(get("/auth/perfil"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void atualizarPerfil_deveAtualizarNome() throws Exception {
        String token = registrarEObterToken("Ana", "ana@test.com", "senha123");
        AtualizarPerfilRequestDTO dto = new AtualizarPerfilRequestDTO("Ana Silva", null, null, null);

        mockMvc.perform(put("/auth/perfil")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Silva"));
    }

    @Test
    void excluirConta_deveRetornar204() throws Exception {
        String token = registrarEObterToken("Carlos", "carlos@test.com", "senha123");
        ExcluirContaRequestDTO dto = new ExcluirContaRequestDTO("senha123");

        mockMvc.perform(delete("/auth/conta")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());
    }
}

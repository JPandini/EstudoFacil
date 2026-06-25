package br.com.estudofacil.estudo_facil_api;

import br.com.estudofacil.estudo_facil_api.dto.request.LoginRequestDTO;
import br.com.estudofacil.estudo_facil_api.dto.request.RegisterRequestDTO;
import br.com.estudofacil.estudo_facil_api.dto.response.AuthResponseDTO;
import br.com.estudofacil.estudo_facil_api.dto.response.UsuarioResponseDTO;
import br.com.estudofacil.estudo_facil_api.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected AuthService authService;

    protected String registrarEObterToken(String nome, String email, String senha) {
        authService.registrar(new RegisterRequestDTO(nome, email, senha));
        AuthResponseDTO auth = authService.login(new LoginRequestDTO(email, senha));
        return auth.token();
    }

    protected MvcResult registrarViaApi(String nome, String email, String senha) throws Exception {
        RegisterRequestDTO dto = new RegisterRequestDTO(nome, email, senha);
        return mockMvc.perform(post("/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();
    }

    protected String loginViaApi(String email, String senha) throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO(email, senha);
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponseDTO auth = objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponseDTO.class);
        return auth.token();
    }
}

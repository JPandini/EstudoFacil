package br.com.estudofacil.estudo_facil_api.controller;

import br.com.estudofacil.estudo_facil_api.dto.request.AtualizarPerfilRequestDTO;
import br.com.estudofacil.estudo_facil_api.dto.request.ExcluirContaRequestDTO;
import br.com.estudofacil.estudo_facil_api.dto.request.LoginRequestDTO;
import br.com.estudofacil.estudo_facil_api.dto.request.RegisterRequestDTO;
import br.com.estudofacil.estudo_facil_api.dto.response.AuthResponseDTO;
import br.com.estudofacil.estudo_facil_api.dto.response.UsuarioResponseDTO;
import br.com.estudofacil.estudo_facil_api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Registro, login e gerenciamento de perfil")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registrar")
    @Operation(summary = "Registrar novo usuário")
    public ResponseEntity<UsuarioResponseDTO> registrar(@Valid @RequestBody RegisterRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(dto));
    }

    @PostMapping("/login")
    @Operation(summary = "Login e obtenção de token JWT")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @GetMapping("/perfil")
    @Operation(summary = "Obter perfil do usuário autenticado")
    public ResponseEntity<UsuarioResponseDTO> perfil(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(authService.buscarPerfil(userDetails.getUsername()));
    }

    @PutMapping("/perfil")
    @Operation(summary = "Atualizar perfil do usuário autenticado")
    public ResponseEntity<UsuarioResponseDTO> atualizarPerfil(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AtualizarPerfilRequestDTO dto) {

        return ResponseEntity.ok(authService.atualizarPerfil(userDetails.getUsername(), dto));
    }

    @DeleteMapping("/conta")
    @Operation(summary = "Excluir conta do usuário autenticado")
    public ResponseEntity<Void> excluirConta(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ExcluirContaRequestDTO dto) {

        authService.excluirConta(userDetails.getUsername(), dto);
        return ResponseEntity.noContent().build();
    }
}

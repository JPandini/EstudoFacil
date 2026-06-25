package br.com.estudofacil.estudo_facil_api.dto.response;

public record AuthResponseDTO(
        String token,
        String tipo,
        UsuarioResponseDTO usuario
) {
    public AuthResponseDTO(String token, UsuarioResponseDTO usuario) {
        this(token, "Bearer", usuario);
    }
}

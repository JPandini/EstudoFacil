package br.com.estudofacil.estudo_facil_api.service;

import br.com.estudofacil.estudo_facil_api.dto.request.AtualizarPerfilRequestDTO;
import br.com.estudofacil.estudo_facil_api.dto.request.ExcluirContaRequestDTO;
import br.com.estudofacil.estudo_facil_api.dto.request.LoginRequestDTO;
import br.com.estudofacil.estudo_facil_api.dto.request.RegisterRequestDTO;
import br.com.estudofacil.estudo_facil_api.dto.response.AuthResponseDTO;
import br.com.estudofacil.estudo_facil_api.dto.response.UsuarioResponseDTO;
import br.com.estudofacil.estudo_facil_api.entity.Usuario;
import br.com.estudofacil.estudo_facil_api.exception.BusinessException;
import br.com.estudofacil.estudo_facil_api.repository.UsuarioRepository;
import br.com.estudofacil.estudo_facil_api.security.JwtUtil;
import br.com.estudofacil.estudo_facil_api.util.EmailUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public UsuarioResponseDTO registrar(RegisterRequestDTO dto) {
        String email = EmailUtil.normalizar(dto.email());

        if (usuarioRepository.existsByEmail(email)) {
            throw new BusinessException("Email já cadastrado: " + email);
        }

        Usuario usuario = Usuario.builder()
                .nome(dto.nome())
                .email(email)
                .senha(passwordEncoder.encode(dto.senha()))
                .build();

        return UsuarioResponseDTO.from(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO dto) {
        String email = EmailUtil.normalizar(dto.email());
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));

        if (!passwordEncoder.matches(dto.senha(), usuario.getSenha())) {
            throw new BadCredentialsException("Credenciais inválidas");
        }

        String token = jwtUtil.generateToken(usuario.getEmail());
        return new AuthResponseDTO(token, UsuarioResponseDTO.from(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPerfil(String email) {
        return UsuarioResponseDTO.from(usuarioService.buscarPorEmail(email));
    }

    @Transactional
    public UsuarioResponseDTO atualizarPerfil(String email, AtualizarPerfilRequestDTO dto) {
        Usuario usuario = usuarioService.buscarPorEmail(email);

        if (StringUtils.hasText(dto.nome())) {
            usuario.setNome(dto.nome());
        }

        if (StringUtils.hasText(dto.email())) {
            String novoEmail = EmailUtil.normalizar(dto.email());
            if (!novoEmail.equals(usuario.getEmail()) && usuarioRepository.existsByEmail(novoEmail)) {
                throw new BusinessException("Email já cadastrado: " + novoEmail);
            }
            usuario.setEmail(novoEmail);
        }

        if (StringUtils.hasText(dto.novaSenha())) {
            if (!StringUtils.hasText(dto.senhaAtual())) {
                throw new BusinessException("Senha atual é obrigatória para alterar a senha");
            }
            if (!passwordEncoder.matches(dto.senhaAtual(), usuario.getSenha())) {
                throw new BadCredentialsException("Senha atual incorreta");
            }
            usuario.setSenha(passwordEncoder.encode(dto.novaSenha()));
        }

        return UsuarioResponseDTO.from(usuarioRepository.save(usuario));
    }

    @Transactional
    public void excluirConta(String email, ExcluirContaRequestDTO dto) {
        Usuario usuario = usuarioService.buscarPorEmail(email);

        if (!passwordEncoder.matches(dto.senhaAtual(), usuario.getSenha())) {
            throw new BadCredentialsException("Senha atual incorreta");
        }

        usuarioRepository.delete(usuario);
    }
}

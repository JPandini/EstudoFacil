package br.com.estudofacil.estudo_facil_api.service;

import br.com.estudofacil.estudo_facil_api.dto.response.UsuarioResponseDTO;
import br.com.estudofacil.estudo_facil_api.entity.Usuario;
import br.com.estudofacil.estudo_facil_api.exception.ResourceNotFoundException;
import br.com.estudofacil.estudo_facil_api.repository.UsuarioRepository;
import br.com.estudofacil.estudo_facil_api.util.EmailUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public Page<UsuarioResponseDTO> listarUsuarios(Pageable pageable) {
        return usuarioRepository.findAll(pageable)
                .map(UsuarioResponseDTO::from);
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        return UsuarioResponseDTO.from(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(EmailUtil.normalizar(email))
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }
}

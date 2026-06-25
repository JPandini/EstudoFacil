package br.com.estudofacil.estudo_facil_api.service;

import br.com.estudofacil.estudo_facil_api.dto.request.NotaRequestDTO;
import br.com.estudofacil.estudo_facil_api.dto.response.NotaResponseDTO;
import br.com.estudofacil.estudo_facil_api.entity.Materia;
import br.com.estudofacil.estudo_facil_api.entity.Nota;
import br.com.estudofacil.estudo_facil_api.entity.Usuario;
import br.com.estudofacil.estudo_facil_api.exception.BusinessException;
import br.com.estudofacil.estudo_facil_api.exception.ResourceNotFoundException;
import br.com.estudofacil.estudo_facil_api.repository.MateriaRepository;
import br.com.estudofacil.estudo_facil_api.repository.NotaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotaService {

    private static final double PESO_MAXIMO = 10.0;

    private final NotaRepository notaRepository;
    private final MateriaRepository materiaRepository;
    private final UsuarioService usuarioService;

    @Transactional(readOnly = true)
    public Page<NotaResponseDTO> listarNotas(String email, Long materiaId, Pageable pageable) {
        Usuario usuario = usuarioService.buscarPorEmail(email);

        if (materiaId != null) {
            materiaRepository.findByIdAndUsuarioId(materiaId, usuario.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Matéria", materiaId));
            return notaRepository.findByMateriaId(materiaId, pageable).map(NotaResponseDTO::from);
        }

        return notaRepository.findByUsuarioId(usuario.getId(), pageable).map(NotaResponseDTO::from);
    }

    @Transactional(readOnly = true)
    public NotaResponseDTO buscarNota(String email, Long id) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        Nota nota = notaRepository.findByIdAndMateriaUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Nota", id));
        return NotaResponseDTO.from(nota);
    }

    @Transactional
    public NotaResponseDTO criarNota(String email, NotaRequestDTO dto) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        Materia materia = materiaRepository.findByIdAndUsuarioId(dto.materiaId(), usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Matéria", dto.materiaId()));

        validarPesoTotal(materia.getId(), dto.peso(), null);

        Nota nota = Nota.builder()
                .valor(dto.valor())
                .peso(dto.peso())
                .observacao(dto.observacao())
                .materia(materia)
                .build();

        return NotaResponseDTO.from(notaRepository.save(nota));
    }

    @Transactional
    public NotaResponseDTO atualizarNota(String email, Long id, NotaRequestDTO dto) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        Nota nota = notaRepository.findByIdAndMateriaUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Nota", id));

        Long materiaId = dto.materiaId();
        if (!nota.getMateria().getId().equals(materiaId)) {
            Materia novaMateria = materiaRepository.findByIdAndUsuarioId(materiaId, usuario.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Matéria", materiaId));
            nota.setMateria(novaMateria);
        }

        validarPesoTotal(materiaId, dto.peso(), id);

        nota.setValor(dto.valor());
        nota.setPeso(dto.peso());
        nota.setObservacao(dto.observacao());

        return NotaResponseDTO.from(notaRepository.save(nota));
    }

    @Transactional
    public void deletarNota(String email, Long id) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        Nota nota = notaRepository.findByIdAndMateriaUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Nota", id));
        notaRepository.delete(nota);
    }

    private void validarPesoTotal(Long materiaId, Double novoPeso, Long notaIdExcluir) {
        Double pesoAtual = notaIdExcluir != null
                ? notaRepository.somarPesoPorMateriaExcluindo(materiaId, notaIdExcluir)
                : notaRepository.somarPesoPorMateria(materiaId);

        if (pesoAtual + novoPeso > PESO_MAXIMO) {
            throw new BusinessException("Soma dos pesos das notas não pode ultrapassar " + (int) PESO_MAXIMO);
        }
    }
}

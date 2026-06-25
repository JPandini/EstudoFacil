package br.com.estudofacil.estudo_facil_api.service;

import br.com.estudofacil.estudo_facil_api.dto.request.TarefaRequestDTO;
import br.com.estudofacil.estudo_facil_api.dto.response.TarefaResponseDTO;
import br.com.estudofacil.estudo_facil_api.entity.Materia;
import br.com.estudofacil.estudo_facil_api.entity.Tarefa;
import br.com.estudofacil.estudo_facil_api.entity.Usuario;
import br.com.estudofacil.estudo_facil_api.enums.TaskStatus;
import br.com.estudofacil.estudo_facil_api.exception.BusinessException;
import br.com.estudofacil.estudo_facil_api.exception.ResourceNotFoundException;
import br.com.estudofacil.estudo_facil_api.repository.MateriaRepository;
import br.com.estudofacil.estudo_facil_api.repository.TarefaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TarefaService {

    private final TarefaRepository tarefaRepository;
    private final MateriaRepository materiaRepository;
    private final UsuarioService usuarioService;

    @Transactional(readOnly = true)
    public Page<TarefaResponseDTO> listarTarefas(String email, Long materiaId, TaskStatus status, Pageable pageable) {
        Usuario usuario = usuarioService.buscarPorEmail(email);

        if (materiaId != null) {
            materiaRepository.findByIdAndUsuarioId(materiaId, usuario.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Matéria", materiaId));
            if (status != null) {
                return tarefaRepository.findByMateriaIdAndStatus(materiaId, status, pageable)
                        .map(TarefaResponseDTO::from);
            }
            return tarefaRepository.findByMateriaId(materiaId, pageable).map(TarefaResponseDTO::from);
        }

        if (status != null) {
            return tarefaRepository.findByUsuarioIdAndStatus(usuario.getId(), status, pageable)
                    .map(TarefaResponseDTO::from);
        }

        return tarefaRepository.findByUsuarioId(usuario.getId(), pageable).map(TarefaResponseDTO::from);
    }

    @Transactional(readOnly = true)
    public TarefaResponseDTO buscarTarefa(String email, Long id) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        Tarefa tarefa = tarefaRepository.findById(id)
                .filter(t -> t.getMateria().getUsuario().getId().equals(usuario.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa", id));
        return TarefaResponseDTO.from(tarefa);
    }

    @Transactional
    public TarefaResponseDTO criarTarefa(String email, TarefaRequestDTO dto) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        Materia materia = materiaRepository.findByIdAndUsuarioId(dto.materiaId(), usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Matéria", dto.materiaId()));

        validarDataEntrega(dto.dataEntrega());

        Tarefa tarefa = Tarefa.builder()
                .titulo(dto.titulo())
                .descricao(dto.descricao())
                .dataEntrega(dto.dataEntrega())
                .status(dto.status() != null ? dto.status() : TaskStatus.PENDENTE)
                .materia(materia)
                .build();

        return TarefaResponseDTO.from(tarefaRepository.save(tarefa));
    }

    @Transactional
    public TarefaResponseDTO atualizarTarefa(String email, Long id, TarefaRequestDTO dto) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        Tarefa tarefa = tarefaRepository.findById(id)
                .filter(t -> t.getMateria().getUsuario().getId().equals(usuario.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa", id));

        if (!tarefa.getMateria().getId().equals(dto.materiaId())) {
            Materia novaMateria = materiaRepository.findByIdAndUsuarioId(dto.materiaId(), usuario.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Matéria", dto.materiaId()));
            tarefa.setMateria(novaMateria);
        }

        tarefa.setTitulo(dto.titulo());
        tarefa.setDescricao(dto.descricao());
        tarefa.setDataEntrega(dto.dataEntrega());
        if (dto.status() != null) {
            tarefa.setStatus(dto.status());
        }

        return TarefaResponseDTO.from(tarefaRepository.save(tarefa));
    }

    @Transactional
    public void deletarTarefa(String email, Long id) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        Tarefa tarefa = tarefaRepository.findById(id)
                .filter(t -> t.getMateria().getUsuario().getId().equals(usuario.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa", id));
        tarefaRepository.delete(tarefa);
    }

    private void validarDataEntrega(LocalDate dataEntrega) {
        if (dataEntrega != null && dataEntrega.isBefore(LocalDate.now())) {
            throw new BusinessException("Data de entrega não pode ser no passado");
        }
    }
}

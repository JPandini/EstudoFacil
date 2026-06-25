package br.com.estudofacil.estudo_facil_api.service;

import br.com.estudofacil.estudo_facil_api.dto.response.DashboardResponseDTO;
import br.com.estudofacil.estudo_facil_api.dto.response.MateriaResumoMediaDTO;
import br.com.estudofacil.estudo_facil_api.dto.response.TarefaResponseDTO;
import br.com.estudofacil.estudo_facil_api.entity.Materia;
import br.com.estudofacil.estudo_facil_api.entity.Usuario;
import br.com.estudofacil.estudo_facil_api.enums.TaskStatus;
import br.com.estudofacil.estudo_facil_api.repository.MateriaRepository;
import br.com.estudofacil.estudo_facil_api.repository.NotaRepository;
import br.com.estudofacil.estudo_facil_api.repository.TarefaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UsuarioService usuarioService;
    private final MateriaRepository materiaRepository;
    private final TarefaRepository tarefaRepository;
    private final NotaRepository notaRepository;

    @Transactional(readOnly = true)
    public DashboardResponseDTO buscarDashboard(String email) {
        Usuario usuario = usuarioService.buscarPorEmail(email);

        long totalMaterias = materiaRepository.countByUsuarioId(usuario.getId());
        long tarefasPendentes = tarefaRepository.countByUsuarioIdAndStatus(usuario.getId(), TaskStatus.PENDENTE);
        long tarefasEmAndamento = tarefaRepository.countByUsuarioIdAndStatus(usuario.getId(), TaskStatus.EM_ANDAMENTO);
        long tarefasConcluidas = tarefaRepository.countByUsuarioIdAndStatus(usuario.getId(), TaskStatus.CONCLUIDA);
        Double mediaGeral = notaRepository.calcularMediaPonderadaPorUsuario(usuario.getId());

        List<TarefaResponseDTO> proximasTarefas = tarefaRepository
                .findProximasTarefas(usuario.getId(), TaskStatus.CONCLUIDA, PageRequest.of(0, 5))
                .stream()
                .map(TarefaResponseDTO::from)
                .toList();

        List<MateriaResumoMediaDTO> materiasComMedia = materiaRepository
                .findByUsuarioId(usuario.getId(), PageRequest.of(0, 100))
                .stream()
                .map(this::toMateriaResumoMedia)
                .toList();

        return new DashboardResponseDTO(
                totalMaterias,
                tarefasPendentes,
                tarefasConcluidas,
                tarefasEmAndamento,
                mediaGeral != null ? Math.round(mediaGeral * 100.0) / 100.0 : null,
                proximasTarefas,
                materiasComMedia
        );
    }

    private MateriaResumoMediaDTO toMateriaResumoMedia(Materia materia) {
        Double media = notaRepository.calcularMediaPonderadaPorMateria(materia.getId());
        return new MateriaResumoMediaDTO(
                materia.getId(),
                materia.getNome(),
                media != null ? Math.round(media * 100.0) / 100.0 : null
        );
    }
}

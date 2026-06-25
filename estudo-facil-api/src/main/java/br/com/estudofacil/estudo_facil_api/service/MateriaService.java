package br.com.estudofacil.estudo_facil_api.service;

import br.com.estudofacil.estudo_facil_api.dto.request.MateriaRequestDTO;
import br.com.estudofacil.estudo_facil_api.dto.response.MateriaMediaResponseDTO;
import br.com.estudofacil.estudo_facil_api.dto.response.MateriaResponseDTO;
import br.com.estudofacil.estudo_facil_api.entity.Materia;
import br.com.estudofacil.estudo_facil_api.entity.Usuario;
import br.com.estudofacil.estudo_facil_api.enums.AuditAction;
import br.com.estudofacil.estudo_facil_api.enums.AuditEntity;
import br.com.estudofacil.estudo_facil_api.exception.ResourceNotFoundException;
import br.com.estudofacil.estudo_facil_api.repository.MateriaRepository;
import br.com.estudofacil.estudo_facil_api.repository.NotaRepository;
import br.com.estudofacil.estudo_facil_api.util.SemestreUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MateriaService {

    private final MateriaRepository materiaRepository;
    private final NotaRepository notaRepository;
    private final UsuarioService usuarioService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public Page<MateriaResponseDTO> listarMaterias(String email, String semestre, Pageable pageable) {
        Usuario usuario = usuarioService.buscarPorEmail(email);

        if (semestre != null && !semestre.isBlank()) {
            return materiaRepository.findByUsuarioIdAndSemestre(usuario.getId(), semestre, pageable)
                    .map(MateriaResponseDTO::from);
        }

        return materiaRepository.findByUsuarioId(usuario.getId(), pageable)
                .map(MateriaResponseDTO::from);
    }

    @Transactional(readOnly = true)
    public MateriaResponseDTO buscarMateria(String email, Long id) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        Materia materia = materiaRepository.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Matéria", id));
        return MateriaResponseDTO.from(materia);
    }

    @Transactional(readOnly = true)
    public MateriaMediaResponseDTO buscarMedia(String email, Long id) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        Materia materia = materiaRepository.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Matéria", id));

        Double media = notaRepository.calcularMediaPonderadaPorMateria(materia.getId());
        long totalNotas = notaRepository.countByMateriaId(materia.getId());

        return new MateriaMediaResponseDTO(
                materia.getId(),
                materia.getNome(),
                media != null ? Math.round(media * 100.0) / 100.0 : null,
                totalNotas
        );
    }

    @Transactional
    public MateriaResponseDTO criarMateria(String email, MateriaRequestDTO dto) {
        Usuario usuario = usuarioService.buscarPorEmail(email);

        Materia materia = Materia.builder()
                .nome(dto.nome())
                .professor(dto.professor())
                .semestre(SemestreUtil.normalizar(dto.semestre()))
                .descricao(dto.descricao())
                .usuario(usuario)
                .build();

        Materia salva = materiaRepository.save(materia);
        auditService.registrar(AuditEntity.MATERIA, AuditAction.CREATE, salva.getId(), email);
        return MateriaResponseDTO.from(salva);
    }

    @Transactional
    public MateriaResponseDTO atualizarMateria(String email, Long id, MateriaRequestDTO dto) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        Materia materia = materiaRepository.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Matéria", id));

        materia.setNome(dto.nome());
        materia.setProfessor(dto.professor());
        materia.setSemestre(SemestreUtil.normalizar(dto.semestre()));
        materia.setDescricao(dto.descricao());

        Materia salva = materiaRepository.save(materia);
        auditService.registrar(AuditEntity.MATERIA, AuditAction.UPDATE, id, email);
        return MateriaResponseDTO.from(salva);
    }

    @Transactional
    public void deletarMateria(String email, Long id) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        Materia materia = materiaRepository.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Matéria", id));
        auditService.registrar(AuditEntity.MATERIA, AuditAction.DELETE, id, email);
        materiaRepository.delete(materia);
    }
}

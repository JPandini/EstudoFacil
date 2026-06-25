package br.com.estudofacil.estudo_facil_api.controller;

import br.com.estudofacil.estudo_facil_api.dto.request.TarefaRequestDTO;
import br.com.estudofacil.estudo_facil_api.dto.response.TarefaResponseDTO;
import br.com.estudofacil.estudo_facil_api.enums.TaskStatus;
import br.com.estudofacil.estudo_facil_api.service.TarefaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tarefas")
@RequiredArgsConstructor
public class TarefaController {

    private final TarefaService tarefaService;

    @GetMapping
    public ResponseEntity<Page<TarefaResponseDTO>> listarTarefas(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long materiaId,
            @RequestParam(required = false) TaskStatus status,
            @PageableDefault(size = 10, sort = "dataEntrega") Pageable pageable) {

        return ResponseEntity.ok(tarefaService.listarTarefas(userDetails.getUsername(), materiaId, status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TarefaResponseDTO> buscarTarefa(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        return ResponseEntity.ok(tarefaService.buscarTarefa(userDetails.getUsername(), id));
    }

    @PostMapping
    public ResponseEntity<TarefaResponseDTO> criarTarefa(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TarefaRequestDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tarefaService.criarTarefa(userDetails.getUsername(), dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TarefaResponseDTO> atualizarTarefa(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody TarefaRequestDTO dto) {

        return ResponseEntity.ok(tarefaService.atualizarTarefa(userDetails.getUsername(), id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarTarefa(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        tarefaService.deletarTarefa(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}

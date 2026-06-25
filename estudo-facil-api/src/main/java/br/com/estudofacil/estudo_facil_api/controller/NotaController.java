package br.com.estudofacil.estudo_facil_api.controller;

import br.com.estudofacil.estudo_facil_api.dto.request.NotaRequestDTO;
import br.com.estudofacil.estudo_facil_api.dto.response.NotaResponseDTO;
import br.com.estudofacil.estudo_facil_api.service.NotaService;
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
@RequestMapping("/notas")
@RequiredArgsConstructor
public class NotaController {

    private final NotaService notaService;

    @GetMapping
    public ResponseEntity<Page<NotaResponseDTO>> listarNotas(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long materiaId,
            @PageableDefault(size = 10, sort = "criadoEm") Pageable pageable) {

        return ResponseEntity.ok(notaService.listarNotas(userDetails.getUsername(), materiaId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotaResponseDTO> buscarNota(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        return ResponseEntity.ok(notaService.buscarNota(userDetails.getUsername(), id));
    }

    @PostMapping
    public ResponseEntity<NotaResponseDTO> criarNota(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody NotaRequestDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notaService.criarNota(userDetails.getUsername(), dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NotaResponseDTO> atualizarNota(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody NotaRequestDTO dto) {

        return ResponseEntity.ok(notaService.atualizarNota(userDetails.getUsername(), id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarNota(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        notaService.deletarNota(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}

package br.com.estudofacil.estudo_facil_api.controller;

import br.com.estudofacil.estudo_facil_api.dto.request.MateriaRequestDTO;
import br.com.estudofacil.estudo_facil_api.dto.response.MateriaMediaResponseDTO;
import br.com.estudofacil.estudo_facil_api.dto.response.MateriaResponseDTO;
import br.com.estudofacil.estudo_facil_api.service.MateriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/materias")
@RequiredArgsConstructor
@Tag(name = "Matérias", description = "CRUD de matérias e médias")
public class MateriaController {

    private final MateriaService materiaService;

    @GetMapping
    public ResponseEntity<Page<MateriaResponseDTO>> listarMaterias(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String semestre,
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {

        return ResponseEntity.ok(materiaService.listarMaterias(userDetails.getUsername(), semestre, pageable));
    }

    @GetMapping("/{id}/media")
    @Operation(summary = "Obter média ponderada de uma matéria")
    public ResponseEntity<MateriaMediaResponseDTO> buscarMedia(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        return ResponseEntity.ok(materiaService.buscarMedia(userDetails.getUsername(), id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar matéria por ID")
    public ResponseEntity<MateriaResponseDTO> buscarMateria(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        return ResponseEntity.ok(materiaService.buscarMateria(userDetails.getUsername(), id));
    }

    @PostMapping
    public ResponseEntity<MateriaResponseDTO> criarMateria(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MateriaRequestDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(materiaService.criarMateria(userDetails.getUsername(), dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MateriaResponseDTO> atualizarMateria(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody MateriaRequestDTO dto) {

        return ResponseEntity.ok(materiaService.atualizarMateria(userDetails.getUsername(), id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarMateria(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        materiaService.deletarMateria(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}

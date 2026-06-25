package br.com.estudofacil.estudo_facil_api.controller;

import br.com.estudofacil.estudo_facil_api.dto.response.AuditLogResponseDTO;
import br.com.estudofacil.estudo_facil_api.enums.AuditAction;
import br.com.estudofacil.estudo_facil_api.enums.AuditEntity;
import br.com.estudofacil.estudo_facil_api.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/log")
@RequiredArgsConstructor
@Tag(name = "Auditoria", description = "Consulta de registros de auditoria")
public class LogController {

    private final AuditService auditService;

    @GetMapping
    @Operation(summary = "Listar registros de auditoria do usuário autenticado")
    public ResponseEntity<Page<AuditLogResponseDTO>> listarLogs(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) AuditEntity entity,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) Long entityId,
            @PageableDefault(size = 20, sort = "timestamp") Pageable pageable) {

        return ResponseEntity.ok(auditService.listar(
                userDetails.getUsername(),
                entity,
                action,
                entityId,
                pageable
        ));
    }
}

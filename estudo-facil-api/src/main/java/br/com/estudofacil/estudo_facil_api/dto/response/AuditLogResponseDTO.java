package br.com.estudofacil.estudo_facil_api.dto.response;

import br.com.estudofacil.estudo_facil_api.entity.AuditLog;
import br.com.estudofacil.estudo_facil_api.enums.AuditAction;
import br.com.estudofacil.estudo_facil_api.enums.AuditEntity;

import java.time.LocalDateTime;

public record AuditLogResponseDTO(
        Long id,
        AuditEntity entity,
        AuditAction action,
        Long entityId,
        String userEmail,
        LocalDateTime timestamp
) {
    public static AuditLogResponseDTO from(AuditLog log) {
        return new AuditLogResponseDTO(
                log.getId(),
                log.getEntity(),
                log.getAction(),
                log.getEntityId(),
                log.getUserEmail(),
                log.getTimestamp()
        );
    }
}

package br.com.estudofacil.estudo_facil_api.service;

import br.com.estudofacil.estudo_facil_api.dto.response.AuditLogResponseDTO;
import br.com.estudofacil.estudo_facil_api.entity.AuditLog;
import br.com.estudofacil.estudo_facil_api.enums.AuditAction;
import br.com.estudofacil.estudo_facil_api.enums.AuditEntity;
import br.com.estudofacil.estudo_facil_api.repository.AuditLogRepository;
import br.com.estudofacil.estudo_facil_api.util.EmailUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void registrar(AuditEntity entity, AuditAction action, Long entityId, String userEmail) {
        AuditLog log = AuditLog.builder()
                .entity(entity)
                .action(action)
                .entityId(entityId)
                .userEmail(EmailUtil.normalizar(userEmail))
                .build();

        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponseDTO> listar(
            String userEmail,
            AuditEntity entity,
            AuditAction action,
            Long entityId,
            Pageable pageable
    ) {
        return auditLogRepository.findByFiltros(
                EmailUtil.normalizar(userEmail),
                entity,
                action,
                entityId,
                pageable
        ).map(AuditLogResponseDTO::from);
    }
}

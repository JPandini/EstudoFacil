package br.com.estudofacil.estudo_facil_api.repository;

import br.com.estudofacil.estudo_facil_api.entity.AuditLog;
import br.com.estudofacil.estudo_facil_api.enums.AuditAction;
import br.com.estudofacil.estudo_facil_api.enums.AuditEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("""
            SELECT a FROM AuditLog a
            WHERE a.userEmail = :userEmail
              AND (:entity IS NULL OR a.entity = :entity)
              AND (:action IS NULL OR a.action = :action)
              AND (:entityId IS NULL OR a.entityId = :entityId)
            ORDER BY a.timestamp DESC
            """)
    Page<AuditLog> findByFiltros(
            @Param("userEmail") String userEmail,
            @Param("entity") AuditEntity entity,
            @Param("action") AuditAction action,
            @Param("entityId") Long entityId,
            Pageable pageable
    );
}

package br.com.estudofacil.estudo_facil_api.entity;

import br.com.estudofacil.estudo_facil_api.enums.AuditAction;
import br.com.estudofacil.estudo_facil_api.enums.AuditEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_user_email", columnList = "user_email"),
        @Index(name = "idx_audit_entidade", columnList = "entidade"),
        @Index(name = "idx_audit_acao", columnList = "acao"),
        @Index(name = "idx_audit_entidade_id", columnList = "entidade_id"),
        @Index(name = "idx_audit_registrado_em", columnList = "registrado_em")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "entidade", nullable = false, length = 50)
    private AuditEntity entity;

    @Enumerated(EnumType.STRING)
    @Column(name = "acao", nullable = false, length = 20)
    private AuditAction action;

    @Column(name = "entidade_id", nullable = false)
    private Long entityId;

    @Column(name = "user_email")
    private String userEmail;

    @CreationTimestamp
    @Column(name = "registrado_em", updatable = false)
    private LocalDateTime timestamp;
}

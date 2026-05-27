package co.com.marketplace.r2dbc.audit;

import co.com.marketplace.model.audit.AuditLog;
import co.com.marketplace.model.audit.gateways.AuditLogGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuditLogRepositoryAdapter implements AuditLogGateway {

    @Override
    public Mono<AuditLog> save(AuditLog auditLog) {
        return Mono.just(auditLog)
                .doOnSubscribe(s -> log.debug("[AuditLogRepositoryAdapter#save] DB request: id={}", auditLog.getId()))
                .doOnSuccess(r -> log.debug("[AuditLogRepositoryAdapter#save] DB response: result={}", r != null))
                .doOnError(e -> log.error("[AuditLogRepositoryAdapter#save] DB error: {}", e.getMessage()));
    }
}

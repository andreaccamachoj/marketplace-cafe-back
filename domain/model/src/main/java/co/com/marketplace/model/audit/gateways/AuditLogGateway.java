package co.com.marketplace.model.audit.gateways;

import co.com.marketplace.model.audit.AuditLog;
import reactor.core.publisher.Mono;

public interface AuditLogGateway {
    Mono<AuditLog> save(AuditLog log);
}

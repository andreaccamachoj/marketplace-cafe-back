package co.com.marketplace.model.admin.gateways;

import co.com.marketplace.model.admin.AdminActivityLog;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface ActivityLogGateway {
    Mono<AdminActivityLog> save(AdminActivityLog log);
    Flux<AdminActivityLog> findAll(UUID actorId, String action, OffsetDateTime from,
                                   OffsetDateTime to, int page, int size);
    Mono<Long> countAll(UUID actorId, String action, OffsetDateTime from, OffsetDateTime to);
}

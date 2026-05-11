package co.com.marketplace.usecase.admin;

import co.com.marketplace.model.admin.AdminActivityLog;
import co.com.marketplace.model.admin.gateways.ActivityLogGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public final class ListAdminActivityUseCase {

    private final ActivityLogGateway activityLogGateway;

    public Flux<AdminActivityLog> execute(UUID actorId, String action,
                                           OffsetDateTime from, OffsetDateTime to,
                                           int page, int size) {
        return activityLogGateway.findAll(actorId, action, from, to, page, size);
    }

    public Mono<Long> count(UUID actorId, String action,
                             OffsetDateTime from, OffsetDateTime to) {
        return activityLogGateway.countAll(actorId, action, from, to);
    }
}

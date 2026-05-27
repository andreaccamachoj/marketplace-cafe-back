package co.com.marketplace.usecase.admin;

import co.com.marketplace.model.admin.AdminActivityLog;
import co.com.marketplace.model.admin.gateways.ActivityLogGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListAdminActivityUseCaseTest {

    @Mock private ActivityLogGateway activityLogGateway;

    @InjectMocks
    private ListAdminActivityUseCase useCase;

    private final UUID actorId = UUID.randomUUID();

    @Test
    void execute_returnsActivityLogs() {
        AdminActivityLog log = AdminActivityLog.builder().id(UUID.randomUUID())
                .actorId(actorId).type("BAN_USER").title("Banned user")
                .createdAt(OffsetDateTime.now()).build();
        when(activityLogGateway.findAll(isNull(), isNull(), isNull(), isNull(), any(int.class), any(int.class)))
                .thenReturn(Flux.just(log));

        StepVerifier.create(useCase.execute(null, null, null, null, 0, 10))
                .expectNextMatches(l -> "BAN_USER".equals(l.getType()))
                .verifyComplete();
    }

    @Test
    void count_returnsCount() {
        when(activityLogGateway.countAll(isNull(), isNull(), isNull(), isNull())).thenReturn(Mono.just(3L));

        StepVerifier.create(useCase.count(null, null, null, null))
                .expectNext(3L)
                .verifyComplete();
    }
}

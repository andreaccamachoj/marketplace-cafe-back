package co.com.marketplace.r2dbc.admin;

import co.com.marketplace.model.admin.AdminActivityLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.function.BiFunction;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked", "rawtypes"})
@ExtendWith(MockitoExtension.class)
class ActivityLogRepositoryAdapterTest {

    @Mock private AdminActivityLogReactiveRepository repo;
    @Mock private DatabaseClient db;
    @Mock private DatabaseClient.GenericExecuteSpec spec;
    @Mock private RowsFetchSpec fetchSpec;

    @InjectMocks private ActivityLogRepositoryAdapter adapter;

    private final UUID logId = UUID.randomUUID();
    private final UUID actorId = UUID.randomUUID();
    private AdminActivityLogData logData;
    private AdminActivityLog log;

    @BeforeEach
    void setUp() {
        logData = AdminActivityLogData.builder()
                .id(logId)
                .actorId(actorId)
                .actorNameSnapshot("Admin User")
                .type("USER_BANNED")
                .title("Usuario baneado")
                .description("Se baneó al usuario X")
                .severity("high")
                .iconEmoji("🔨")
                .createdAt(OffsetDateTime.now())
                .build();

        log = AdminActivityLog.builder()
                .id(logId)
                .actorId(actorId)
                .actorNameSnapshot("Admin User")
                .type("USER_BANNED")
                .title("Usuario baneado")
                .description("Se baneó al usuario X")
                .severity("high")
                .iconEmoji("🔨")
                .createdAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void save_returnsLog_whenSuccessful() {
        when(repo.save(any(AdminActivityLogData.class))).thenReturn(Mono.just(logData));

        StepVerifier.create(adapter.save(log))
                .expectNextMatches(l -> logId.equals(l.getId()) && "USER_BANNED".equals(l.getType()))
                .verifyComplete();
    }

    @Test
    void save_propagatesError_whenRepositoryFails() {
        when(repo.save(any(AdminActivityLogData.class))).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.save(log))
                .verifyError(RuntimeException.class);
    }

    @Test
    void findAll_returnsLogs_withNoFilters() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Flux.just(logData)).when(fetchSpec).all();

        StepVerifier.create(adapter.findAll(null, null, null, null, 0, 10))
                .expectNextMatches(l -> logId.equals(l.getId()))
                .verifyComplete();
    }

    @Test
    void findAll_withFilters_bindsThem() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Flux.empty()).when(fetchSpec).all();

        OffsetDateTime from = OffsetDateTime.now().minusDays(7);
        OffsetDateTime to = OffsetDateTime.now();

        StepVerifier.create(adapter.findAll(actorId, "USER_BANNED", from, to, 0, 10))
                .verifyComplete();

        verify(spec, atLeastOnce()).bind(anyString(), any());
    }

    @Test
    void countAll_returnsCount_withNoFilters() {
        when(db.sql(anyString())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.just(15L)).when(fetchSpec).one();

        StepVerifier.create(adapter.countAll(null, null, null, null))
                .expectNext(15L)
                .verifyComplete();
    }

    @Test
    void countAll_withFilters_returnsCount() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.just(3L)).when(fetchSpec).one();

        StepVerifier.create(adapter.countAll(actorId, "USER_BANNED", null, null))
                .expectNext(3L)
                .verifyComplete();
    }

    @Test
    void findAll_propagatesError() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Flux.error(new RuntimeException("DB error"))).when(fetchSpec).all();

        StepVerifier.create(adapter.findAll(null, null, null, null, 0, 10))
                .verifyError(RuntimeException.class);
    }

    @Test
    void countAll_propagatesError() {
        when(db.sql(anyString())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.error(new RuntimeException("DB error"))).when(fetchSpec).one();

        StepVerifier.create(adapter.countAll(null, null, null, null))
                .verifyError(RuntimeException.class);
    }
}

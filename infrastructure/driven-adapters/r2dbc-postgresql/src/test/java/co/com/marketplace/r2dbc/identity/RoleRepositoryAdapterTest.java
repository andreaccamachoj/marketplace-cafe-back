package co.com.marketplace.r2dbc.identity;

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

import java.util.UUID;
import java.util.function.BiFunction;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked", "rawtypes"})
@ExtendWith(MockitoExtension.class)
class RoleRepositoryAdapterTest {

    @Mock private RoleReactiveRepository repository;
    @Mock private DatabaseClient databaseClient;
    @Mock private DatabaseClient.GenericExecuteSpec spec;
    @Mock private RowsFetchSpec fetchSpec;

    @InjectMocks private RoleRepositoryAdapter adapter;

    private final UUID userId = UUID.randomUUID();
    private RoleData roleData;

    @BeforeEach
    void setUp() {
        roleData = RoleData.builder()
                .id(1)
                .name("BUYER")
                .description("Buyer role")
                .build();
    }

    @Test
    void findByName_returnsRole_whenFound() {
        when(repository.findByName("BUYER")).thenReturn(Mono.just(roleData));

        StepVerifier.create(adapter.findByName("BUYER"))
                .expectNextMatches(r -> "BUYER".equals(r.getName()))
                .verifyComplete();
    }

    @Test
    void findByName_returnsEmpty_whenNotFound() {
        when(repository.findByName("UNKNOWN")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByName("UNKNOWN"))
                .verifyComplete();
    }

    @Test
    void findByUserId_returnsRoles_whenFound() {
        when(databaseClient.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Flux.just(roleData)).when(fetchSpec).all();

        StepVerifier.create(adapter.findByUserId(userId))
                .expectNextMatches(r -> "BUYER".equals(r.getName()))
                .verifyComplete();
    }

    @Test
    void findByUserId_returnsEmpty_whenNoRoles() {
        when(databaseClient.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Flux.empty()).when(fetchSpec).all();

        StepVerifier.create(adapter.findByUserId(userId))
                .verifyComplete();
    }

    @Test
    void assignRoleToUser_completesSuccessfully() {
        when(databaseClient.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        when(spec.then()).thenReturn(Mono.empty());

        StepVerifier.create(adapter.assignRoleToUser(userId, 1))
                .verifyComplete();
    }

    @Test
    void assignRoleToUser_propagatesError() {
        when(databaseClient.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        when(spec.then()).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.assignRoleToUser(userId, 1))
                .verifyError(RuntimeException.class);
    }
}

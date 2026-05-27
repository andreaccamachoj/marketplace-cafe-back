package co.com.marketplace.r2dbc.identity;

import co.com.marketplace.model.identity.User;
import co.com.marketplace.model.identity.UserStatus;
import co.com.marketplace.r2dbc.type.UserStatusType;
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
class UserRepositoryAdapterTest {

    @Mock private UserReactiveRepository repository;
    @Mock private DatabaseClient databaseClient;
    @Mock private DatabaseClient.GenericExecuteSpec spec;
    @Mock private RowsFetchSpec fetchSpec;

    @InjectMocks private UserRepositoryAdapter adapter;

    private final UUID userId = UUID.randomUUID();
    private UserData userData;
    private User user;

    @BeforeEach
    void setUp() {
        userData = UserData.builder()
                .id(userId)
                .email("test@example.com")
                .passwordHash("hashed")
                .fullName("Test User")
                .phone("123456")
                .status(UserStatusType.active)
                .privacyConsent(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        user = User.builder()
                .id(userId)
                .email("test@example.com")
                .hashedPassword("hashed")
                .fullName("Test User")
                .phone("123456")
                .status(UserStatus.active)
                .privacyConsent(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void save_returnsUser_whenSuccessful() {
        when(repository.save(any(UserData.class))).thenReturn(Mono.just(userData));

        StepVerifier.create(adapter.save(user))
                .expectNextMatches(u -> userId.equals(u.getId()) && "test@example.com".equals(u.getEmail()))
                .verifyComplete();
    }

    @Test
    void save_propagatesError_whenRepositoryFails() {
        when(repository.save(any(UserData.class))).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.save(user))
                .verifyError(RuntimeException.class);
    }

    @Test
    void findById_returnsUser_whenFound() {
        when(repository.findById(userId)).thenReturn(Mono.just(userData));

        StepVerifier.create(adapter.findById(userId))
                .expectNextMatches(u -> userId.equals(u.getId()))
                .verifyComplete();
    }

    @Test
    void findById_returnsEmpty_whenNotFound() {
        when(repository.findById(userId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findById(userId))
                .verifyComplete();
    }

    @Test
    void findByEmail_returnsUser_whenFound() {
        when(repository.findByEmail("test@example.com")).thenReturn(Mono.just(userData));

        StepVerifier.create(adapter.findByEmail("test@example.com"))
                .expectNextMatches(u -> "test@example.com".equals(u.getEmail()))
                .verifyComplete();
    }

    @Test
    void findByEmail_returnsEmpty_whenNotFound() {
        when(repository.findByEmail("nobody@example.com")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByEmail("nobody@example.com"))
                .verifyComplete();
    }

    @Test
    void existsByEmail_returnsTrue_whenExists() {
        when(repository.existsByEmail("test@example.com")).thenReturn(Mono.just(true));

        StepVerifier.create(adapter.existsByEmail("test@example.com"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void existsByEmail_returnsFalse_whenNotExists() {
        when(repository.existsByEmail("nobody@example.com")).thenReturn(Mono.just(false));

        StepVerifier.create(adapter.existsByEmail("nobody@example.com"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void update_returnsUpdatedUser_whenSuccessful() {
        when(repository.save(any(UserData.class))).thenReturn(Mono.just(userData));

        StepVerifier.create(adapter.update(user))
                .expectNextMatches(u -> userId.equals(u.getId()))
                .verifyComplete();
    }

    @Test
    void deleteById_completesSuccessfully() {
        when(repository.deleteById(userId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.deleteById(userId))
                .verifyComplete();
    }

    @Test
    void findAll_usesDatabaseClient_withNoFilters() {
        when(databaseClient.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Flux.empty()).when(fetchSpec).all();

        StepVerifier.create(adapter.findAll(null, null, null, 0, 10))
                .verifyComplete();
    }

    @Test
    void countAll_usesDatabaseClient_withNoFilters() {
        when(databaseClient.sql(anyString())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.just(5L)).when(fetchSpec).one();

        StepVerifier.create(adapter.countAll(null, null, null))
                .expectNext(5L)
                .verifyComplete();
    }

    @Test
    void findAll_withFilters_bindsThem() {
        when(databaseClient.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Flux.empty()).when(fetchSpec).all();

        StepVerifier.create(adapter.findAll("BUYER", UserStatus.active, "test", 0, 10))
                .verifyComplete();

        verify(spec, atLeastOnce()).bind(anyString(), any());
    }

    @Test
    void findById_propagatesError_whenRepositoryFails() {
        when(repository.findById(userId)).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.findById(userId))
                .verifyError(RuntimeException.class);
    }

    @Test
    void findByEmail_propagatesError_whenRepositoryFails() {
        when(repository.findByEmail("test@example.com")).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.findByEmail("test@example.com"))
                .verifyError(RuntimeException.class);
    }

    @Test
    void update_propagatesError_whenRepositoryFails() {
        when(repository.save(any(UserData.class))).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.update(user))
                .verifyError(RuntimeException.class);
    }

    @Test
    void deleteById_propagatesError_whenRepositoryFails() {
        when(repository.deleteById(userId)).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.deleteById(userId))
                .verifyError(RuntimeException.class);
    }

    @Test
    void existsByEmail_propagatesError_whenRepositoryFails() {
        when(repository.existsByEmail("test@example.com")).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.existsByEmail("test@example.com"))
                .verifyError(RuntimeException.class);
    }

    @Test
    void findAll_withOnlyRoleFilter_bindsRoleFilter() {
        when(databaseClient.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Flux.just(user)).when(fetchSpec).all();

        StepVerifier.create(adapter.findAll("BUYER", null, null, 0, 10))
                .expectNextCount(1)
                .verifyComplete();

        verify(spec).bind(eq("roleFilter"), eq("BUYER"));
    }

    @Test
    void findAll_withOnlyStatusFilter_bindsStatusFilter() {
        when(databaseClient.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Flux.empty()).when(fetchSpec).all();

        StepVerifier.create(adapter.findAll(null, UserStatus.active, null, 0, 10))
                .verifyComplete();

        verify(spec).bind(eq("statusFilter"), eq("active"));
    }

    @Test
    void findAll_withOnlySearch_bindsSearch() {
        when(databaseClient.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Flux.empty()).when(fetchSpec).all();

        StepVerifier.create(adapter.findAll(null, null, "cafe", 0, 10))
                .verifyComplete();

        verify(spec).bind(eq("search"), eq("%cafe%"));
    }

    @Test
    void countAll_withOnlyRoleFilter_bindsRoleFilter() {
        when(databaseClient.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.just(3L)).when(fetchSpec).one();

        StepVerifier.create(adapter.countAll("BUYER", null, null))
                .expectNext(3L)
                .verifyComplete();

        verify(spec).bind(eq("roleFilter"), eq("BUYER"));
    }

    @Test
    void countAll_withOnlyStatusFilter_bindsStatusFilter() {
        when(databaseClient.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.just(2L)).when(fetchSpec).one();

        StepVerifier.create(adapter.countAll(null, UserStatus.active, null))
                .expectNext(2L)
                .verifyComplete();
    }

    @Test
    void countAll_withOnlySearch_bindsSearch() {
        when(databaseClient.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.just(1L)).when(fetchSpec).one();

        StepVerifier.create(adapter.countAll(null, null, "test"))
                .expectNext(1L)
                .verifyComplete();

        verify(spec).bind(eq("search"), eq("%test%"));
    }

    @Test
    void countAll_withAllFilters_bindsAll() {
        when(databaseClient.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.just(7L)).when(fetchSpec).one();

        StepVerifier.create(adapter.countAll("BUYER", UserStatus.active, "test"))
                .expectNext(7L)
                .verifyComplete();

        verify(spec, atLeastOnce()).bind(anyString(), any());
    }

    @Test
    void findAll_propagatesError_whenDatabaseClientFails() {
        when(databaseClient.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Flux.error(new RuntimeException("DB error"))).when(fetchSpec).all();

        StepVerifier.create(adapter.findAll(null, null, null, 0, 10))
                .verifyError(RuntimeException.class);
    }
}

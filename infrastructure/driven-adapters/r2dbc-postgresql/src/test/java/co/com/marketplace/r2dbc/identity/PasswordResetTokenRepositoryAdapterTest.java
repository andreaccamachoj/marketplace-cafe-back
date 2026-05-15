package co.com.marketplace.r2dbc.identity;

import co.com.marketplace.model.identity.PasswordResetToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetTokenRepositoryAdapterTest {

    @Mock private PasswordResetTokenReactiveRepository repository;
    @Mock private DatabaseClient databaseClient;
    @Mock private DatabaseClient.GenericExecuteSpec spec;

    @InjectMocks private PasswordResetTokenRepositoryAdapter adapter;

    private final UUID tokenId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private PasswordResetTokenData tokenData;
    private PasswordResetToken token;

    @BeforeEach
    void setUp() {
        tokenData = PasswordResetTokenData.builder()
                .id(tokenId)
                .userId(userId)
                .tokenHash("abc123hash")
                .expiresAt(OffsetDateTime.now().plusHours(1))
                .createdAt(OffsetDateTime.now())
                .build();

        token = PasswordResetToken.builder()
                .id(tokenId)
                .userId(userId)
                .tokenHash("abc123hash")
                .expiresAt(OffsetDateTime.now().plusHours(1))
                .createdAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void save_returnsToken_whenSuccessful() {
        when(repository.save(any(PasswordResetTokenData.class))).thenReturn(Mono.just(tokenData));

        StepVerifier.create(adapter.save(token))
                .expectNextMatches(t -> tokenId.equals(t.getId()) && "abc123hash".equals(t.getTokenHash()))
                .verifyComplete();
    }

    @Test
    void save_propagatesError_whenRepositoryFails() {
        when(repository.save(any(PasswordResetTokenData.class))).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.save(token))
                .verifyError(RuntimeException.class);
    }

    @Test
    void findByTokenHash_returnsToken_whenFound() {
        when(repository.findByTokenHash("abc123hash")).thenReturn(Mono.just(tokenData));

        StepVerifier.create(adapter.findByTokenHash("abc123hash"))
                .expectNextMatches(t -> "abc123hash".equals(t.getTokenHash()))
                .verifyComplete();
    }

    @Test
    void findByTokenHash_returnsEmpty_whenNotFound() {
        when(repository.findByTokenHash("invalid")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByTokenHash("invalid"))
                .verifyComplete();
    }

    @Test
    void markUsed_completesSuccessfully() {
        when(databaseClient.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        when(spec.then()).thenReturn(Mono.empty());

        StepVerifier.create(adapter.markUsed(tokenId))
                .verifyComplete();
    }

    @Test
    void markUsed_propagatesError() {
        when(databaseClient.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        when(spec.then()).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.markUsed(tokenId))
                .verifyError(RuntimeException.class);
    }
}

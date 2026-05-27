package co.com.marketplace.usecase.identity;

import co.com.marketplace.model.identity.PasswordResetToken;
import co.com.marketplace.model.identity.User;
import co.com.marketplace.model.identity.UserStatus;
import co.com.marketplace.model.identity.gateways.PasswordResetTokenGateway;
import co.com.marketplace.model.identity.gateways.UserGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestPasswordResetUseCaseTest {

    @Mock private UserGateway userGateway;
    @Mock private PasswordResetTokenGateway tokenGateway;

    @InjectMocks
    private RequestPasswordResetUseCase useCase;

    private final UUID userId = UUID.randomUUID();

    @Test
    void execute_returnsRawToken_whenUserFound() {
        User user = User.builder().id(userId).email("u@e.com").fullName("U").status(UserStatus.active)
                .privacyConsent(false).createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
        PasswordResetToken saved = PasswordResetToken.builder().id(UUID.randomUUID())
                .userId(userId).tokenHash("hash").expiresAt(OffsetDateTime.now().plusHours(1))
                .createdAt(OffsetDateTime.now()).build();

        when(userGateway.findByEmail("u@e.com")).thenReturn(Mono.just(user));
        when(tokenGateway.save(any())).thenReturn(Mono.just(saved));

        StepVerifier.create(useCase.execute("u@e.com"))
                .expectNextMatches(token -> token != null && !token.isEmpty())
                .verifyComplete();
    }

    @Test
    void execute_returnsEmpty_whenUserNotFound() {
        when(userGateway.findByEmail("none@e.com")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute("none@e.com"))
                .expectNext("")
                .verifyComplete();
    }
}

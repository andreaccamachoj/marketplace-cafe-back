package co.com.marketplace.usecase.identity;

import co.com.marketplace.model.exception.ValidationException;
import co.com.marketplace.model.gateway.PasswordEncoderGateway;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfirmPasswordResetUseCaseTest {

    @Mock private PasswordResetTokenGateway tokenGateway;
    @Mock private UserGateway userGateway;
    @Mock private PasswordEncoderGateway passwordEncoder;

    @InjectMocks
    private ConfirmPasswordResetUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID tokenId = UUID.randomUUID();

    // raw token whose SHA-256 we can match by mocking findByTokenHash to accept any string
    private final String rawToken = "testtoken123";

    @Test
    void execute_completesSuccessfully_whenTokenValid() {
        PasswordResetToken token = PasswordResetToken.builder()
                .id(tokenId).userId(userId).tokenHash("any")
                .expiresAt(OffsetDateTime.now().plusHours(1))
                .usedAt(null).createdAt(OffsetDateTime.now()).build();
        User user = User.builder().id(userId).email("u@e.com").fullName("U").status(UserStatus.active)
                .hashedPassword("old").privacyConsent(false)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();

        when(tokenGateway.findByTokenHash(anyString())).thenReturn(Mono.just(token));
        when(userGateway.findById(userId)).thenReturn(Mono.just(user));
        when(passwordEncoder.encode("newpass")).thenReturn("newhash");
        when(userGateway.update(any())).thenReturn(Mono.just(user));
        when(tokenGateway.markUsed(tokenId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(rawToken, "newpass"))
                .verifyComplete();
    }

    @Test
    void execute_throwsValidation_whenTokenNotFound() {
        when(tokenGateway.findByTokenHash(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(rawToken, "newpass"))
                .verifyError(ValidationException.class);
    }

    @Test
    void execute_throwsValidation_whenTokenAlreadyUsed() {
        PasswordResetToken token = PasswordResetToken.builder()
                .id(tokenId).userId(userId).tokenHash("any")
                .expiresAt(OffsetDateTime.now().plusHours(1))
                .usedAt(OffsetDateTime.now().minusMinutes(5))
                .createdAt(OffsetDateTime.now()).build();

        when(tokenGateway.findByTokenHash(anyString())).thenReturn(Mono.just(token));

        StepVerifier.create(useCase.execute(rawToken, "newpass"))
                .verifyError(ValidationException.class);
    }

    @Test
    void execute_throwsValidation_whenTokenExpired() {
        PasswordResetToken token = PasswordResetToken.builder()
                .id(tokenId).userId(userId).tokenHash("any")
                .expiresAt(OffsetDateTime.now().minusHours(1))
                .usedAt(null).createdAt(OffsetDateTime.now()).build();

        when(tokenGateway.findByTokenHash(anyString())).thenReturn(Mono.just(token));

        StepVerifier.create(useCase.execute(rawToken, "newpass"))
                .verifyError(ValidationException.class);
    }
}

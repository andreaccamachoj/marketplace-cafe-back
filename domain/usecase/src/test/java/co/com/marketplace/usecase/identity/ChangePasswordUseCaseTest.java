package co.com.marketplace.usecase.identity;

import co.com.marketplace.model.exception.ValidationException;
import co.com.marketplace.model.gateway.PasswordEncoderGateway;
import co.com.marketplace.model.identity.User;
import co.com.marketplace.model.identity.UserStatus;
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
class ChangePasswordUseCaseTest {

    @Mock private UserGateway userGateway;
    @Mock private PasswordEncoderGateway passwordEncoder;

    @InjectMocks
    private ChangePasswordUseCase changePasswordUseCase;

    private final UUID userId = UUID.randomUUID();

    private User buildUser() {
        return User.builder()
                .id(userId).email("user@example.com").hashedPassword("oldHashed")
                .fullName("User").phone("111").status(UserStatus.active)
                .privacyConsent(false).createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void execute_updatesPassword_whenCurrentPasswordCorrect() {
        User user = buildUser();
        when(userGateway.findById(userId)).thenReturn(Mono.just(user));
        when(passwordEncoder.matches("oldPass", "oldHashed")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("newHashed");
        when(userGateway.update(any())).thenReturn(Mono.just(user));

        StepVerifier.create(changePasswordUseCase.execute(userId, "oldPass", "newPass"))
                .verifyComplete();
    }

    @Test
    void execute_throwsValidation_whenCurrentPasswordWrong() {
        User user = buildUser();
        when(userGateway.findById(userId)).thenReturn(Mono.just(user));
        when(passwordEncoder.matches("wrongPass", "oldHashed")).thenReturn(false);

        StepVerifier.create(changePasswordUseCase.execute(userId, "wrongPass", "newPass"))
                .verifyError(ValidationException.class);
    }
}

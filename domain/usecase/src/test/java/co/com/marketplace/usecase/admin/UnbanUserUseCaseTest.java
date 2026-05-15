package co.com.marketplace.usecase.admin;

import co.com.marketplace.model.exception.NotFoundException;
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
class UnbanUserUseCaseTest {

    @Mock private UserGateway userGateway;

    @InjectMocks
    private UnbanUserUseCase useCase;

    private final UUID userId = UUID.randomUUID();

    @Test
    void execute_unbansUser_whenFound() {
        User user = User.builder().id(userId).email("u@e.com").fullName("U")
                .status(UserStatus.banned).privacyConsent(false)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
        User unbanned = user.toBuilder().status(UserStatus.active).build();

        when(userGateway.findById(userId)).thenReturn(Mono.just(user));
        when(userGateway.update(any())).thenReturn(Mono.just(unbanned));

        StepVerifier.create(useCase.execute(userId))
                .expectNextMatches(u -> UserStatus.active.equals(u.getStatus()))
                .verifyComplete();
    }

    @Test
    void execute_throwsNotFound_whenUserMissing() {
        when(userGateway.findById(userId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(userId))
                .verifyError(NotFoundException.class);
    }
}

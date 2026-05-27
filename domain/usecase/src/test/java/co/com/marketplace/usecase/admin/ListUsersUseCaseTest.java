package co.com.marketplace.usecase.admin;

import co.com.marketplace.model.identity.User;
import co.com.marketplace.model.identity.UserStatus;
import co.com.marketplace.model.identity.gateways.UserGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListUsersUseCaseTest {

    @Mock private UserGateway userGateway;

    @InjectMocks
    private ListUsersUseCase useCase;

    @Test
    void execute_returnsUsers() {
        User user = User.builder().id(UUID.randomUUID()).email("u@e.com").fullName("U")
                .status(UserStatus.active).privacyConsent(false)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
        when(userGateway.findAll("BUYER", null, null, 0, 10)).thenReturn(Flux.just(user));

        StepVerifier.create(useCase.execute("BUYER", null, null, 0, 10))
                .expectNextMatches(u -> UserStatus.active.equals(u.getStatus()))
                .verifyComplete();
    }

    @Test
    void execute_returnsEmpty_whenNoUsers() {
        when(userGateway.findAll(null, null, null, 0, 10)).thenReturn(Flux.empty());

        StepVerifier.create(useCase.execute(null, null, null, 0, 10))
                .verifyComplete();
    }
}

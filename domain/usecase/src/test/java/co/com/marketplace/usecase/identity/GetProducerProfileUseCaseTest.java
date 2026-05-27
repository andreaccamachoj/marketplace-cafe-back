package co.com.marketplace.usecase.identity;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.identity.ProducerProfile;
import co.com.marketplace.model.identity.ProducerStatus;
import co.com.marketplace.model.identity.User;
import co.com.marketplace.model.identity.UserStatus;
import co.com.marketplace.model.identity.gateways.ProducerProfileGateway;
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

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProducerProfileUseCaseTest {

    @Mock private UserGateway userGateway;
    @Mock private ProducerProfileGateway producerProfileGateway;

    @InjectMocks
    private GetProducerProfileUseCase useCase;

    private final UUID userId = UUID.randomUUID();

    private User buildUser() {
        return User.builder().id(userId).email("p@e.com").fullName("Producer").phone("2")
                .status(UserStatus.active).privacyConsent(false)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
    }

    @Test
    void execute_returnsResult_whenBothFound() {
        ProducerProfile profile = ProducerProfile.builder().userId(userId)
                .bio("Coffee grower").city("Medellín").department("Antioquia")
                .status(ProducerStatus.approved).avatarInitials("PG").build();
        when(userGateway.findById(userId)).thenReturn(Mono.just(buildUser()));
        when(producerProfileGateway.findByUserId(userId)).thenReturn(Mono.just(profile));

        StepVerifier.create(useCase.execute(userId))
                .expectNextMatches(r -> "approved".equals(r.status()) && "Medellín".equals(r.city()))
                .verifyComplete();
    }

    @Test
    void execute_returnsNullStatus_whenProfileMissing() {
        when(userGateway.findById(userId)).thenReturn(Mono.just(buildUser()));
        when(producerProfileGateway.findByUserId(userId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(userId))
                .expectNextMatches(r -> r.status() == null)
                .verifyComplete();
    }

    @Test
    void execute_throwsNotFound_whenUserMissing() {
        when(userGateway.findById(userId)).thenReturn(Mono.empty());
        when(producerProfileGateway.findByUserId(userId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(userId))
                .verifyError(NotFoundException.class);
    }
}

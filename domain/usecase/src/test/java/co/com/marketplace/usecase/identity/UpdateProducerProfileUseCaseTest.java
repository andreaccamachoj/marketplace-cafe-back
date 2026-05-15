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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateProducerProfileUseCaseTest {

    @Mock private UserGateway userGateway;
    @Mock private ProducerProfileGateway producerProfileGateway;

    @InjectMocks
    private UpdateProducerProfileUseCase useCase;

    private final UUID userId = UUID.randomUUID();

    @Test
    void execute_returnsUpdatedResult_whenUserExists() {
        User user = User.builder().id(userId).email("p@e.com").fullName("Old").phone("1")
                .status(UserStatus.active).privacyConsent(false)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
        User updated = user.toBuilder().fullName("New").build();
        ProducerProfile profile = ProducerProfile.builder().userId(userId).status(ProducerStatus.approved).bio("bio").build();
        ProducerProfile updatedProfile = profile.toBuilder().bio("new bio").build();

        when(userGateway.findById(userId)).thenReturn(Mono.just(user));
        when(userGateway.update(any())).thenReturn(Mono.just(updated));
        when(producerProfileGateway.findByUserId(userId)).thenReturn(Mono.just(profile));
        when(producerProfileGateway.update(any())).thenReturn(Mono.just(updatedProfile));

        UpdateProducerProfileUseCase.Command cmd = new UpdateProducerProfileUseCase.Command(
                "New", null, "new bio", null, null, null);

        StepVerifier.create(useCase.execute(userId, cmd))
                .expectNextMatches(r -> "New".equals(r.fullName()) && "new bio".equals(r.bio()))
                .verifyComplete();
    }

    @Test
    void execute_throwsNotFound_whenUserMissing() {
        when(userGateway.findById(userId)).thenReturn(Mono.empty());
        when(producerProfileGateway.findByUserId(userId)).thenReturn(Mono.empty());

        UpdateProducerProfileUseCase.Command cmd = new UpdateProducerProfileUseCase.Command(
                null, null, null, null, null, null);

        StepVerifier.create(useCase.execute(userId, cmd))
                .verifyError(NotFoundException.class);
    }
}

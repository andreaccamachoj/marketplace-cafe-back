package co.com.marketplace.usecase.identity;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.identity.BuyerProfile;
import co.com.marketplace.model.identity.User;
import co.com.marketplace.model.identity.UserStatus;
import co.com.marketplace.model.identity.gateways.BuyerProfileGateway;
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
class UpdateBuyerProfileUseCaseTest {

    @Mock private UserGateway userGateway;
    @Mock private BuyerProfileGateway buyerProfileGateway;

    @InjectMocks
    private UpdateBuyerProfileUseCase useCase;

    private final UUID userId = UUID.randomUUID();

    private User buildUser(UUID id) {
        return User.builder().id(id).email("u@e.com").fullName("Old Name").phone("1")
                .status(UserStatus.active).privacyConsent(false)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
    }

    @Test
    void execute_returnsUpdatedResult_whenUserExists() {
        User user = buildUser(userId);
        User updated = user.toBuilder().fullName("New Name").build();
        BuyerProfile profile = BuyerProfile.builder().userId(userId).city("Bogotá").newsletterOptIn(true).build();
        BuyerProfile updatedProfile = profile.toBuilder().city("Cali").build();

        when(userGateway.findById(userId)).thenReturn(Mono.just(user));
        when(userGateway.update(any())).thenReturn(Mono.just(updated));
        when(buyerProfileGateway.findByUserId(userId)).thenReturn(Mono.just(profile));
        when(buyerProfileGateway.update(any())).thenReturn(Mono.just(updatedProfile));

        UpdateBuyerProfileUseCase.Command cmd = new UpdateBuyerProfileUseCase.Command(
                "New Name", null, "Cali", null, null, null, null);

        StepVerifier.create(useCase.execute(userId, cmd))
                .expectNextMatches(r -> "New Name".equals(r.fullName()))
                .verifyComplete();
    }

    @Test
    void execute_throwsNotFound_whenUserMissing() {
        when(userGateway.findById(userId)).thenReturn(Mono.empty());
        when(buyerProfileGateway.findByUserId(userId)).thenReturn(Mono.empty());

        UpdateBuyerProfileUseCase.Command cmd = new UpdateBuyerProfileUseCase.Command(
                null, null, null, null, null, null, null);

        StepVerifier.create(useCase.execute(userId, cmd))
                .verifyError(NotFoundException.class);
    }
}

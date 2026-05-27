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

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetBuyerProfileUseCaseTest {

    @Mock private UserGateway userGateway;
    @Mock private BuyerProfileGateway buyerProfileGateway;

    @InjectMocks
    private GetBuyerProfileUseCase useCase;

    private final UUID userId = UUID.randomUUID();

    private User buildUser() {
        return User.builder().id(userId).email("u@e.com").fullName("U").phone("1")
                .status(UserStatus.active).privacyConsent(false)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
    }

    @Test
    void execute_returnsResult_whenBothFound() {
        BuyerProfile profile = BuyerProfile.builder().userId(userId).city("Bogotá").department("Cundinamarca")
                .preferredPayment("card").newsletterOptIn(true).avatarInitials("AB").build();
        when(userGateway.findById(userId)).thenReturn(Mono.just(buildUser()));
        when(buyerProfileGateway.findByUserId(userId)).thenReturn(Mono.just(profile));

        StepVerifier.create(useCase.execute(userId))
                .expectNextMatches(r -> "Bogotá".equals(r.city()) && r.newsletterOptIn())
                .verifyComplete();
    }

    @Test
    void execute_returnsEmptyProfile_whenProfileMissing() {
        when(userGateway.findById(userId)).thenReturn(Mono.just(buildUser()));
        when(buyerProfileGateway.findByUserId(userId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(userId))
                .expectNextMatches(r -> r.city() == null)
                .verifyComplete();
    }

    @Test
    void execute_throwsNotFound_whenUserMissing() {
        when(userGateway.findById(userId)).thenReturn(Mono.empty());
        when(buyerProfileGateway.findByUserId(userId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(userId))
                .verifyError(NotFoundException.class);
    }
}

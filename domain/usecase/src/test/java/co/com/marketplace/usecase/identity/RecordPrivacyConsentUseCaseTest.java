package co.com.marketplace.usecase.identity;

import co.com.marketplace.model.identity.PrivacyConsent;
import co.com.marketplace.model.identity.User;
import co.com.marketplace.model.identity.UserStatus;
import co.com.marketplace.model.identity.gateways.PrivacyConsentGateway;
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
class RecordPrivacyConsentUseCaseTest {

    @Mock private PrivacyConsentGateway consentGateway;
    @Mock private UserGateway userGateway;

    @InjectMocks
    private RecordPrivacyConsentUseCase useCase;

    private final UUID userId = UUID.randomUUID();

    @Test
    void execute_completesSuccessfully() {
        User user = User.builder().id(userId).email("u@e.com").fullName("U").status(UserStatus.active)
                .privacyConsent(false).createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
        PrivacyConsent consent = PrivacyConsent.builder().userId(userId).policyVersion("v1")
                .acceptedAt(OffsetDateTime.now()).ipAddress("127.0.0.1").build();

        when(consentGateway.save(any())).thenReturn(Mono.just(consent));
        when(userGateway.findById(userId)).thenReturn(Mono.just(user));
        when(userGateway.update(any())).thenReturn(Mono.just(user.toBuilder().privacyConsent(true).build()));

        StepVerifier.create(useCase.execute(userId, "v1", "127.0.0.1"))
                .verifyComplete();
    }

    @Test
    void execute_propagatesError_whenConsentSaveFails() {
        when(consentGateway.save(any())).thenReturn(Mono.error(new RuntimeException("DB error")));
        when(userGateway.findById(userId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(userId, "v1", "127.0.0.1"))
                .verifyError(RuntimeException.class);
    }
}

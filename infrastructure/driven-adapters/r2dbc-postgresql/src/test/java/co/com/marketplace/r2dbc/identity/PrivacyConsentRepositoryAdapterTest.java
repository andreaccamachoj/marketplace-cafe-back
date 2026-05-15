package co.com.marketplace.r2dbc.identity;

import co.com.marketplace.model.identity.PrivacyConsent;
import org.junit.jupiter.api.BeforeEach;
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
class PrivacyConsentRepositoryAdapterTest {

    @Mock private PrivacyConsentReactiveRepository repository;

    @InjectMocks private PrivacyConsentRepositoryAdapter adapter;

    private final UUID consentId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private PrivacyConsentData consentData;
    private PrivacyConsent consent;

    @BeforeEach
    void setUp() {
        consentData = PrivacyConsentData.builder()
                .id(consentId)
                .userId(userId)
                .policyVersion("1.0")
                .acceptedAt(OffsetDateTime.now())
                .ipAddress("127.0.0.1")
                .build();

        consent = PrivacyConsent.builder()
                .id(consentId)
                .userId(userId)
                .policyVersion("1.0")
                .acceptedAt(OffsetDateTime.now())
                .ipAddress("127.0.0.1")
                .build();
    }

    @Test
    void save_returnsConsent_whenSuccessful() {
        when(repository.save(any(PrivacyConsentData.class))).thenReturn(Mono.just(consentData));

        StepVerifier.create(adapter.save(consent))
                .expectNextMatches(c -> consentId.equals(c.getId()) && "1.0".equals(c.getPolicyVersion()))
                .verifyComplete();
    }

    @Test
    void save_propagatesError_whenRepositoryFails() {
        when(repository.save(any(PrivacyConsentData.class))).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.save(consent))
                .verifyError(RuntimeException.class);
    }

    @Test
    void findLatestByUserId_returnsConsent_whenFound() {
        when(repository.findLatestByUserId(userId)).thenReturn(Mono.just(consentData));

        StepVerifier.create(adapter.findLatestByUserId(userId))
                .expectNextMatches(c -> userId.equals(c.getUserId()))
                .verifyComplete();
    }

    @Test
    void findLatestByUserId_returnsEmpty_whenNotFound() {
        when(repository.findLatestByUserId(userId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findLatestByUserId(userId))
                .verifyComplete();
    }
}

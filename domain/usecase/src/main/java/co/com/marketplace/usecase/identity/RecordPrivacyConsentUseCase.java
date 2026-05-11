package co.com.marketplace.usecase.identity;

import co.com.marketplace.model.identity.PrivacyConsent;
import co.com.marketplace.model.identity.gateways.PrivacyConsentGateway;
import co.com.marketplace.model.identity.gateways.UserGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public final class RecordPrivacyConsentUseCase {

    private final PrivacyConsentGateway consentGateway;
    private final UserGateway userGateway;

    public Mono<Void> execute(UUID userId, String policyVersion, String ipAddress) {
        return consentGateway.save(PrivacyConsent.builder()
                        .userId(userId)
                        .policyVersion(policyVersion)
                        .acceptedAt(OffsetDateTime.now())
                        .ipAddress(ipAddress)
                        .build())
                .then(userGateway.findById(userId)
                        .flatMap(user -> userGateway.update(user.toBuilder()
                                .privacyConsent(true)
                                .updatedAt(OffsetDateTime.now())
                                .build())))
                .then();
    }
}

package co.com.marketplace.model.identity.gateways;

import co.com.marketplace.model.identity.PrivacyConsent;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PrivacyConsentGateway {
    Mono<PrivacyConsent> save(PrivacyConsent consent);
    Mono<PrivacyConsent> findLatestByUserId(UUID userId);
}

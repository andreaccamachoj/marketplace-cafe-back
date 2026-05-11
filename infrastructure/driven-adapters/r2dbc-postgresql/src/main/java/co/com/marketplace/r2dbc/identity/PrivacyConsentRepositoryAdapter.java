package co.com.marketplace.r2dbc.identity;

import co.com.marketplace.model.identity.PrivacyConsent;
import co.com.marketplace.model.identity.gateways.PrivacyConsentGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrivacyConsentRepositoryAdapter implements PrivacyConsentGateway {

    private final PrivacyConsentReactiveRepository repository;

    private static PrivacyConsent toDomain(PrivacyConsentData d) {
        return PrivacyConsent.builder()
                .id(d.getId())
                .userId(d.getUserId())
                .policyVersion(d.getPolicyVersion())
                .acceptedAt(d.getAcceptedAt())
                .ipAddress(d.getIpAddress())
                .build();
    }

    private static PrivacyConsentData toData(PrivacyConsent c) {
        return PrivacyConsentData.builder()
                .id(c.getId())
                .userId(c.getUserId())
                .policyVersion(c.getPolicyVersion())
                .acceptedAt(c.getAcceptedAt())
                .ipAddress(c.getIpAddress())
                .build();
    }

    @Override
    public Mono<PrivacyConsent> save(PrivacyConsent consent) {
        return repository.save(toData(consent))
                .doOnSubscribe(s -> log.debug("[PrivacyConsentRepositoryAdapter#save] DB request: userId={}", consent.getUserId()))
                .doOnSuccess(r -> log.debug("[PrivacyConsentRepositoryAdapter#save] DB response: result={}", r != null))
                .doOnError(e -> log.error("[PrivacyConsentRepositoryAdapter#save] DB error: {}", e.getMessage()))
                .map(PrivacyConsentRepositoryAdapter::toDomain);
    }

    @Override
    public Mono<PrivacyConsent> findLatestByUserId(UUID userId) {
        return repository.findLatestByUserId(userId)
                .doOnSubscribe(s -> log.debug("[PrivacyConsentRepositoryAdapter#findLatestByUserId] DB request: userId={}", userId))
                .doOnSuccess(r -> log.debug("[PrivacyConsentRepositoryAdapter#findLatestByUserId] DB response: found={}", r != null))
                .doOnError(e -> log.error("[PrivacyConsentRepositoryAdapter#findLatestByUserId] DB error: {}", e.getMessage()))
                .map(PrivacyConsentRepositoryAdapter::toDomain);
    }
}

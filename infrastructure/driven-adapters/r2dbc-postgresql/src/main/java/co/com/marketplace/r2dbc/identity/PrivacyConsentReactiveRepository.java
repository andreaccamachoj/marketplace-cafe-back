package co.com.marketplace.r2dbc.identity;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PrivacyConsentReactiveRepository extends ReactiveCrudRepository<PrivacyConsentData, UUID> {
    @Query("SELECT * FROM marketplace.privacy_consents WHERE user_id = :userId ORDER BY accepted_at DESC LIMIT 1")
    Mono<PrivacyConsentData> findLatestByUserId(UUID userId);
}

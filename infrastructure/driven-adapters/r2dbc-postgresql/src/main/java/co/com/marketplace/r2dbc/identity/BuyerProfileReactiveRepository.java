package co.com.marketplace.r2dbc.identity;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface BuyerProfileReactiveRepository extends ReactiveCrudRepository<BuyerProfileData, UUID> {
    Mono<BuyerProfileData> findByUserId(UUID userId);
}

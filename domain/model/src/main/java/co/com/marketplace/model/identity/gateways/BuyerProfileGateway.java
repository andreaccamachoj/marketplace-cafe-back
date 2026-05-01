package co.com.marketplace.model.identity.gateways;

import co.com.marketplace.model.identity.BuyerProfile;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface BuyerProfileGateway {
    Mono<BuyerProfile> save(BuyerProfile profile);
    Mono<BuyerProfile> findByUserId(UUID userId);
    Mono<BuyerProfile> update(BuyerProfile profile);
}

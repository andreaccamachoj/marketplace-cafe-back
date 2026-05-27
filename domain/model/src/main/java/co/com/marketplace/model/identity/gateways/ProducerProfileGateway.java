package co.com.marketplace.model.identity.gateways;

import co.com.marketplace.model.identity.ProducerProfile;
import co.com.marketplace.model.identity.ProducerStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProducerProfileGateway {
    Mono<ProducerProfile> save(ProducerProfile profile);
    Mono<ProducerProfile> findByUserId(UUID userId);
    Mono<ProducerProfile> findById(UUID id);
    Mono<ProducerProfile> update(ProducerProfile profile);
    Flux<ProducerProfile> findByStatus(ProducerStatus status, int page, int size);
    Mono<Long> countByStatus(ProducerStatus status);
}

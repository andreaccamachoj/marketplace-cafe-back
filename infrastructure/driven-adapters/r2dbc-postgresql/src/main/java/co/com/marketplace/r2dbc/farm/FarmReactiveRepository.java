package co.com.marketplace.r2dbc.farm;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FarmReactiveRepository extends ReactiveCrudRepository<FarmData, UUID> {
    Mono<FarmData> findByProducerId(UUID producerId);
}

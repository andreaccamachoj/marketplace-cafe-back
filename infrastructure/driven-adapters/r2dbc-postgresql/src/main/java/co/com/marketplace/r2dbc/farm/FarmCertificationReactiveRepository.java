package co.com.marketplace.r2dbc.farm;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface FarmCertificationReactiveRepository extends ReactiveCrudRepository<FarmCertificationData, UUID> {
    Flux<FarmCertificationData> findByFarmId(UUID farmId);
}

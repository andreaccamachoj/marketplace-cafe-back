package co.com.marketplace.r2dbc.identity;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface AddressReactiveRepository extends ReactiveCrudRepository<AddressData, UUID> {
    Flux<AddressData> findByUserId(UUID userId);
}

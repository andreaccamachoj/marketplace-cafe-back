package co.com.marketplace.model.catalog.gateways;

import co.com.marketplace.model.catalog.RoastLevel;
import reactor.core.publisher.Flux;

public interface RoastLevelGateway {
    Flux<RoastLevel> findAll();
}

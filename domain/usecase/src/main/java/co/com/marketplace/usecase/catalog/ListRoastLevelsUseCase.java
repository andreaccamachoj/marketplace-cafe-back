package co.com.marketplace.usecase.catalog;

import co.com.marketplace.model.catalog.RoastLevel;
import co.com.marketplace.model.catalog.gateways.RoastLevelGateway;
import reactor.core.publisher.Flux;

public final class ListRoastLevelsUseCase {

    private final RoastLevelGateway roastLevelGateway;

    public ListRoastLevelsUseCase(RoastLevelGateway roastLevelGateway) {
        this.roastLevelGateway = roastLevelGateway;
    }

    public Flux<RoastLevel> execute() {
        return roastLevelGateway.findAll();
    }
}

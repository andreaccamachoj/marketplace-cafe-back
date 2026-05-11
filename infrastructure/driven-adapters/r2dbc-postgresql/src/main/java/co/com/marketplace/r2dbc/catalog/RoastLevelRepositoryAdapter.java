package co.com.marketplace.r2dbc.catalog;

import co.com.marketplace.model.catalog.RoastLevel;
import co.com.marketplace.model.catalog.gateways.RoastLevelGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoastLevelRepositoryAdapter implements RoastLevelGateway {

    private final RoastLevelReactiveRepository repository;

    @Override
    public Flux<RoastLevel> findAll() {
        return repository.findAll()
                .doOnSubscribe(s -> log.debug("[RoastLevelRepositoryAdapter#findAll] DB request"))
                .doOnComplete(() -> log.debug("[RoastLevelRepositoryAdapter#findAll] DB response: complete"))
                .doOnError(e -> log.error("[RoastLevelRepositoryAdapter#findAll] DB error: {}", e.getMessage()))
                .map(RoastLevelRepositoryAdapter::toDomain);
    }

    static RoastLevel toDomain(RoastLevelData d) {
        return RoastLevel.builder()
                .id(d.getId())
                .code(d.getCode())
                .name(d.getName())
                .description(d.getDescription())
                .icon(d.getIcon())
                .build();
    }
}

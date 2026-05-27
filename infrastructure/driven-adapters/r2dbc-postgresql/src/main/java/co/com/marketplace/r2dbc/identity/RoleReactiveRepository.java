package co.com.marketplace.r2dbc.identity;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface RoleReactiveRepository extends ReactiveCrudRepository<RoleData, Integer> {
    Mono<RoleData> findByName(String name);
}

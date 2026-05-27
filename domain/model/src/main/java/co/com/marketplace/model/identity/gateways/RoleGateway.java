package co.com.marketplace.model.identity.gateways;

import co.com.marketplace.model.identity.Role;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RoleGateway {
    Mono<Role> findByName(String name);
    Flux<Role> findByUserId(UUID userId);
    Mono<Void> assignRoleToUser(UUID userId, Integer roleId);
}

package co.com.marketplace.r2dbc.identity;

import co.com.marketplace.model.identity.Role;
import co.com.marketplace.model.identity.gateways.RoleGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoleRepositoryAdapter implements RoleGateway {

    private final RoleReactiveRepository repository;
    private final DatabaseClient databaseClient;

    private static Role toDomain(RoleData d) {
        return Role.builder()
                .id(d.getId())
                .name(d.getName())
                .description(d.getDescription())
                .build();
    }

    @Override
    public Mono<Role> findByName(String name) {
        return repository.findByName(name)
                .doOnSubscribe(s -> log.debug("[RoleRepositoryAdapter#findByName] DB request: name={}", name))
                .doOnSuccess(r -> log.debug("[RoleRepositoryAdapter#findByName] DB response: found={}", r != null))
                .doOnError(e -> log.error("[RoleRepositoryAdapter#findByName] DB error: {}", e.getMessage()))
                .map(RoleRepositoryAdapter::toDomain);
    }

    @Override
    public Flux<Role> findByUserId(UUID userId) {
        return databaseClient.sql(
                        "SELECT r.* FROM marketplace.roles r " +
                        "JOIN marketplace.user_roles ur ON r.id = ur.role_id " +
                        "WHERE ur.user_id = :userId")
                .bind("userId", userId)
                .map((row, meta) -> RoleData.builder()
                        .id(row.get("id", Integer.class))
                        .name(row.get("name", String.class))
                        .description(row.get("description", String.class))
                        .build())
                .all()
                .doOnSubscribe(s -> log.debug("[RoleRepositoryAdapter#findByUserId] DB request: userId={}", userId))
                .doOnComplete(() -> log.debug("[RoleRepositoryAdapter#findByUserId] DB response: complete"))
                .doOnError(e -> log.error("[RoleRepositoryAdapter#findByUserId] DB error: {}", e.getMessage()))
                .map(RoleRepositoryAdapter::toDomain);
    }

    @Override
    public Mono<Void> assignRoleToUser(UUID userId, Integer roleId) {
        return databaseClient.sql(
                        "INSERT INTO marketplace.user_roles (user_id, role_id) " +
                        "VALUES (:userId, :roleId) ON CONFLICT DO NOTHING")
                .bind("userId", userId)
                .bind("roleId", roleId)
                .then()
                .doOnSubscribe(s -> log.debug("[RoleRepositoryAdapter#assignRoleToUser] DB request: userId={}, roleId={}", userId, roleId))
                .doOnTerminate(() -> log.debug("[RoleRepositoryAdapter#assignRoleToUser] DB response: done"))
                .doOnError(e -> log.error("[RoleRepositoryAdapter#assignRoleToUser] DB error: {}", e.getMessage()));
    }
}

package co.com.marketplace.model.identity.gateways;

import co.com.marketplace.model.identity.User;
import co.com.marketplace.model.identity.UserStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserGateway {
    Mono<User> save(User user);
    Mono<User> findById(UUID id);
    Mono<User> findByEmail(String email);
    Mono<Boolean> existsByEmail(String email);
    Mono<User> update(User user);
    Mono<Void> deleteById(UUID id);
    Flux<User> findAll(String roleFilter, UserStatus statusFilter, String search, int page, int size);
    Mono<Long> countAll(String roleFilter, UserStatus statusFilter, String search);
}

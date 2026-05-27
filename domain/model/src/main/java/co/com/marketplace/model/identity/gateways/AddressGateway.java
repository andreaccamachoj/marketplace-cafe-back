package co.com.marketplace.model.identity.gateways;

import co.com.marketplace.model.identity.Address;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AddressGateway {
    Mono<Address> save(Address address);
    Mono<Address> findById(UUID id);
    Flux<Address> findByUserId(UUID userId);
    Mono<Address> update(Address address);
    Mono<Void> deleteById(UUID id);
    Mono<Void> clearDefaultForUser(UUID userId);
}

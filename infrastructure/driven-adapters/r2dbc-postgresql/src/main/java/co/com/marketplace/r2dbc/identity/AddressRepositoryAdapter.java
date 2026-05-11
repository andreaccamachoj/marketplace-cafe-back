package co.com.marketplace.r2dbc.identity;

import co.com.marketplace.model.identity.Address;
import co.com.marketplace.model.identity.gateways.AddressGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddressRepositoryAdapter implements AddressGateway {

    private final AddressReactiveRepository repository;
    private final R2dbcEntityTemplate template;
    private final DatabaseClient databaseClient;

    private static Address toDomain(AddressData d) {
        return Address.builder()
                .id(d.getId())
                .userId(d.getUserId())
                .label(d.getLabel())
                .line1(d.getLine1())
                .line2(d.getLine2())
                .city(d.getCity())
                .department(d.getDepartment())
                .zipCode(d.getZipCode())
                .isDefault(d.isDefault())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }

    private static AddressData toData(Address a) {
        return AddressData.builder()
                .id(a.getId())
                .userId(a.getUserId())
                .label(a.getLabel())
                .line1(a.getLine1())
                .line2(a.getLine2())
                .city(a.getCity())
                .department(a.getDepartment())
                .zipCode(a.getZipCode())
                .isDefault(a.isDefault())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }

    @Override
    public Mono<Address> save(Address address) {
        return repository.save(toData(address))
                .doOnSubscribe(s -> log.debug("[AddressRepositoryAdapter#save] DB request: userId={}", address.getUserId()))
                .doOnSuccess(r -> log.debug("[AddressRepositoryAdapter#save] DB response: result={}", r != null))
                .doOnError(e -> log.error("[AddressRepositoryAdapter#save] DB error: {}", e.getMessage()))
                .map(AddressRepositoryAdapter::toDomain);
    }

    @Override
    public Mono<Address> findById(UUID id) {
        return repository.findById(id)
                .doOnSubscribe(s -> log.debug("[AddressRepositoryAdapter#findById] DB request: id={}", id))
                .doOnSuccess(r -> log.debug("[AddressRepositoryAdapter#findById] DB response: found={}", r != null))
                .doOnError(e -> log.error("[AddressRepositoryAdapter#findById] DB error: {}", e.getMessage()))
                .map(AddressRepositoryAdapter::toDomain);
    }

    @Override
    public Flux<Address> findByUserId(UUID userId) {
        return repository.findByUserId(userId)
                .doOnSubscribe(s -> log.debug("[AddressRepositoryAdapter#findByUserId] DB request: userId={}", userId))
                .doOnComplete(() -> log.debug("[AddressRepositoryAdapter#findByUserId] DB response: complete"))
                .doOnError(e -> log.error("[AddressRepositoryAdapter#findByUserId] DB error: {}", e.getMessage()))
                .map(AddressRepositoryAdapter::toDomain);
    }

    @Override
    public Mono<Address> update(Address address) {
        return template.update(toData(address))
                .doOnSubscribe(s -> log.debug("[AddressRepositoryAdapter#update] DB request: id={}", address.getId()))
                .doOnSuccess(r -> log.debug("[AddressRepositoryAdapter#update] DB response: result={}", r != null))
                .doOnError(e -> log.error("[AddressRepositoryAdapter#update] DB error: {}", e.getMessage()))
                .map(AddressRepositoryAdapter::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return repository.deleteById(id)
                .doOnSubscribe(s -> log.debug("[AddressRepositoryAdapter#deleteById] DB request: id={}", id))
                .doOnTerminate(() -> log.debug("[AddressRepositoryAdapter#deleteById] DB response: done"))
                .doOnError(e -> log.error("[AddressRepositoryAdapter#deleteById] DB error: {}", e.getMessage()));
    }

    @Override
    public Mono<Void> clearDefaultForUser(UUID userId) {
        return databaseClient.sql(
                        "UPDATE marketplace.addresses SET is_default = FALSE WHERE user_id = :userId")
                .bind("userId", userId)
                .then()
                .doOnSubscribe(s -> log.debug("[AddressRepositoryAdapter#clearDefaultForUser] DB request: userId={}", userId))
                .doOnTerminate(() -> log.debug("[AddressRepositoryAdapter#clearDefaultForUser] DB response: done"))
                .doOnError(e -> log.error("[AddressRepositoryAdapter#clearDefaultForUser] DB error: {}", e.getMessage()));
    }
}

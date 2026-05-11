package co.com.marketplace.usecase.address;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.identity.gateways.AddressGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public final class SetDefaultAddressUseCase {

    private final AddressGateway addressGateway;

    public Mono<Void> execute(UUID userId, UUID addressId) {
        return addressGateway.findById(addressId)
                .switchIfEmpty(Mono.error(new NotFoundException("ADDRESS_NOT_FOUND",
                        "Dirección no encontrada")))
                .flatMap(address -> addressGateway.clearDefaultForUser(userId)
                        .then(addressGateway.update(address.toBuilder()
                                .isDefault(true)
                                .updatedAt(OffsetDateTime.now())
                                .build())))
                .then();
    }
}

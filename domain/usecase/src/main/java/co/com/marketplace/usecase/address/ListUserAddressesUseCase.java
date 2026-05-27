package co.com.marketplace.usecase.address;

import co.com.marketplace.model.identity.Address;
import co.com.marketplace.model.identity.gateways.AddressGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RequiredArgsConstructor
public final class ListUserAddressesUseCase {

    private final AddressGateway addressGateway;

    public Flux<Address> execute(UUID userId) {
        return addressGateway.findByUserId(userId);
    }
}

package co.com.marketplace.usecase.address;

import co.com.marketplace.model.identity.Address;
import co.com.marketplace.model.identity.gateways.AddressGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public final class CreateAddressUseCase {

    private final AddressGateway addressGateway;

    public record Command(String label, String line1, String line2, String city,
                          String department, String zipCode, boolean isDefault) {}

    public Mono<Address> execute(UUID userId, Command cmd) {
        Mono<Void> clearDefault = cmd.isDefault()
                ? addressGateway.clearDefaultForUser(userId)
                : Mono.empty();

        return clearDefault.then(addressGateway.save(Address.builder()
                .userId(userId)
                .label(cmd.label())
                .line1(cmd.line1())
                .line2(cmd.line2())
                .city(cmd.city())
                .department(cmd.department())
                .zipCode(cmd.zipCode())
                .isDefault(cmd.isDefault())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build()));
    }
}

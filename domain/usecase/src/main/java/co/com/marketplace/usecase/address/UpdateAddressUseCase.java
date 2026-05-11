package co.com.marketplace.usecase.address;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.identity.Address;
import co.com.marketplace.model.identity.gateways.AddressGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public final class UpdateAddressUseCase {

    private final AddressGateway addressGateway;

    public record Command(String label, String line1, String line2, String city,
                          String department, String zipCode) {}

    public Mono<Address> execute(UUID userId, UUID addressId, Command cmd) {
        return addressGateway.findById(addressId)
                .switchIfEmpty(Mono.error(new NotFoundException("ADDRESS_NOT_FOUND",
                        "Dirección no encontrada")))
                .flatMap(address -> addressGateway.update(address.toBuilder()
                        .label(cmd.label() != null ? cmd.label() : address.getLabel())
                        .line1(cmd.line1() != null ? cmd.line1() : address.getLine1())
                        .line2(cmd.line2() != null ? cmd.line2() : address.getLine2())
                        .city(cmd.city() != null ? cmd.city() : address.getCity())
                        .department(cmd.department() != null ? cmd.department() : address.getDepartment())
                        .zipCode(cmd.zipCode() != null ? cmd.zipCode() : address.getZipCode())
                        .updatedAt(OffsetDateTime.now())
                        .build()));
    }
}

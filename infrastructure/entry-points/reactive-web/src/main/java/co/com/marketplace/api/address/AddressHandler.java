package co.com.marketplace.api.address;

import co.com.marketplace.usecase.address.CreateAddressUseCase;
import co.com.marketplace.usecase.address.DeleteAddressUseCase;
import co.com.marketplace.usecase.address.ListUserAddressesUseCase;
import co.com.marketplace.usecase.address.SetDefaultAddressUseCase;
import co.com.marketplace.usecase.address.UpdateAddressUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AddressHandler {

    private final ListUserAddressesUseCase listUserAddressesUseCase;
    private final CreateAddressUseCase createAddressUseCase;
    private final UpdateAddressUseCase updateAddressUseCase;
    private final DeleteAddressUseCase deleteAddressUseCase;
    private final SetDefaultAddressUseCase setDefaultAddressUseCase;

    record AddressRequest(String label, String line1, String line2, String city,
                          String department, String zipCode, boolean isDefault) {}

    public Mono<ServerResponse> list(ServerRequest request) {
        return userId(request)
                .flatMapMany(listUserAddressesUseCase::execute)
                .collectList()
                .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        return userId(request)
                .flatMap(uid -> request.bodyToMono(AddressRequest.class)
                        .flatMap(body -> createAddressUseCase.execute(uid,
                                new CreateAddressUseCase.Command(
                                        body.label(), body.line1(), body.line2(),
                                        body.city(), body.department(), body.zipCode(), body.isDefault()))))
                .flatMap(addr -> ServerResponse.status(HttpStatus.CREATED).bodyValue(addr));
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        UUID addressId = UUID.fromString(request.pathVariable("id"));
        return userId(request)
                .flatMap(uid -> request.bodyToMono(AddressRequest.class)
                        .flatMap(body -> updateAddressUseCase.execute(uid, addressId,
                                new UpdateAddressUseCase.Command(
                                        body.label(), body.line1(), body.line2(),
                                        body.city(), body.department(), body.zipCode()))))
                .flatMap(addr -> ServerResponse.ok().bodyValue(addr));
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        UUID addressId = UUID.fromString(request.pathVariable("id"));
        return userId(request)
                .flatMap(uid -> deleteAddressUseCase.execute(uid, addressId))
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> setDefault(ServerRequest request) {
        UUID addressId = UUID.fromString(request.pathVariable("id"));
        return userId(request)
                .flatMap(uid -> setDefaultAddressUseCase.execute(uid, addressId))
                .then(ServerResponse.noContent().build());
    }

    private Mono<UUID> userId(ServerRequest request) {
        return request.principal().map(p -> UUID.fromString(p.getName()));
    }
}

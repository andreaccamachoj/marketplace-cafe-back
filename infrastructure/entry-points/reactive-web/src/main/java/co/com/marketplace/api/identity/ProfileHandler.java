package co.com.marketplace.api.identity;

import co.com.marketplace.usecase.identity.GetBuyerProfileUseCase;
import co.com.marketplace.usecase.identity.GetProducerProfileUseCase;
import co.com.marketplace.usecase.identity.UpdateBuyerProfileUseCase;
import co.com.marketplace.usecase.identity.UpdateProducerProfileUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProfileHandler {

    private final GetBuyerProfileUseCase getBuyerProfileUseCase;
    private final GetProducerProfileUseCase getProducerProfileUseCase;
    private final UpdateBuyerProfileUseCase updateBuyerProfileUseCase;
    private final UpdateProducerProfileUseCase updateProducerProfileUseCase;

    record BuyerProfilePatch(String city, String department, String preferredPayment,
                             Boolean newsletterOptIn, String avatarInitials) {}
    record ProducerProfilePatch(String bio, String city, String department, String avatarInitials) {}

    public Mono<ServerResponse> getBuyerProfile(ServerRequest request) {
        return userId(request)
                .flatMap(getBuyerProfileUseCase::execute)
                .flatMap(profile -> ServerResponse.ok().bodyValue(profile));
    }

    public Mono<ServerResponse> patchBuyerProfile(ServerRequest request) {
        return userId(request)
                .flatMap(uid -> request.bodyToMono(BuyerProfilePatch.class)
                        .flatMap(body -> updateBuyerProfileUseCase.execute(uid,
                                new UpdateBuyerProfileUseCase.Command(
                                        body.city(), body.department(), body.preferredPayment(),
                                        body.newsletterOptIn(), body.avatarInitials()))))
                .flatMap(profile -> ServerResponse.ok().bodyValue(profile));
    }

    public Mono<ServerResponse> getProducerProfile(ServerRequest request) {
        return userId(request)
                .flatMap(getProducerProfileUseCase::execute)
                .flatMap(profile -> ServerResponse.ok().bodyValue(profile));
    }

    public Mono<ServerResponse> patchProducerProfile(ServerRequest request) {
        return userId(request)
                .flatMap(uid -> request.bodyToMono(ProducerProfilePatch.class)
                        .flatMap(body -> updateProducerProfileUseCase.execute(uid,
                                new UpdateProducerProfileUseCase.Command(
                                        body.bio(), body.city(), body.department(), body.avatarInitials()))))
                .flatMap(profile -> ServerResponse.ok().bodyValue(profile));
    }

    private Mono<UUID> userId(ServerRequest request) {
        return request.principal().map(p -> UUID.fromString(p.getName()));
    }
}

package co.com.marketplace.usecase.identity;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.identity.BuyerProfile;
import co.com.marketplace.model.identity.User;
import co.com.marketplace.model.identity.gateways.BuyerProfileGateway;
import co.com.marketplace.model.identity.gateways.UserGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public final class GetBuyerProfileUseCase {

    private final UserGateway userGateway;
    private final BuyerProfileGateway buyerProfileGateway;

    public record Result(UUID id, String email, String fullName, String phone,
                         String city, String department, String preferredPayment,
                         boolean newsletterOptIn, String avatarInitials) {}

    public Mono<Result> execute(UUID userId) {
        return Mono.zip(
                userGateway.findById(userId)
                        .switchIfEmpty(Mono.error(new NotFoundException("USER_NOT_FOUND", "Usuario no encontrado"))),
                buyerProfileGateway.findByUserId(userId)
                        .switchIfEmpty(Mono.just(BuyerProfile.builder().build()))
        ).map(t -> {
            User u = t.getT1();
            BuyerProfile p = t.getT2();
            return new Result(u.getId(), u.getEmail(), u.getFullName(), u.getPhone(),
                    p.getCity(), p.getDepartment(), p.getPreferredPayment(),
                    p.isNewsletterOptIn(), p.getAvatarInitials());
        });
    }
}

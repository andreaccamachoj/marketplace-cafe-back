package co.com.marketplace.usecase.identity;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.identity.BuyerProfile;
import co.com.marketplace.model.identity.gateways.BuyerProfileGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public final class UpdateBuyerProfileUseCase {

    private final BuyerProfileGateway buyerProfileGateway;

    public record Command(String city, String department, String preferredPayment,
                          Boolean newsletterOptIn, String avatarInitials) {}

    public Mono<BuyerProfile> execute(UUID userId, Command cmd) {
        return buyerProfileGateway.findByUserId(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("PROFILE_NOT_FOUND",
                        "Perfil de comprador no encontrado")))
                .flatMap(profile -> buyerProfileGateway.update(profile.toBuilder()
                        .city(cmd.city() != null ? cmd.city() : profile.getCity())
                        .department(cmd.department() != null ? cmd.department() : profile.getDepartment())
                        .preferredPayment(cmd.preferredPayment() != null ? cmd.preferredPayment() : profile.getPreferredPayment())
                        .newsletterOptIn(cmd.newsletterOptIn() != null ? cmd.newsletterOptIn() : profile.isNewsletterOptIn())
                        .avatarInitials(cmd.avatarInitials() != null ? cmd.avatarInitials() : profile.getAvatarInitials())
                        .updatedAt(OffsetDateTime.now())
                        .build()));
    }
}

package co.com.marketplace.usecase.identity;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.identity.BuyerProfile;
import co.com.marketplace.model.identity.User;
import co.com.marketplace.model.identity.gateways.BuyerProfileGateway;
import co.com.marketplace.model.identity.gateways.UserGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public final class UpdateBuyerProfileUseCase {

    private final UserGateway userGateway;
    private final BuyerProfileGateway buyerProfileGateway;

    public record Command(String fullName, String phone, String city, String department,
                          String preferredPayment, Boolean newsletterOptIn, String avatarInitials) {}

    public record Result(UUID id, String email, String fullName, String phone,
                         String city, String department, String preferredPayment,
                         boolean newsletterOptIn, String avatarInitials) {}

    public Mono<Result> execute(UUID userId, Command cmd) {
        Mono<User> updateUser = userGateway.findById(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("USER_NOT_FOUND", "Usuario no encontrado")))
                .flatMap(user -> userGateway.update(user.toBuilder()
                        .fullName(cmd.fullName() != null ? cmd.fullName() : user.getFullName())
                        .phone(cmd.phone() != null ? cmd.phone() : user.getPhone())
                        .updatedAt(OffsetDateTime.now())
                        .build()));

        Mono<BuyerProfile> updateProfile = buyerProfileGateway.findByUserId(userId)
                .switchIfEmpty(Mono.just(BuyerProfile.builder().userId(userId).build()))
                .flatMap(profile -> buyerProfileGateway.update(profile.toBuilder()
                        .city(cmd.city() != null ? cmd.city() : profile.getCity())
                        .department(cmd.department() != null ? cmd.department() : profile.getDepartment())
                        .preferredPayment(cmd.preferredPayment() != null ? cmd.preferredPayment() : profile.getPreferredPayment())
                        .newsletterOptIn(cmd.newsletterOptIn() != null ? cmd.newsletterOptIn() : profile.isNewsletterOptIn())
                        .avatarInitials(cmd.avatarInitials() != null ? cmd.avatarInitials() : profile.getAvatarInitials())
                        .updatedAt(OffsetDateTime.now())
                        .build()));

        return Mono.zip(updateUser, updateProfile)
                .map(t -> {
                    User u = t.getT1();
                    BuyerProfile p = t.getT2();
                    return new Result(u.getId(), u.getEmail(), u.getFullName(), u.getPhone(),
                            p.getCity(), p.getDepartment(), p.getPreferredPayment(),
                            p.isNewsletterOptIn(), p.getAvatarInitials());
                });
    }
}

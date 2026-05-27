package co.com.marketplace.usecase.identity;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.identity.ProducerProfile;
import co.com.marketplace.model.identity.User;
import co.com.marketplace.model.identity.gateways.ProducerProfileGateway;
import co.com.marketplace.model.identity.gateways.UserGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public final class UpdateProducerProfileUseCase {

    private final UserGateway userGateway;
    private final ProducerProfileGateway producerProfileGateway;

    public record Command(String fullName, String phone, String bio, String city,
                          String department, String avatarInitials) {}

    public record Result(UUID id, String email, String fullName, String phone,
                         String bio, String city, String department,
                         String status, String avatarInitials) {}

    public Mono<Result> execute(UUID userId, Command cmd) {
        Mono<User> updateUser = userGateway.findById(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("USER_NOT_FOUND", "Usuario no encontrado")))
                .flatMap(user -> userGateway.update(user.toBuilder()
                        .fullName(cmd.fullName() != null ? cmd.fullName() : user.getFullName())
                        .phone(cmd.phone() != null ? cmd.phone() : user.getPhone())
                        .updatedAt(OffsetDateTime.now())
                        .build()));

        Mono<ProducerProfile> updateProfile = producerProfileGateway.findByUserId(userId)
                .switchIfEmpty(Mono.just(ProducerProfile.builder().userId(userId).build()))
                .flatMap(profile -> producerProfileGateway.update(profile.toBuilder()
                        .bio(cmd.bio() != null ? cmd.bio() : profile.getBio())
                        .city(cmd.city() != null ? cmd.city() : profile.getCity())
                        .department(cmd.department() != null ? cmd.department() : profile.getDepartment())
                        .avatarInitials(cmd.avatarInitials() != null ? cmd.avatarInitials() : profile.getAvatarInitials())
                        .updatedAt(OffsetDateTime.now())
                        .build()));

        return Mono.zip(updateUser, updateProfile)
                .map(t -> {
                    User u = t.getT1();
                    ProducerProfile p = t.getT2();
                    return new Result(u.getId(), u.getEmail(), u.getFullName(), u.getPhone(),
                            p.getBio(), p.getCity(), p.getDepartment(),
                            p.getStatus() != null ? p.getStatus().name() : null,
                            p.getAvatarInitials());
                });
    }
}

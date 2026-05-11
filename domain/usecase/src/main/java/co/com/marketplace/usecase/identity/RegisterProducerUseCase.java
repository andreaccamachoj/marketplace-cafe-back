package co.com.marketplace.usecase.identity;

import co.com.marketplace.model.admin.ProducerApproval;
import co.com.marketplace.model.admin.gateways.ProducerApprovalGateway;
import co.com.marketplace.model.cart.Cart;
import co.com.marketplace.model.cart.gateways.CartGateway;
import co.com.marketplace.model.exception.ConflictException;
import co.com.marketplace.model.gateway.PasswordEncoderGateway;
import co.com.marketplace.model.gateway.TokenProviderGateway;
import co.com.marketplace.model.identity.AuthTokens;
import co.com.marketplace.model.identity.ProducerProfile;
import co.com.marketplace.model.identity.ProducerStatus;
import co.com.marketplace.model.identity.User;
import co.com.marketplace.model.identity.UserStatus;
import co.com.marketplace.model.identity.gateways.ProducerProfileGateway;
import co.com.marketplace.model.identity.gateways.RoleGateway;
import co.com.marketplace.model.identity.gateways.UserGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@RequiredArgsConstructor
public final class RegisterProducerUseCase {

    private final UserGateway userGateway;
    private final RoleGateway roleGateway;
    private final ProducerProfileGateway producerProfileGateway;
    private final CartGateway cartGateway;
    private final ProducerApprovalGateway approvalGateway;
    private final PasswordEncoderGateway passwordEncoder;
    private final TokenProviderGateway tokenProvider;

    public record Command(String email, String rawPassword, String fullName, String phone,
                          String bio, String city, String department) {}

    public Mono<AuthTokens> execute(Command cmd) {
        return userGateway.existsByEmail(cmd.email())
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new ConflictException("AUTH_EMAIL_TAKEN",
                        "El correo ya está registrado")))
                .then(Mono.fromCallable(() -> passwordEncoder.encode(cmd.rawPassword())))
                .flatMap(hash -> userGateway.save(User.builder()
                        .email(cmd.email())
                        .hashedPassword(hash)
                        .fullName(cmd.fullName())
                        .phone(cmd.phone())
                        .status(UserStatus.active)
                        .privacyConsent(false)
                        .createdAt(OffsetDateTime.now())
                        .updatedAt(OffsetDateTime.now())
                        .build()))
                .flatMap(user -> roleGateway.findByName("PRODUCER")
                        .flatMap(role -> roleGateway.assignRoleToUser(user.getId(), role.getId()))
                        .then(producerProfileGateway.save(ProducerProfile.builder()
                                .userId(user.getId())
                                .bio(cmd.bio())
                                .city(cmd.city())
                                .department(cmd.department())
                                .status(ProducerStatus.pending)
                                .createdAt(OffsetDateTime.now())
                                .updatedAt(OffsetDateTime.now())
                                .build()))
                        .flatMap(profile -> approvalGateway.save(ProducerApproval.builder()
                                .producerId(user.getId())
                                .producerNameSnapshot(user.getFullName())
                                .email(user.getEmail())
                                .phone(user.getPhone())
                                .status(ProducerStatus.pending)
                                .submittedAt(OffsetDateTime.now())
                                .build())
                                .then(cartGateway.save(Cart.builder()
                                        .userId(user.getId())
                                        .createdAt(OffsetDateTime.now())
                                        .updatedAt(OffsetDateTime.now())
                                        .build())))
                        .thenReturn(new AuthTokens(
                                tokenProvider.generateAccessToken(user.getId(), user.getEmail(), "PRODUCER"),
                                tokenProvider.generateRefreshToken(user.getId())
                        )));
    }
}

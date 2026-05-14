package co.com.marketplace.usecase.identity;

import co.com.marketplace.model.cart.Cart;
import co.com.marketplace.model.cart.gateways.CartGateway;
import co.com.marketplace.model.exception.ConflictException;
import co.com.marketplace.model.gateway.PasswordEncoderGateway;
import co.com.marketplace.model.gateway.TokenProviderGateway;
import co.com.marketplace.model.identity.BuyerProfile;
import co.com.marketplace.model.identity.Role;
import co.com.marketplace.model.identity.User;
import co.com.marketplace.model.identity.UserStatus;
import co.com.marketplace.model.identity.gateways.BuyerProfileGateway;
import co.com.marketplace.model.identity.gateways.RoleGateway;
import co.com.marketplace.model.identity.gateways.UserGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterBuyerUseCaseTest {

    @Mock private UserGateway userGateway;
    @Mock private RoleGateway roleGateway;
    @Mock private BuyerProfileGateway buyerProfileGateway;
    @Mock private CartGateway cartGateway;
    @Mock private PasswordEncoderGateway passwordEncoder;
    @Mock private TokenProviderGateway tokenProvider;

    @InjectMocks
    private RegisterBuyerUseCase registerBuyerUseCase;

    private final UUID userId = UUID.randomUUID();

    private RegisterBuyerUseCase.Command buildCommand() {
        return new RegisterBuyerUseCase.Command("new@example.com", "pass123", "New User", "555");
    }

    @Test
    void execute_returnsTokens_whenEmailAvailable() {
        User savedUser = User.builder()
                .id(userId).email("new@example.com").hashedPassword("hashed")
                .fullName("New User").phone("555").status(UserStatus.active)
                .privacyConsent(false).createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now())
                .build();
        Role role = Role.builder().id(1).name("BUYER").description("Buyer").build();

        when(userGateway.existsByEmail("new@example.com")).thenReturn(Mono.just(false));
        when(passwordEncoder.encode("pass123")).thenReturn("hashed");
        when(userGateway.save(any())).thenReturn(Mono.just(savedUser));
        when(roleGateway.findByName("BUYER")).thenReturn(Mono.just(role));
        when(roleGateway.assignRoleToUser(userId, 1)).thenReturn(Mono.empty());
        when(buyerProfileGateway.save(any())).thenReturn(Mono.just(BuyerProfile.builder()
                .userId(userId).newsletterOptIn(false)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build()));
        when(cartGateway.save(any())).thenReturn(Mono.just(Cart.builder()
                .userId(userId).createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build()));
        when(tokenProvider.generateAccessToken(userId, "new@example.com", "BUYER")).thenReturn("access");
        when(tokenProvider.generateRefreshToken(userId)).thenReturn("refresh");

        StepVerifier.create(registerBuyerUseCase.execute(buildCommand()))
                .expectNextMatches(t -> "access".equals(t.accessToken()) && "refresh".equals(t.refreshToken()))
                .verifyComplete();
    }

    @Test
    void execute_throwsConflict_whenEmailAlreadyTaken() {
        when(userGateway.existsByEmail("new@example.com")).thenReturn(Mono.just(true));

        StepVerifier.create(registerBuyerUseCase.execute(buildCommand()))
                .verifyError(ConflictException.class);
    }
}

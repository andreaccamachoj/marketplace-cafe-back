package co.com.marketplace.usecase.identity;

import co.com.marketplace.model.admin.ProducerApproval;
import co.com.marketplace.model.admin.gateways.ProducerApprovalGateway;
import co.com.marketplace.model.cart.Cart;
import co.com.marketplace.model.cart.gateways.CartGateway;
import co.com.marketplace.model.exception.ConflictException;
import co.com.marketplace.model.gateway.PasswordEncoderGateway;
import co.com.marketplace.model.gateway.TokenProviderGateway;
import co.com.marketplace.model.identity.ProducerProfile;
import co.com.marketplace.model.identity.Role;
import co.com.marketplace.model.identity.User;
import co.com.marketplace.model.identity.UserStatus;
import co.com.marketplace.model.identity.gateways.ProducerProfileGateway;
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
class RegisterProducerUseCaseTest {

    @Mock private UserGateway userGateway;
    @Mock private RoleGateway roleGateway;
    @Mock private ProducerProfileGateway producerProfileGateway;
    @Mock private CartGateway cartGateway;
    @Mock private ProducerApprovalGateway approvalGateway;
    @Mock private PasswordEncoderGateway passwordEncoder;
    @Mock private TokenProviderGateway tokenProvider;

    @InjectMocks
    private RegisterProducerUseCase useCase;

    private final UUID userId = UUID.randomUUID();

    private RegisterProducerUseCase.Command cmd() {
        return new RegisterProducerUseCase.Command("p@e.com", "pass", "Producer", "3", "bio", "Medellín", "Antioquia");
    }

    @Test
    void execute_returnsTokens_whenEmailAvailable() {
        User saved = User.builder().id(userId).email("p@e.com").fullName("Producer").phone("3")
                .status(UserStatus.active).privacyConsent(false)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
        Role role = Role.builder().id(2).name("PRODUCER").description("Producer").build();
        ProducerProfile profile = ProducerProfile.builder().userId(userId).build();
        Cart cart = Cart.builder().userId(userId).createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();

        when(userGateway.existsByEmail("p@e.com")).thenReturn(Mono.just(false));
        when(passwordEncoder.encode("pass")).thenReturn("hashed");
        when(userGateway.save(any())).thenReturn(Mono.just(saved));
        when(roleGateway.findByName("PRODUCER")).thenReturn(Mono.just(role));
        when(roleGateway.assignRoleToUser(userId, 2)).thenReturn(Mono.empty());
        when(producerProfileGateway.save(any())).thenReturn(Mono.just(profile));
        when(approvalGateway.save(any(ProducerApproval.class))).thenReturn(Mono.just(ProducerApproval.builder().build()));
        when(cartGateway.save(any(Cart.class))).thenReturn(Mono.just(cart));
        when(tokenProvider.generateAccessToken(userId, "p@e.com", "PRODUCER")).thenReturn("at");
        when(tokenProvider.generateRefreshToken(userId)).thenReturn("rt");

        StepVerifier.create(useCase.execute(cmd()))
                .expectNextMatches(t -> "at".equals(t.accessToken()))
                .verifyComplete();
    }

    @Test
    void execute_throwsConflict_whenEmailTaken() {
        when(userGateway.existsByEmail("p@e.com")).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.execute(cmd()))
                .verifyError(ConflictException.class);
    }
}

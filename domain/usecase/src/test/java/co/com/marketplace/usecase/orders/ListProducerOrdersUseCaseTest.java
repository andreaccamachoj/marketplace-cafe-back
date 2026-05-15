package co.com.marketplace.usecase.orders;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.identity.ProducerProfile;
import co.com.marketplace.model.identity.gateways.ProducerProfileGateway;
import co.com.marketplace.model.orders.Order;
import co.com.marketplace.model.orders.OrderStatus;
import co.com.marketplace.model.orders.gateways.OrderGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListProducerOrdersUseCaseTest {

    @Mock private OrderGateway orderGateway;
    @Mock private ProducerProfileGateway producerProfileGateway;

    @InjectMocks
    private ListProducerOrdersUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID profileId = UUID.randomUUID();

    @Test
    void execute_returnsOrders_whenProfileFound() {
        ProducerProfile profile = ProducerProfile.builder().id(profileId).userId(userId).build();
        Order order = Order.builder().id(UUID.randomUUID()).buyerId(UUID.randomUUID()).code("WCM-2026-001")
                .yearlySequence(1).year(2026).subtotal(BigDecimal.TEN)
                .shippingAmount(BigDecimal.ZERO).discountAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.TEN).status(OrderStatus.confirmed)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();

        when(producerProfileGateway.findByUserId(userId)).thenReturn(Mono.just(profile));
        when(orderGateway.findByProducerId(profileId, null, 0, 10)).thenReturn(Flux.just(order));

        StepVerifier.create(useCase.execute(userId, null, 0, 10))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void execute_throwsNotFound_whenProfileMissing() {
        when(producerProfileGateway.findByUserId(userId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(userId, null, 0, 10))
                .verifyError(NotFoundException.class);
    }
}

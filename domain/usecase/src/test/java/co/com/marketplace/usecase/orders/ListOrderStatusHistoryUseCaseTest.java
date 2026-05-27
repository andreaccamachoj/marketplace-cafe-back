package co.com.marketplace.usecase.orders;

import co.com.marketplace.model.orders.OrderStatus;
import co.com.marketplace.model.orders.OrderStatusHistory;
import co.com.marketplace.model.orders.gateways.OrderStatusHistoryGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListOrderStatusHistoryUseCaseTest {

    @Mock private OrderStatusHistoryGateway orderStatusHistoryGateway;

    @InjectMocks
    private ListOrderStatusHistoryUseCase useCase;

    private final UUID orderId = UUID.randomUUID();

    @Test
    void execute_returnsHistory() {
        OrderStatusHistory entry = OrderStatusHistory.builder().id(UUID.randomUUID())
                .orderId(orderId).status(OrderStatus.confirmed).changedBy(UUID.randomUUID())
                .changedAt(OffsetDateTime.now()).build();
        when(orderStatusHistoryGateway.findByOrderId(orderId)).thenReturn(Flux.just(entry));

        StepVerifier.create(useCase.execute(orderId))
                .expectNextMatches(h -> orderId.equals(h.getOrderId()))
                .verifyComplete();
    }

    @Test
    void execute_returnsEmpty_whenNoHistory() {
        when(orderStatusHistoryGateway.findByOrderId(orderId)).thenReturn(Flux.empty());

        StepVerifier.create(useCase.execute(orderId))
                .verifyComplete();
    }
}

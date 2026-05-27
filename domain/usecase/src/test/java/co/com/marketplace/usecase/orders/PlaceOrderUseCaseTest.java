package co.com.marketplace.usecase.orders;

import co.com.marketplace.model.cart.Cart;
import co.com.marketplace.model.cart.CartItem;
import co.com.marketplace.model.cart.gateways.CartGateway;
import co.com.marketplace.model.catalog.Product;
import co.com.marketplace.model.catalog.ProductStatus;
import co.com.marketplace.model.catalog.gateways.ProductGateway;
import co.com.marketplace.model.exception.ValidationException;
import co.com.marketplace.model.orders.Order;
import co.com.marketplace.model.orders.OrderStatus;
import co.com.marketplace.model.orders.OrderStatusHistory;
import co.com.marketplace.model.orders.gateways.OrderGateway;
import co.com.marketplace.model.orders.gateways.OrderStatusHistoryGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaceOrderUseCaseTest {

    @Mock private CartGateway cartGateway;
    @Mock private OrderGateway orderGateway;
    @Mock private OrderStatusHistoryGateway orderStatusHistoryGateway;
    @Mock private ProductGateway productGateway;

    @InjectMocks
    private PlaceOrderUseCase useCase;

    private final UUID buyerId = UUID.randomUUID();
    private final UUID cartId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final UUID addressId = UUID.randomUUID();

    @Test
    void execute_placesOrder_whenCartHasItems() {
        CartItem cartItem = CartItem.builder().id(UUID.randomUUID()).cartId(cartId)
                .productId(productId).quantity(2).unitPriceSnapshot(BigDecimal.TEN)
                .addedAt(OffsetDateTime.now()).build();
        Cart cart = Cart.builder().id(cartId).userId(buyerId).items(List.of(cartItem))
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
        Product product = Product.builder().id(productId).name("Café").price(BigDecimal.TEN)
                .status(ProductStatus.active).soldCount(0).emoji("☕")
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
        Order savedOrder = Order.builder().id(UUID.randomUUID()).buyerId(buyerId)
                .addressId(addressId).code("WCM-2026-001").yearlySequence(1).year(2026)
                .subtotal(BigDecimal.valueOf(20)).shippingAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO).totalAmount(BigDecimal.valueOf(20))
                .status(OrderStatus.pending_verification).items(List.of())
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();

        when(cartGateway.findByUserId(buyerId)).thenReturn(Mono.just(cart));
        when(orderGateway.nextYearlySequence(anyInt())).thenReturn(Mono.just(1));
        when(productGateway.findById(productId)).thenReturn(Mono.just(product));
        when(orderGateway.save(any())).thenReturn(Mono.just(savedOrder));
        when(cartGateway.clearItems(cartId)).thenReturn(Mono.empty());
        when(orderStatusHistoryGateway.save(any(OrderStatusHistory.class))).thenReturn(Mono.just(OrderStatusHistory.builder()
                .id(UUID.randomUUID()).orderId(savedOrder.getId()).status(OrderStatus.pending_verification)
                .changedBy(buyerId).changedAt(OffsetDateTime.now()).build()));

        PlaceOrderUseCase.Command cmd = new PlaceOrderUseCase.Command(buyerId, addressId, "std", "transfer", null);

        StepVerifier.create(useCase.execute(cmd))
                .expectNextMatches(o -> buyerId.equals(o.getBuyerId()))
                .verifyComplete();
    }

    @Test
    void execute_throwsValidation_whenCartEmpty() {
        Cart cart = Cart.builder().id(cartId).userId(buyerId).items(List.of())
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();

        when(cartGateway.findByUserId(buyerId)).thenReturn(Mono.just(cart));

        PlaceOrderUseCase.Command cmd = new PlaceOrderUseCase.Command(buyerId, addressId, "std", "transfer", null);

        StepVerifier.create(useCase.execute(cmd))
                .verifyError(ValidationException.class);
    }
}

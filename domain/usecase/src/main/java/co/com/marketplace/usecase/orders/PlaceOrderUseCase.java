package co.com.marketplace.usecase.orders;

import co.com.marketplace.model.cart.CartItem;
import co.com.marketplace.model.cart.gateways.CartGateway;
import co.com.marketplace.model.exception.ValidationException;
import co.com.marketplace.model.orders.Order;
import co.com.marketplace.model.orders.OrderItem;
import co.com.marketplace.model.orders.OrderStatus;
import co.com.marketplace.model.orders.OrderStatusHistory;
import co.com.marketplace.model.orders.gateways.OrderGateway;
import co.com.marketplace.model.orders.gateways.OrderStatusHistoryGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public final class PlaceOrderUseCase {

    private final CartGateway cartGateway;
    private final OrderGateway orderGateway;
    private final OrderStatusHistoryGateway orderStatusHistoryGateway;

    public record Command(
            UUID buyerId,
            UUID addressId,
            String shippingOptionId,
            String paymentMethodCode,
            String notes
    ) {}

    public Mono<Order> execute(Command cmd) {
        return cartGateway.findByUserId(cmd.buyerId())
                .flatMap(cart -> {
                    List<CartItem> items = cart.getItems();
                    if (items == null || items.isEmpty()) {
                        return Mono.error(new ValidationException("CART_IS_EMPTY", "Cart is empty"));
                    }
                    int year = OffsetDateTime.now().getYear();
                    return orderGateway.nextYearlySequence(year)
                            .flatMap(seq -> {
                                String code = String.format("WCM-%d-%03d", year, seq);
                                BigDecimal subtotal = items.stream()
                                        .map(i -> i.getUnitPriceSnapshot()
                                                .multiply(BigDecimal.valueOf(i.getQuantity())))
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                Order order = Order.builder()
                                        .id(UUID.randomUUID())
                                        .buyerId(cmd.buyerId())
                                        .addressId(cmd.addressId())
                                        .shippingOptionId(cmd.shippingOptionId())
                                        .couponId(cart.getCouponId())
                                        .code(code)
                                        .yearlySequence(seq)
                                        .year(year)
                                        .subtotal(subtotal)
                                        .shippingAmount(BigDecimal.ZERO)
                                        .discountAmount(BigDecimal.ZERO)
                                        .totalAmount(subtotal)
                                        .status(OrderStatus.pending_verification)
                                        .createdAt(OffsetDateTime.now())
                                        .updatedAt(OffsetDateTime.now())
                                        .build();
                                return orderGateway.save(order)
                                        .flatMap(saved -> saveOrderItems(saved, items)
                                                .then(cartGateway.clearItems(cart.getId()))
                                                .then(saveInitialHistory(saved, cmd.buyerId()))
                                                .thenReturn(saved));
                            });
                });
    }

    private Mono<Void> saveOrderItems(Order order, List<CartItem> items) {
        return Flux.fromIterable(items)
                .flatMap(item -> {
                    OrderItem orderItem = OrderItem.builder()
                            .id(UUID.randomUUID())
                            .orderId(order.getId())
                            .productId(item.getProductId())
                            .productNameSnapshot("")
                            .productEmojiSnapshot("")
                            .quantity(item.getQuantity())
                            .unitPriceSnapshot(item.getUnitPriceSnapshot())
                            .subtotal(item.getUnitPriceSnapshot()
                                    .multiply(BigDecimal.valueOf(item.getQuantity())))
                            .build();
                    return Mono.just(orderItem);
                })
                .then();
    }

    private Mono<Void> saveInitialHistory(Order order, UUID changedBy) {
        OrderStatusHistory history = OrderStatusHistory.builder()
                .id(UUID.randomUUID())
                .orderId(order.getId())
                .status(order.getStatus())
                .changedBy(changedBy)
                .notes("Order placed")
                .changedAt(OffsetDateTime.now())
                .build();
        return orderStatusHistoryGateway.save(history).then();
    }
}

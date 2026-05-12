package co.com.marketplace.usecase.orders;

import co.com.marketplace.model.cart.CartItem;
import co.com.marketplace.model.cart.gateways.CartGateway;
import co.com.marketplace.model.catalog.gateways.ProductGateway;
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
    private final ProductGateway productGateway;

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
                            .flatMap(seq -> buildOrderItems(items)
                                    .flatMap(orderItems -> {
                                        String code = String.format("WCM-%d-%03d", year, seq);
                                        BigDecimal subtotal = orderItems.stream()
                                                .map(OrderItem::getSubtotal)
                                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        Order order = Order.builder()
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
                                                .items(orderItems)
                                                .createdAt(OffsetDateTime.now())
                                                .updatedAt(OffsetDateTime.now())
                                                .build();
                                        return orderGateway.save(order)
                                                .flatMap(saved -> cartGateway.clearItems(cart.getId())
                                                        .then(saveInitialHistory(saved, cmd.buyerId()))
                                                        .thenReturn(saved));
                                    }));
                });
    }

    private Mono<List<OrderItem>> buildOrderItems(List<CartItem> cartItems) {
        return Flux.fromIterable(cartItems)
                .flatMap(i -> productGateway.findById(i.getProductId())
                        .map(p -> OrderItem.builder()
                                .productId(i.getProductId())
                                .productNameSnapshot(p.getName() != null ? p.getName() : "")
                                .productEmojiSnapshot(p.getEmoji() != null ? p.getEmoji() : "")
                                .quantity(i.getQuantity())
                                .unitPriceSnapshot(i.getUnitPriceSnapshot())
                                .subtotal(i.getUnitPriceSnapshot().multiply(BigDecimal.valueOf(i.getQuantity())))
                                .build())
                        .onErrorReturn(OrderItem.builder()
                                .productId(i.getProductId())
                                .productNameSnapshot("")
                                .productEmojiSnapshot("")
                                .quantity(i.getQuantity())
                                .unitPriceSnapshot(i.getUnitPriceSnapshot())
                                .subtotal(i.getUnitPriceSnapshot().multiply(BigDecimal.valueOf(i.getQuantity())))
                                .build()))
                .collectList();
    }

    private Mono<Void> saveInitialHistory(Order order, UUID changedBy) {
        OrderStatusHistory history = OrderStatusHistory.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .changedBy(changedBy)
                .notes("Order placed")
                .changedAt(OffsetDateTime.now())
                .build();
        return orderStatusHistoryGateway.save(history).then();
    }
}

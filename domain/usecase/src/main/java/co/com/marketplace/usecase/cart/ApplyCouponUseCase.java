package co.com.marketplace.usecase.cart;

import co.com.marketplace.model.cart.Cart;
import co.com.marketplace.model.cart.Coupon;
import co.com.marketplace.model.cart.gateways.CartGateway;
import co.com.marketplace.model.cart.gateways.CouponGateway;
import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public final class ApplyCouponUseCase {

    private final CartGateway cartGateway;
    private final CouponGateway couponGateway;

    public Mono<Cart> execute(UUID userId, String couponCode) {
        return couponGateway.findByCode(couponCode)
                .switchIfEmpty(Mono.error(new NotFoundException("COUPON_NOT_FOUND", "Coupon not found: " + couponCode)))
                .flatMap(coupon -> {
                    OffsetDateTime now = OffsetDateTime.now();
                    if (!coupon.isActive()) {
                        return Mono.error(new ValidationException("COUPON_NOT_ACTIVE", "Coupon is not active"));
                    }
                    if (coupon.getValidUntil() != null && now.isAfter(coupon.getValidUntil())) {
                        return Mono.error(new ValidationException("COUPON_EXPIRED", "Coupon has expired"));
                    }
                    if (coupon.getValidFrom() != null && now.isBefore(coupon.getValidFrom())) {
                        return Mono.error(new ValidationException("COUPON_NOT_YET_VALID", "Coupon is not yet valid"));
                    }
                    if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
                        return Mono.error(new ValidationException("COUPON_USAGE_LIMIT_EXCEEDED", "Coupon usage limit exceeded"));
                    }
                    return cartGateway.findByUserId(userId)
                            .flatMap(cart -> validateSubtotalAndApply(cart, coupon));
                });
    }

    private Mono<Cart> validateSubtotalAndApply(Cart cart, Coupon coupon) {
        if (coupon.getMinSubtotal() != null && cart.getItems() != null) {
            BigDecimal subtotal = cart.getItems().stream()
                    .map(i -> i.getUnitPriceSnapshot().multiply(BigDecimal.valueOf(i.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (subtotal.compareTo(coupon.getMinSubtotal()) < 0) {
                return Mono.error(new ValidationException(
                        "COUPON_MIN_SUBTOTAL_NOT_MET", "Minimum subtotal of " + coupon.getMinSubtotal() + " not met"));
            }
        }
        return cartGateway.applyCoupon(cart.getId(), coupon.getId())
                .then(couponGateway.incrementUsage(coupon.getId()))
                .then(cartGateway.findByUserId(cart.getUserId()));
    }
}

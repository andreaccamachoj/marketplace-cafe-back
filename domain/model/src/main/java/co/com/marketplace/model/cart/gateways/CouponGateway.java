package co.com.marketplace.model.cart.gateways;

import co.com.marketplace.model.cart.Coupon;
import reactor.core.publisher.Mono;

public interface CouponGateway {
    Mono<Coupon> findByCode(String code);
    Mono<Void> incrementUsage(Integer couponId);
}

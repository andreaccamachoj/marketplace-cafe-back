package co.com.marketplace.r2dbc.cart;

import co.com.marketplace.model.cart.Coupon;
import co.com.marketplace.model.cart.CouponDiscountType;
import co.com.marketplace.model.cart.gateways.CouponGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponRepositoryAdapter implements CouponGateway {

    private final CouponReactiveRepository repo;
    private final DatabaseClient db;

    @Override
    public Mono<Coupon> findByCode(String code) {
        return repo.findByCode(code)
                .doOnSubscribe(s -> log.debug("[CouponRepositoryAdapter#findByCode] DB request: code={}", code))
                .doOnSuccess(r -> log.debug("[CouponRepositoryAdapter#findByCode] DB response: found={}", r != null))
                .doOnError(e -> log.error("[CouponRepositoryAdapter#findByCode] DB error: {}", e.getMessage()))
                .map(this::toDomain);
    }

    @Override
    public Mono<Void> incrementUsage(Integer couponId) {
        return db.sql("UPDATE marketplace.coupons SET used_count = used_count + 1 WHERE id = :id")
                .bind("id", couponId)
                .then()
                .doOnSubscribe(s -> log.debug("[CouponRepositoryAdapter#incrementUsage] DB request: couponId={}", couponId))
                .doOnTerminate(() -> log.debug("[CouponRepositoryAdapter#incrementUsage] DB response: done"))
                .doOnError(e -> log.error("[CouponRepositoryAdapter#incrementUsage] DB error: {}", e.getMessage()));
    }

    private Coupon toDomain(CouponData d) {
        return Coupon.builder()
                .id(d.getId())
                .code(d.getCode())
                .description(d.getDescription())
                .discountType(CouponDiscountType.valueOf(d.getDiscountType().name()))
                .discountValue(d.getDiscountValue())
                .minSubtotal(d.getMinSubtotal())
                .usageLimit(d.getUsageLimit())
                .usedCount(d.getUsedCount())
                .validFrom(d.getValidFrom())
                .validUntil(d.getValidUntil())
                .isActive(d.isActive())
                .build();
    }
}

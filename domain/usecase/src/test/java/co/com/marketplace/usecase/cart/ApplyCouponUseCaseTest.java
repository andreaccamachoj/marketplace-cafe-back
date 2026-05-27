package co.com.marketplace.usecase.cart;

import co.com.marketplace.model.cart.Cart;
import co.com.marketplace.model.cart.Coupon;
import co.com.marketplace.model.cart.CouponDiscountType;
import co.com.marketplace.model.cart.gateways.CartGateway;
import co.com.marketplace.model.cart.gateways.CouponGateway;
import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.exception.ValidationException;
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

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplyCouponUseCaseTest {

    @Mock private CartGateway cartGateway;
    @Mock private CouponGateway couponGateway;

    @InjectMocks
    private ApplyCouponUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID cartId = UUID.randomUUID();

    private Cart buildCart() {
        return Cart.builder().id(cartId).userId(userId).items(List.of())
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
    }

    private Coupon activeCoupon() {
        return Coupon.builder().id(1).code("SAVE10").discountType(CouponDiscountType.percentage)
                .discountValue(BigDecimal.TEN).isActive(true)
                .validFrom(OffsetDateTime.now().minusDays(1))
                .validUntil(OffsetDateTime.now().plusDays(10))
                .usageLimit(100).usedCount(5).build();
    }

    @Test
    void execute_appliesCoupon_whenValid() {
        Cart cart = buildCart();
        Coupon coupon = activeCoupon();

        when(couponGateway.findByCode("SAVE10")).thenReturn(Mono.just(coupon));
        when(cartGateway.findByUserId(userId)).thenReturn(Mono.just(cart));
        when(cartGateway.applyCoupon(cartId, 1)).thenReturn(Mono.empty());
        when(couponGateway.incrementUsage(1)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(userId, "SAVE10"))
                .expectNextMatches(c -> userId.equals(c.getUserId()))
                .verifyComplete();
    }

    @Test
    void execute_throwsNotFound_whenCouponMissing() {
        when(couponGateway.findByCode("INVALID")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(userId, "INVALID"))
                .verifyError(NotFoundException.class);
    }

    @Test
    void execute_throwsValidation_whenCouponInactive() {
        Coupon inactive = Coupon.builder().id(2).code("OLD").discountType(CouponDiscountType.fixed)
                .discountValue(BigDecimal.ONE).isActive(false).usedCount(0).build();
        when(couponGateway.findByCode("OLD")).thenReturn(Mono.just(inactive));

        StepVerifier.create(useCase.execute(userId, "OLD"))
                .verifyError(ValidationException.class);
    }

    @Test
    void execute_throwsValidation_whenCouponExpired() {
        Coupon expired = Coupon.builder().id(3).code("EXP").discountType(CouponDiscountType.fixed)
                .discountValue(BigDecimal.ONE).isActive(true)
                .validUntil(OffsetDateTime.now().minusDays(1))
                .usedCount(0).build();
        when(couponGateway.findByCode("EXP")).thenReturn(Mono.just(expired));

        StepVerifier.create(useCase.execute(userId, "EXP"))
                .verifyError(ValidationException.class);
    }
}

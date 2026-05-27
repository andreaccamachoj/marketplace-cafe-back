package co.com.marketplace.r2dbc.cart;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface CouponReactiveRepository extends ReactiveCrudRepository<CouponData, Integer> {
    Mono<CouponData> findByCode(String code);
}

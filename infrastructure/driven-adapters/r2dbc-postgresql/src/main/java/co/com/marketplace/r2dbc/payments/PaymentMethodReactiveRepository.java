package co.com.marketplace.r2dbc.payments;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PaymentMethodReactiveRepository extends ReactiveCrudRepository<PaymentMethodData, UUID> {
    @Query("SELECT * FROM marketplace.payment_methods WHERE is_active = TRUE ORDER BY display_order ASC")
    Flux<PaymentMethodData> findAllActive();

    Mono<PaymentMethodData> findByCode(String code);
}

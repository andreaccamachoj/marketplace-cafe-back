package co.com.marketplace.r2dbc.cart;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ShippingOptionReactiveRepository extends ReactiveCrudRepository<ShippingOptionData, String> {
}

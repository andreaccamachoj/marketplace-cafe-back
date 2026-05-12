package co.com.marketplace.usecase.orders;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.identity.gateways.ProducerProfileGateway;
import co.com.marketplace.model.orders.Order;
import co.com.marketplace.model.orders.OrderStatus;
import co.com.marketplace.model.orders.gateways.OrderGateway;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public final class ListProducerOrdersUseCase {

    private final OrderGateway orderGateway;
    private final ProducerProfileGateway producerProfileGateway;

    public ListProducerOrdersUseCase(OrderGateway orderGateway, ProducerProfileGateway producerProfileGateway) {
        this.orderGateway = orderGateway;
        this.producerProfileGateway = producerProfileGateway;
    }

    public Flux<Order> execute(UUID userId, OrderStatus status, int page, int size) {
        return producerProfileGateway.findByUserId(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("PRODUCER_PROFILE_NOT_FOUND",
                        "Perfil de productor no encontrado para usuario: " + userId)))
                .flatMapMany(profile -> orderGateway.findByProducerId(profile.getId(), status, page, size));
    }
}

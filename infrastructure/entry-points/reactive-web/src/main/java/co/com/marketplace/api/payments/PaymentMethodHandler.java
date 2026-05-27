package co.com.marketplace.api.payments;

import co.com.marketplace.usecase.payments.ListPaymentMethodsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class PaymentMethodHandler {

    private final ListPaymentMethodsUseCase listPaymentMethodsUseCase;

    public Mono<ServerResponse> list(ServerRequest request) {
        return listPaymentMethodsUseCase.execute()
                .collectList()
                .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }
}

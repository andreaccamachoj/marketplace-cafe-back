package co.com.marketplace.usecase.payments;

import co.com.marketplace.model.payments.PaymentMethod;
import co.com.marketplace.model.payments.gateways.PaymentMethodGateway;
import reactor.core.publisher.Flux;

public final class ListPaymentMethodsUseCase {

    private final PaymentMethodGateway paymentMethodGateway;

    public ListPaymentMethodsUseCase(PaymentMethodGateway paymentMethodGateway) {
        this.paymentMethodGateway = paymentMethodGateway;
    }

    public Flux<PaymentMethod> execute() {
        return paymentMethodGateway.findAllActive();
    }
}

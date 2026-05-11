package co.com.marketplace.api.orders;

import co.com.marketplace.model.orders.OrderStatus;
import co.com.marketplace.usecase.orders.CancelOrderUseCase;
import co.com.marketplace.usecase.orders.ConfirmOrderPaymentUseCase;
import co.com.marketplace.usecase.orders.GenerateInvoiceUseCase;
import co.com.marketplace.usecase.orders.GetOrderDetailUseCase;
import co.com.marketplace.usecase.orders.GetOrderPaymentDetailsUseCase;
import co.com.marketplace.usecase.orders.ListBuyerOrdersUseCase;
import co.com.marketplace.usecase.orders.ListOrderStatusHistoryUseCase;
import co.com.marketplace.usecase.orders.PlaceOrderUseCase;
import co.com.marketplace.usecase.payments.RegisterManualPaymentProofUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderHandler {

    private final PlaceOrderUseCase placeOrderUseCase;
    private final ListBuyerOrdersUseCase listBuyerOrdersUseCase;
    private final GetOrderDetailUseCase getOrderDetailUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final ListOrderStatusHistoryUseCase listOrderStatusHistoryUseCase;
    private final GenerateInvoiceUseCase generateInvoiceUseCase;
    private final RegisterManualPaymentProofUseCase registerManualPaymentProofUseCase;
    private final GetOrderPaymentDetailsUseCase getOrderPaymentDetailsUseCase;
    private final ConfirmOrderPaymentUseCase confirmOrderPaymentUseCase;
    private final TransactionalOperator tx;

    record PlaceOrderRequest(UUID addressId, String shippingOptionId, String paymentMethodCode, String notes) {}
    record CancelRequest(String reason) {}
    record PaymentProofRequest(String paymentMethodCode, BigDecimal amount, String reference, String proofUrl) {}
    record ConfirmPaymentRequest(boolean verified, String note) {}

    public Mono<ServerResponse> placeOrder(ServerRequest request) {
        return userId(request)
                .flatMap(uid -> request.bodyToMono(PlaceOrderRequest.class)
                        .flatMap(body -> placeOrderUseCase.execute(
                                new PlaceOrderUseCase.Command(uid, body.addressId(),
                                        body.shippingOptionId(), body.paymentMethodCode(), body.notes()))
                                .as(tx::transactional)))
                .flatMap(order -> ServerResponse.status(HttpStatus.CREATED).bodyValue(order));
    }

    public Mono<ServerResponse> listOrders(ServerRequest request) {
        OrderStatus status = request.queryParam("status")
                .map(s -> OrderStatus.valueOf(s)).orElse(null);
        int page = request.queryParam("page").map(Integer::parseInt).orElse(0);
        int size = request.queryParam("size").map(Integer::parseInt).orElse(20);
        return userId(request)
                .flatMapMany(uid -> listBuyerOrdersUseCase.execute(uid, status, page, size))
                .collectList()
                .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }

    public Mono<ServerResponse> getOrder(ServerRequest request) {
        UUID orderId = UUID.fromString(request.pathVariable("id"));
        return getOrderDetailUseCase.execute(orderId)
                .flatMap(order -> ServerResponse.ok().bodyValue(order));
    }

    public Mono<ServerResponse> cancelOrder(ServerRequest request) {
        UUID orderId = UUID.fromString(request.pathVariable("id"));
        return userId(request)
                .flatMap(uid -> request.bodyToMono(CancelRequest.class)
                        .flatMap(body -> cancelOrderUseCase.execute(orderId, uid, body.reason())
                                .as(tx::transactional)))
                .flatMap(order -> ServerResponse.ok().bodyValue(order));
    }

    public Mono<ServerResponse> getTimeline(ServerRequest request) {
        UUID orderId = UUID.fromString(request.pathVariable("id"));
        return listOrderStatusHistoryUseCase.execute(orderId)
                .collectList()
                .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }

    public Mono<ServerResponse> getInvoice(ServerRequest request) {
        UUID orderId = UUID.fromString(request.pathVariable("id"));
        return generateInvoiceUseCase.execute(orderId)
                .flatMap(invoice -> ServerResponse.ok().bodyValue(invoice));
    }

    public Mono<ServerResponse> submitPaymentProof(ServerRequest request) {
        UUID orderId = UUID.fromString(request.pathVariable("id"));
        return request.bodyToMono(PaymentProofRequest.class)
                .flatMap(body -> registerManualPaymentProofUseCase.execute(
                        new RegisterManualPaymentProofUseCase.Command(
                                orderId, body.paymentMethodCode(), body.amount(),
                                body.reference(), body.proofUrl()))
                        .as(tx::transactional))
                .flatMap(payment -> ServerResponse.ok().bodyValue(payment));
    }

    public Mono<ServerResponse> getPayment(ServerRequest request) {
        UUID orderId = UUID.fromString(request.pathVariable("id"));
        return getOrderPaymentDetailsUseCase.execute(orderId)
                .flatMap(payment -> ServerResponse.ok().bodyValue(payment));
    }

    private Mono<UUID> userId(ServerRequest request) {
        return request.principal().map(p -> UUID.fromString(p.getName()));
    }
}

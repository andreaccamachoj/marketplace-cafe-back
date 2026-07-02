package co.com.marketplace.sqs;

import co.com.marketplace.model.orders.OrderStatusChangedEvent;
import co.com.marketplace.model.orders.gateways.OrderEventPublisherGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class SqsEventPublisherAdapter implements OrderEventPublisherGateway {

    private final SqsAsyncClient sqsAsyncClient;
    private final String queueUrl;
    private final ObjectMapper objectMapper;

    public SqsEventPublisherAdapter(SqsAsyncClient sqsAsyncClient,
                                    @Value("${aws.sqs.order-status-queue-url}") String queueUrl,
                                    ObjectMapper objectMapper) {
        this.sqsAsyncClient = sqsAsyncClient;
        this.queueUrl = queueUrl;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> publishStatusChanged(OrderStatusChangedEvent event) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(toPayload(event)))
                .flatMap(body -> Mono.fromFuture(() -> sqsAsyncClient.sendMessage(
                        SendMessageRequest.builder()
                                .queueUrl(queueUrl)
                                .messageBody(body)
                                .build())))
                .doOnSuccess(r -> log.debug("[SqsEventPublisherAdapter] published status change for order {}", event.orderId()))
                .doOnError(e -> log.error("[SqsEventPublisherAdapter] failed to publish order {} status change: {}",
                        event.orderId(), e.getMessage()))
                .then();
    }

    private Map<String, Object> toPayload(OrderStatusChangedEvent e) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("orderId", e.orderId() != null ? e.orderId().toString() : null);
        p.put("orderCode", e.orderCode());
        p.put("previousStatus", e.previousStatus() != null ? e.previousStatus().name() : null);
        p.put("newStatus", e.newStatus() != null ? e.newStatus().name() : null);
        p.put("buyerEmail", e.buyerEmail());
        p.put("buyerId", e.buyerId() != null ? e.buyerId().toString() : null);
        p.put("totalAmount", e.totalAmount());
        p.put("note", e.note());
        p.put("changedAt", e.changedAt() != null ? e.changedAt().toString() : null);
        return p;
    }
}

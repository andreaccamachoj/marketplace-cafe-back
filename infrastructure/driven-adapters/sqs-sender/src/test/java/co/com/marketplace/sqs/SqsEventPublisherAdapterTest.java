package co.com.marketplace.sqs;

import co.com.marketplace.model.orders.OrderStatus;
import co.com.marketplace.model.orders.OrderStatusChangedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SqsEventPublisherAdapterTest {

    @Mock
    private SqsAsyncClient sqsAsyncClient;

    private SqsEventPublisherAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new SqsEventPublisherAdapter(sqsAsyncClient, "https://sqs/queue", JsonMapper.builder().build());
    }

    private OrderStatusChangedEvent sampleEvent() {
        return OrderStatusChangedEvent.builder()
                .orderId(UUID.randomUUID())
                .orderCode("WCM-2026-001")
                .previousStatus(OrderStatus.confirmed)
                .newStatus(OrderStatus.preparing)
                .buyerEmail("buyer@example.com")
                .buyerId(UUID.randomUUID())
                .totalAmount(BigDecimal.valueOf(85000))
                .note("En preparación")
                .changedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void publishStatusChanged_sendsJsonMessageToQueue() {
        when(sqsAsyncClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(SendMessageResponse.builder().build()));

        StepVerifier.create(adapter.publishStatusChanged(sampleEvent()))
                .verifyComplete();

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsAsyncClient).sendMessage(captor.capture());
        SendMessageRequest request = captor.getValue();
        assertEquals("https://sqs/queue", request.queueUrl());
        String body = request.messageBody();
        assertTrue(body.contains("\"orderCode\":\"WCM-2026-001\""), body);
        assertTrue(body.contains("\"previousStatus\":\"confirmed\""), body);
        assertTrue(body.contains("\"newStatus\":\"preparing\""), body);
        assertTrue(body.contains("\"buyerEmail\":\"buyer@example.com\""), body);
    }

    @Test
    void publishStatusChanged_propagatesError() {
        CompletableFuture<SendMessageResponse> failed = new CompletableFuture<>();
        failed.completeExceptionally(new RuntimeException("SQS down"));
        when(sqsAsyncClient.sendMessage(any(SendMessageRequest.class))).thenReturn(failed);

        StepVerifier.create(adapter.publishStatusChanged(sampleEvent()))
                .verifyError(RuntimeException.class);
    }
}

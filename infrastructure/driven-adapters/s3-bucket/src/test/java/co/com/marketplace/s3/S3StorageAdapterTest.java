package co.com.marketplace.s3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3StorageAdapterTest {

    @Mock
    private S3AsyncClient s3AsyncClient;

    private S3StorageAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new S3StorageAdapter(s3AsyncClient, "my-bucket", "us-east-1");
    }

    @Test
    void upload_putsObjectAndReturnsPublicUrl() {
        when(s3AsyncClient.putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class)))
                .thenReturn(CompletableFuture.completedFuture(PutObjectResponse.builder().build()));

        StepVerifier.create(adapter.upload(new byte[]{1, 2, 3}, "image/png", "products/abc/cover"))
                .expectNext("https://my-bucket.s3.us-east-1.amazonaws.com/products/abc/cover")
                .verifyComplete();

        verify(s3AsyncClient).putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class));
    }

    @Test
    void upload_propagatesError() {
        CompletableFuture<PutObjectResponse> failed = new CompletableFuture<>();
        failed.completeExceptionally(new RuntimeException("S3 down"));
        when(s3AsyncClient.putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class)))
                .thenReturn(failed);

        StepVerifier.create(adapter.upload(new byte[]{1}, "image/jpeg", "k"))
                .verifyError(RuntimeException.class);
    }

    @Test
    void delete_removesObject() {
        when(s3AsyncClient.deleteObject(any(DeleteObjectRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(DeleteObjectResponse.builder().build()));

        StepVerifier.create(adapter.delete("products/abc/cover"))
                .verifyComplete();

        verify(s3AsyncClient).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void delete_propagatesError() {
        CompletableFuture<DeleteObjectResponse> failed = new CompletableFuture<>();
        failed.completeExceptionally(new RuntimeException("S3 down"));
        when(s3AsyncClient.deleteObject(any(DeleteObjectRequest.class))).thenReturn(failed);

        StepVerifier.create(adapter.delete("k"))
                .verifyError(RuntimeException.class);
    }
}

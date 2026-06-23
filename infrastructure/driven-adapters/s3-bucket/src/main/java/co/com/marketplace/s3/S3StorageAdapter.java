package co.com.marketplace.s3;

import co.com.marketplace.model.catalog.gateways.ImageStorageGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
public class S3StorageAdapter implements ImageStorageGateway {

    private final S3AsyncClient s3AsyncClient;
    private final String bucket;
    private final String region;

    public S3StorageAdapter(S3AsyncClient s3AsyncClient,
                            @Value("${aws.s3.bucket}") String bucket,
                            @Value("${aws.region}") String region) {
        this.s3AsyncClient = s3AsyncClient;
        this.bucket = bucket;
        this.region = region;
    }

    @Override
    public Mono<String> upload(byte[] content, String contentType, String key) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .contentLength((long) content.length)
                .build();
        return Mono.fromFuture(() -> s3AsyncClient.putObject(request, AsyncRequestBody.fromBytes(content)))
                .thenReturn(publicUrl(key));
    }

    @Override
    public Mono<Void> delete(String key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        return Mono.fromFuture(() -> s3AsyncClient.deleteObject(request)).then();
    }

    private String publicUrl(String key) {
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }
}

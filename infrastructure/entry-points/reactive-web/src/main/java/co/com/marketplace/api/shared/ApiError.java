package co.com.marketplace.api.shared;

import java.time.OffsetDateTime;

public record ApiError(String code, String message, OffsetDateTime timestamp, String path) {

    public static ApiError of(String code, String message, String path) {
        return new ApiError(code, message, OffsetDateTime.now(), path);
    }
}

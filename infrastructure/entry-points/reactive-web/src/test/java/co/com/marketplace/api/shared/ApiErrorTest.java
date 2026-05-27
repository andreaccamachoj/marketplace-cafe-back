package co.com.marketplace.api.shared;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiErrorTest {

    @Test
    void of_createsApiError_withCorrectFields() {
        ApiError error = ApiError.of("CODE", "msg", "/path");
        assertEquals("CODE", error.code());
        assertEquals("msg", error.message());
        assertEquals("/path", error.path());
        assertNotNull(error.timestamp());
    }

    @Test
    void record_equality() {
        var t = java.time.OffsetDateTime.now();
        ApiError a = new ApiError("C", "m", t, "/p");
        ApiError b = new ApiError("C", "m", t, "/p");
        assertEquals(a, b);
    }
}

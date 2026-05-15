package co.com.marketplace.api.shared;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PagedResponseTest {

    @Test
    void of_createsPagedResponse_withCorrectFields() {
        List<String> items = List.of("a", "b");
        PagedResponse<String> paged = PagedResponse.of(items, 0, 10, 2L);
        assertEquals(items, paged.content());
        assertEquals(0, paged.page());
        assertEquals(10, paged.size());
        assertEquals(2L, paged.totalElements());
    }
}

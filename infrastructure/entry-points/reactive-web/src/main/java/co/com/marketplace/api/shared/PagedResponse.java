package co.com.marketplace.api.shared;

import java.util.List;

public record PagedResponse<T>(List<T> content, int page, int size, long totalElements) {

    public static <T> PagedResponse<T> of(List<T> content, int page, int size, long totalElements) {
        return new PagedResponse<>(content, page, size, totalElements);
    }
}

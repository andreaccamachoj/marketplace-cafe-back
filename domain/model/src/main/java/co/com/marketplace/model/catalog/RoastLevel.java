package co.com.marketplace.model.catalog;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RoastLevel {
    Integer id;
    String code;
    String name;
    String description;
    String icon;
}

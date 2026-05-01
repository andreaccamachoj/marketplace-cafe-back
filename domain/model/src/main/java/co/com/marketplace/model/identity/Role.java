package co.com.marketplace.model.identity;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Role {
    Integer id;
    String name;
    String description;
}

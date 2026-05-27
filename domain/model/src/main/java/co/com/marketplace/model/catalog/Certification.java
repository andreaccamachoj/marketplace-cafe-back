package co.com.marketplace.model.catalog;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Certification {
    Integer id;
    String code;
    String name;
    String issuingBody;
    String description;
}

package chetmine.marketplace.parser.dto.parser;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ProductPreview {
    private String name;
    private String brand;

    private double price;
    private Double oldPrice;

    private String link;
    private String imgUrl;

    private String deliveryDate;

    @JsonProperty("isAvailable")
    private boolean isAvailable;
    private String marketplace;
}

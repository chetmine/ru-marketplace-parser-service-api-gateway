package chetmine.marketplace.parser.dto.parser;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Product {
    private String name;
    private String brand;

    private double price;
    private Double oldPrice;

    private String link;
    private String imgUrl;

    @JsonProperty("isAvailable")
    private boolean isAvailable;
    private String marketplace;

    private String deliveryDate;
    private ScoresInfo scoresInfo;
    private List<ProductFeature> features;
}

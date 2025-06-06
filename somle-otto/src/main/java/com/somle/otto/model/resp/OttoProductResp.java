package com.somle.otto.model.resp;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OttoProductResp {

    private List<ProductVariation> productVariations;
    private List<Link> links;

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ProductVariation {
        private String productReference;
        private String sku;
        private String ean;
        private String moin;
        private ProductDescription productDescription;
        private List<MediaAsset> mediaAssets;
        private Pricing pricing;
        private ProductSafety productSafety;
        private Order order;
        private Integer quantity;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ProductDescription {
        private String category;
        private String brand;
        private String brandId;
        private String productLine;
        private String description;
        private List<String> bulletPoints;
        private List<Attribute> attributes;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Attribute {
        private String name;
        private List<String> values;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class MediaAsset {
        private String type;
        private String filename;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Pricing {
        private Price standardPrice;
        private String vat;
        private Price msrp;
        private Sale sale;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Price {
        private Double amount;
        private String currency;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Sale {
        private Price salePrice;
        private String startDate;
        private String endDate;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ProductSafety {
        private String name;
        private String address;
        private String email;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Order {
        private Object maxOrderQuantity; // 根据实际结构可能为空对象
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Link {
        private String rel;
        private String href;
    }
}
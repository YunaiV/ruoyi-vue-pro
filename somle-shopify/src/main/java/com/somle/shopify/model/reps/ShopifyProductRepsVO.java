package com.somle.shopify.model.reps;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@NoArgsConstructor
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ShopifyProductRepsVO {

    private Long id;
    private String title;
    private String bodyHtml;
    private String vendor;
    private String productType;
    private ZonedDateTime createdAt;
    private String handle;
    private ZonedDateTime updatedAt;
    private ZonedDateTime publishedAt;
    private String templateSuffix;
    private String publishedScope;
    private String tags;
    private String status;
    private String adminGraphqlApiId;
    private List<Variant> variants;
    private List<Option> options;
    private List<Image> images;
    private Image image;


    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)

    public static class Variant {
        private Long id;
        private Long productId;
        private String title;
        private String price;
        private Integer position;
        private String inventoryPolicy;
        private String compareAtPrice;
        private String option1;
        private String option2;
        private String option3;
        private ZonedDateTime createdAt;
        private ZonedDateTime updatedAt;
        private Boolean taxable;
        private String barcode;
        private String fulfillmentService;
        private Integer grams;
        private String inventoryManagement;
        private Boolean requiresShipping;
        private String sku;
        private Double weight;
        private String weightUnit;
        private Long inventoryItemId;
        private Integer inventoryQuantity;
        private Integer oldInventoryQuantity;
        private String adminGraphqlApiId;
        private Long imageId;
    }


    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Option {
        private Long id;
        private Long productId;
        private String name;
        private Integer position;
        private List<String> values;
    }


    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Image {
        private Long id;
        private String alt;
        private Integer position;
        private Long productId;
        private ZonedDateTime createdAt;
        private ZonedDateTime updatedAt;
        private String adminGraphqlApiId;
        private Integer width;
        private Integer height;
        private String src;
        private List<Long> variantIds;
    }
}

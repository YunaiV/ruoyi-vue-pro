package com.somle.rakuten.model.reps;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RakutenProductsRepsVO {
    private Integer offset;
    private Integer numFound;
    private List<Result> results;

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Result {
        private Item item;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Item {
        private String manageNumber;
        private String itemNumber;
        private String itemType;
        private String title;
        private String tagline;
        private ProductDescription productDescription;
        private String salesDescription;
        private List<Image> images;
        private WhiteBgImage whiteBgImage;
        private String genreId;
        private Boolean hideItem;
        private Boolean unlimitedInventoryFlag;
        private List<CustomizationOption> customizationOptions;
        private Features features;
        private Payment payment;
        private Integer itemDisplaySequence;
        private Layout layout;
        private Map<String, Variant> variants;
        private Date created;
        private Date updated;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ProductDescription {
        private String pc;
        private String sp;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Image {
        private String type;
        private String location;
        private String alt;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class WhiteBgImage {
        private String type;
        private String location;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CustomizationOption {
        private String displayName;
        private String inputType;
        private Boolean required;
        private List<Selection> selections;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Selection {
        private String displayValue;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Features {
        private String searchVisibility;
        private Boolean shopContact;
        private String review;
        private Boolean displayManufacturerContents;
        private Boolean displayNormalCartButton;
        private Boolean displaySubscriptionCartButton;
        private String inventoryDisplay;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Payment {
        private Boolean taxIncluded;
        private Boolean cashOnDeliveryFeeIncluded;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Layout {
        private Integer itemLayoutId;
        private Integer navigationId;
        private Integer layoutSequenceId;
        private Integer smallDescriptionId;
        private Integer largeDescriptionId;
        private Integer showcaseId;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Variant {
        private Boolean restockOnCancel;
        private Boolean backOrderFlag;
        private Integer normalDeliveryDateId;
        private ArticleNumber articleNumber;
        private String standardPrice;
        private Shipping shipping;
        private String merchantDefinedSkuId;
        private Boolean hidden;
        private VariantFeatures features;
        private List<Attribute> attributes;
        private Integer sellableQuantity;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ArticleNumber {
        private String value;
        private Integer exemptionReason;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Shipping {
        private Boolean postageIncluded;
        private Integer singleItemShipping;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class VariantFeatures {
        private Boolean restockNotification;
        private Boolean noshi;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Attribute {
        private String name;
        private List<String> values;
        private String unit;
    }
}
package com.somle.shopee.model.reps;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ShopeeModelListResp {
    private String message;
    private String warning;
    private String requestId;
    private ResponseData response;
    private String error;

    @Data
    @NoArgsConstructor
    public static class ResponseData {
        private List<TierVariation> tierVariation;
        private List<Model> model;
        private List<StandardiseTierVariation> standardiseTierVariation;
    }

    @Data
    @NoArgsConstructor
    public static class TierVariation {
        private String name;
        private List<Option> optionList;

        @Data
        @NoArgsConstructor
        public static class Option {
            private String option;
            private Image image;
        }
    }

    @Data
    @NoArgsConstructor
    public static class Model {
        private List<PriceInfo> priceInfo;
        private String modelSku;
        private String modelStatus;
        private PreOrder preOrder;
        private StockInfoV2 stockInfoV2;
        private String gtinCode;
        private String weight;
        private Dimension dimension;
    }

    @Data
    @NoArgsConstructor
    public static class PriceInfo {
        private String currency;
        private Double currentPrice;
        private Double originalPrice;
        private Double inflatedPriceOfOriginalPrice;
        private Double inflatedPriceOfCurrentPrice;
        private Double sipItemPrice;
        private String sipItemPriceSource;
        private String sipItemPriceCurrency;
    }

    @Data
    @NoArgsConstructor
    public static class PreOrder {
        private Boolean isPreOrder;
        private Integer daysToShip;
    }

    @Data
    @NoArgsConstructor
    public static class StockInfoV2 {
        private StockSummary summaryInfo;
        private List<SellerStock> sellerStock;
        private List<ShopeeStock> shopeeStock;
    }

    @Data
    @NoArgsConstructor
    public static class StockSummary {
        private Integer totalReservedStock;
        private Integer totalAvailableStock;
    }

    @Data
    @NoArgsConstructor
    public static class SellerStock {
        private String locationId;
        private Integer stock;
        private Boolean ifSaleable;
    }

    @Data
    @NoArgsConstructor
    public static class ShopeeStock {
        private String locationId;
        private String stock;
    }

    @Data
    @NoArgsConstructor
    public static class Dimension {
        private Integer packageHeight;
        private Integer packageLength;
        private Integer packageWidth;
    }

    @Data
    @NoArgsConstructor
    public static class StandardiseTierVariation {
        private String variationName;
        private List<VariationOption> variationOptionList;
    }

    @Data
    @NoArgsConstructor
    public static class VariationOption {
        private String variationOptionName;
        private String imageId;
        private String imageUrl;
    }

    @Data
    @NoArgsConstructor
    public static class Image {
        private String imageId;
        private String imageUrl;
    }
}
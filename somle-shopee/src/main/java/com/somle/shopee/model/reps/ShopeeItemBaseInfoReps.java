package com.somle.shopee.model.reps;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ShopeeItemBaseInfoReps {
    private String error;
    private String message;
    private String warning;
    private String requestId;
    private Response response;

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Response {
        private List<Item> itemList;

        @Data
        @NoArgsConstructor
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class Item {
            private Long itemId;
            private Long categoryId;
            private String itemName;
            private String description;
            private String itemSku;
            private Long createTime;
            private Long updateTime;
            private List<Attribute> attributeList;
            private List<PriceInfo> priceInfo;
            private Image image;
            private String weight;
            private Dimension dimension;
            private List<LogisticInfo> logisticInfo;
            private PreOrder preOrder;
            private List<Wholesale> wholesales;
            private String condition;
            private String sizeChart;
            private String itemStatus;
            private String deboost;
            private Boolean hasModel;
            private Long promotionId;
            private List<VideoInfo> videoInfo;
            private Brand brand;
            private Integer itemDangerous;
            private ComplaintPolicy complaintPolicy;
            private TaxInfo taxInfo;
            private DescriptionInfo descriptionInfo;
            private String descriptionType;
            private StockInfoV2 stockInfoV2;
            private ShopeeModelListResp shopeeModelListResp;

            @Data
            @NoArgsConstructor
            @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
            public static class Attribute {
                private Long attributeId;
                private String originalAttributeName;
                private Boolean isMandatory;
                private List<AttributeValue> attributeValueList;

                @Data
                @NoArgsConstructor
                @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
                public static class AttributeValue {
                    private Long valueId;
                    private String originalValueName;
                    private String valueUnit;
                }
            }

            @Data
            @NoArgsConstructor
            @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
            public static class PriceInfo {
                private String currency;
                private Double originalPrice;
                private Double currentPrice;
                private Double inflatedPriceOfOriginalPrice;
                private Double inflatedPriceOfCurrentPrice;
                private Double sipItemPrice;
                private String sipItemPriceSource;
            }

            @Data
            @NoArgsConstructor
            @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
            public static class Image {
                private List<String> imageUrlList;
                private List<String> imageIdList;
            }

            @Data
            @NoArgsConstructor
            @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
            public static class Dimension {
                private Integer packageLength;
                private Integer packageWidth;
                private Integer packageHeight;
            }

            @Data
            @NoArgsConstructor
            @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
            public static class LogisticInfo {
                private Long logisticId;
                private String logisticName;
                private Boolean enabled;
                private Double shippingFee;
                private Integer sizeId;
                private Boolean isFree;
                private Double estimatedShippingFee;
            }

            @Data
            @NoArgsConstructor
            @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
            public static class PreOrder {
                private Boolean isPreOrder;
                private Integer daysToShip;
            }

            @Data
            @NoArgsConstructor
            @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
            public static class Wholesale {
                private Integer minCount;
                private Integer maxCount;
                private Double unitPrice;
                private Double inflatedPriceOfUnitPrice;
            }

            @Data
            @NoArgsConstructor
            @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
            public static class VideoInfo {
                private String videoUrl;
                private String thumbnailUrl;
                private Integer duration;
            }

            @Data
            @NoArgsConstructor
            @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
            public static class Brand {
                private Long brandId;
                private String originalBrandName;
            }

            @Data
            @NoArgsConstructor
            @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
            public static class ComplaintPolicy {
                private String warrantyTime;
                private Boolean excludeEntrepreneurWarranty;
                private Long complaintAddressId;
                private String additionalInformation;
            }

            @Data
            @NoArgsConstructor
            @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
            public static class TaxInfo {
                private String ncm;
                private String diffStateCfop;
                private String csosn;
                private String origin;
                private String cest;
                private String measureUnit;
                private String invoiceOption;
                private String vatRate;
                private String hsCode;
                private String taxCode;
            }

            @Data
            @NoArgsConstructor
            @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
            public static class DescriptionInfo {
                private ExtendedDescription extendedDescription;

                @Data
                @NoArgsConstructor
                @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
                public static class ExtendedDescription {
                    private List<DescriptionField> fieldList;

                    @Data
                    @NoArgsConstructor
                    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
                    public static class DescriptionField {
                        private String fieldType;
                        private String text;
                        private ImageInfo imageInfo;

                        @Data
                        @NoArgsConstructor
                        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
                        public static class ImageInfo {
                            private String imageId;
                            private String imageUrl;
                        }
                    }
                }
            }

            @Data
            @NoArgsConstructor
            @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
            public static class StockInfoV2 {
                private StockSummary summaryInfo;
                private List<SellerStock> sellerStock;
                private List<ShopeeStock> shopeeStock;

                @Data
                @NoArgsConstructor
                @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
                public static class StockSummary {
                    private Integer totalReservedStock;
                    private Integer totalAvailableStock;
                }

                @Data
                @NoArgsConstructor
                @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
                public static class SellerStock {
                    private String locationId;
                    private Integer stock;
                }

                @Data
                @NoArgsConstructor
                @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
                public static class ShopeeStock {
                    private String locationId;
                    private Integer stock;
                }
            }
        }
    }
}
package com.somle.lazada.model.reps;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class LazadaProductResp {
    private String code;
    private ResponseData data;
    private String requestId;

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ResponseData {
        private Integer totalProducts;
        private List<Product> products;

        @Data
        @NoArgsConstructor
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class Product {
            private String createdTime;  // JSON字段：created_time → createdTime（自动转换）
            private String updatedTime;
            private String images;       // 若需解析为List，需添加@JsonDeserialize注解[3,9](@ref)
            private List<Sku> skus;
            private String itemId;
            private String hiddenStatus;
            private List<String> suspendedSkus;
            private String subStatus;
            private String trialProduct;
            private List<RejectReason> rejectReason;
            private String primaryCategory;
            private String marketImages;
            private ProductAttributes attributes;
            private String hiddenReason;
            private String status;

            @Data
            @NoArgsConstructor
            @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
            public static class Sku {
                private String status;       // JSON字段：Status → status（需@JsonProperty覆盖）
                private Integer quantity;
                private String productWeight;
                private List<String> images; // JSON字段：Images → images（自动转换）
                private String SellerSku;
                private String ShopSku;
                private String url;
                private String packageWidth;
                private String specialToTime;
                private String specialFromTime;
                private String packageHeight;
                private Double specialPrice;
                private Double price;
                private String packageLength;
                private String packageWeight;
                private Integer available;
                private Long skuId;
                private String specialToDate;
            }

            @Data
            @NoArgsConstructor
            public static class RejectReason {
                private String suggestion;
                private String violationDetail;
            }

            @Data
            @NoArgsConstructor
            @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
            public static class ProductAttributes {
                private String shortDescription; // JSON字段：short_description → shortDescription
                private String name;
                private String description;
                private String nameEngravement;
                private String warrantyType;
                private String giftWrapping;
                private Integer preorderDays;
                private String brand;
                private String preorder;
            }
        }
    }
}
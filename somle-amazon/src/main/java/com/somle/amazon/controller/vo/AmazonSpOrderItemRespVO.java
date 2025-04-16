package com.somle.amazon.controller.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class AmazonSpOrderItemRespVO {

    private Payload payload;

    @Data
    @Builder
    @JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
    public static class Payload {
        private List<OrderItem> OrderItems;
        private String NextToken;
        private String AmazonOrderId;
    }


    @Data
    @Builder
    @JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
    public static class OrderItem {
        private TaxCollection TaxCollection;
        private ProductInfo ProductInfo;
        private BuyerInfo BuyerInfo;
        private Money ItemTax;
        private Integer QuantityShipped;
        private BuyerRequestedCancel BuyerRequestedCancel;
        private Money ItemPrice;
        private String ASIN;
        private String SellerSKU;
        private String Title;
        private String IsGift;
        private String ConditionSubtypeId;
        private Boolean IsTransparency;
        private Integer QuantityOrdered;
        private Money PromotionDiscountTax;
        private String ConditionId;
        private Money PromotionDiscount;
        private String OrderItemId;

        @Data
        @Builder
        @JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
        public static class TaxCollection {
            private String Model;
            private String ResponsibleParty;
        }

        @Data
        @Builder
        @JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
        public static class ProductInfo {
            private String NumberOfItems;
        }

        @Data
        @Builder
        @JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
        public static class BuyerInfo {
            // 空对象结构保留
        }

        @Data
        @Builder
        @JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
        public static class BuyerRequestedCancel {
            private String IsBuyerRequestedCancel;
            private String BuyerCancelReason;
        }

        @Data
        @Builder
        @JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
        public static class Money {
            private String CurrencyCode;
            private String Amount;
        }

    }
}

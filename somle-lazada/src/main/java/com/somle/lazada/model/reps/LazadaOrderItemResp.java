package com.somle.lazada.model.reps;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class LazadaOrderItemResp {
    private String code;
    private List<OrderData> data;
    private String requestId;

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class OrderData {
        private String orderNumber;
        private String orderId;
        private List<OrderItem> orderItems;

        @Data
        @NoArgsConstructor
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class OrderItem {
            private PickUpStoreInfo pickUpStoreInfo;
            private String taxAmount;
            private String reason;
            private String slaTimeStamp;
            private String paymentTime;
            private String purchaseOrderId;
            private String voucherSeller;
            private String voucherCodeSeller;
            private String voucherCode;
            private String packageId;
            private String buyerId;
            private String variation;
            private String bizGroup;
            private String voucherCodePlatform;
            private String purchaseOrderNumber;
            private String showGiftWrappingTag;
            private String giftWrapping;
            private String scheduleDeliveryStartTimeslot;
            private String sku;
            private String showPersonalizationTag;
            private String invoiceNumber;
            private String orderType;
            private String cancelReturnInitiator;
            private String shopSku;
            private String isReroute;
            private String stagePayStatus;
            private String skuId;
            private String trackingCodePre;
            private String orderItemId;
            private String shopId;
            private String orderFlag;
            private String isFbl;
            private String name;
            private String deliveryOptionSof;
            private String fulfillmentSla;
            private String orderId;
            private String status;
            private String paidPrice;
            private String productMainImage;
            private String voucherPlatform;
            private String productDetailUrl;
            private String promisedShippingTime;
            private String warehouseCode;
            private String shippingType;
            private String createdAt;
            private String supplyPrice;
            private String mp3Order;
            private String voucherSellerLpi;
            private String shippingFeeDiscountPlatform;
            private String personalization;
            private String walletCredits;
            private String updatedAt;
            private String currency;
            private String shippingProviderType;
            private String voucherPlatformLpi;
            private String shippingFeeOriginal;
            private String scheduleDeliveryEndTimeslot;
            private String isDigital;
            private String itemPrice;
            private String shippingServiceCost;
            private String trackingCode;
            private String shippingFeeDiscountSeller;
            private String shippingAmount;
            private String reasonDetail;
            private String returnStatus;
            private String semiManaged;
            private String priorityFulfillmentTag;
            private String shipmentProvider;
            private String supplyPriceCurrency;
            private String voucherAmount;
            private String digitalDeliveryInfo;
            private Object extraAttributes; // 值为"null"时用Object类型

            @Data
            @NoArgsConstructor
            @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
            public static class PickUpStoreInfo {
                private String pickUpStoreAddress;
                private String pickUpStoreName;
                private List<String> pickUpStoreOpenHour;
                private String pickUpStoreCode;
            }
        }
    }
}
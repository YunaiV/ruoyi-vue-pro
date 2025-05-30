package com.somle.lazada.model.reps;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class LazadaOrderResp {
    private String code;
    private ResponseData data;
    private String requestId;

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ResponseData {
        private Integer count;
        private Integer countTotal;
        private List<Order> orders;

        @Data
        @NoArgsConstructor
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class Order {
            private String voucherPlatform;
            private String voucher;
            private String warehouseCode;
            private String orderNumber;
            private String voucherSeller;
            private String createdAt;
            private String voucherCode;
            private String giftOption;
            private String shippingFeeDiscountPlatform;
            private String customerLastName;
            private String promisedShippingTimes;
            private String updatedAt;
            private String price;
            private String nationalRegistrationNumber;
            private String shippingFeeOriginal;
            private String paymentMethod;
            private String addressUpdatedAt;  // 值为"null"时用String类型
            private String buyerNote;
            private String customerFirstName;
            private String shippingFeeDiscountSeller;
            private String shippingFee;
            private String branchNumber;
            private String taxCode;
            private String itemsCount;
            private String deliveryInfo;
            private List<String> statuses;   // 空数组
            private Address addressBilling;
            private Object extraAttributes;  // 值为"null"时用Object类型
            private String orderId;
            private String remarks;
            private String giftMessage;
            private Address addressShipping;
            private List<LazadaOrderItemResp.OrderData.OrderItem> orderItems;

            @Data
            @NoArgsConstructor
            @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
            public static class Address {
                private String country;
                private String address3;
                private String address2;
                private String city;
                private String address1;
                private String phone2;
                private String lastName;
                private String addressDsitrict;  // 注意JSON字段拼写错误
                private String phone;
                private String postCode;
                private String address5;
                private String address4;
                private String firstName;
            }
        }
    }
}
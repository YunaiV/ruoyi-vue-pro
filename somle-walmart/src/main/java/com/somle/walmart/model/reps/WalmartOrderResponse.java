package com.somle.walmart.model.reps;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class WalmartOrderResponse {
    private ListObject list;

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
    public static class ListObject {
        private Meta meta;
        private Elements elements;

        @Data
        @NoArgsConstructor
        @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
        public static class Meta {
            private int totalCount;
            private int limit;
            private String nextCursor;
        }

        @Data
        @NoArgsConstructor
        @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
        public static class Elements {
            private List<Order> order;
        }
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
    public static class Order {
        private String purchaseOrderId;
        private String customerOrderId;
        private String customerEmailId;
        private String orderType;
        private String originalCustomerOrderID;
        private LocalDateTime orderDate;
        private ShippingInfo shippingInfo;
        private OrderLines orderLines;
        private ShipNode shipNode;

        @Data
        @NoArgsConstructor
        @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
        public static class ShippingInfo {
            private String phone;
            private LocalDateTime estimatedDeliveryDate;
            private LocalDateTime estimatedShipDate;
            private String methodCode;
            private PostalAddress postalAddress;

            @Data
            @NoArgsConstructor
            @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
            public static class PostalAddress {
                private String name;
                private String address1;
                private String address2;
                private String city;
                private String state;
                private String postalCode;
                private String country;
                private String addressType;
            }
        }

        @Data
        @NoArgsConstructor
        @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
        public static class OrderLines {
            private List<OrderLine> orderLine;

            @Data
            @NoArgsConstructor
            @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
            public static class OrderLine {
                private String lineNumber;
                private Item item;
                private Charges charges;
                private OrderLineQuantity orderLineQuantity;
                private LocalDateTime statusDate;
                private OrderLineStatuses orderLineStatuses;
                private Object refund; // 根据JSON示例可能为null
                private Fulfillment fulfillment;

                @Data
                @NoArgsConstructor
                @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
                public static class Item {
                    private String productName;
                    private String sku;
                    private String condition;
                }

                @Data
                @NoArgsConstructor
                @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
                public static class Charges {
                    private List<Charge> charge;

                    @Data
                    @NoArgsConstructor
                    @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
                    public static class Charge {
                        private String chargeType;
                        private String chargeName;
                        private Money chargeAmount;
                        private Tax tax;

                        @Data
                        @NoArgsConstructor
                        public static class Money {
                            private String currency;
                            private BigDecimal amount;
                        }

                        @Data
                        @NoArgsConstructor
                        @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
                        public static class Tax {
                            private String taxName;
                            private Money taxAmount;
                        }
                    }
                }

                @Data
                @NoArgsConstructor
                @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
                public static class OrderLineQuantity {
                    private String unitOfMeasurement;
                    private Integer amount;
                }

                @Data
                @NoArgsConstructor
                @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
                public static class OrderLineStatuses {
                    private List<OrderLineStatus> orderLineStatus;

                    @Data
                    @NoArgsConstructor
                    @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
                    public static class OrderLineStatus {
                        private String status;
                        private String subSellerId;
                        private StatusQuantity statusQuantity;
                        private String cancellationReason;
                        private TrackingInfo trackingInfo;
                        private Object returnCenterAddress;

                        @Data
                        @NoArgsConstructor
                        @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
                        public static class StatusQuantity {
                            private String unitOfMeasurement;
                            private String amount;
                        }

                        @Data
                        @NoArgsConstructor
                        @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
                        public static class TrackingInfo {
                            private Long shipDateTime;
                            private CarrierName carrierName;
                            private String methodCode;
                            private Integer carrierMethodCode;
                            private String trackingNumber;
                            private String trackingURL;

                            @Data
                            @NoArgsConstructor
                            @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
                            public static class CarrierName {
                                private String otherCarrier;
                                private String carrier;
                            }
                        }
                    }
                }

                @Data
                @NoArgsConstructor
                @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
                public static class Fulfillment {
                    private String fulfillmentOption;
                    private String shipMethod;
                    private String storeId;
                    private Long pickUpDateTime;
                    private String pickUpBy;
                    private String shippingProgramType;
                    private String shippingSLA;
                    private String shippingConfigSource;
                }
            }
        }

        @Data
        @NoArgsConstructor
        @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
        public static class ShipNode {
            private String type;
        }
    }
}
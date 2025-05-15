package com.somle.wayfair.model.reps;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class WayfairOrderRepsVO {
    private DataObject data;

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class DataObject {
        private List<DropshipPurchaseOrder> getDropshipPurchaseOrders;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class DropshipPurchaseOrder {
        private String poNumber;
        private LocalDateTime poDate;
        private Long orderId;
        private LocalDateTime estimatedShipDate;
        private String customerName;
        private String customerAddress1;
        private String customerAddress2;
        private String customerCity;
        private String customerState;
        private String customerPostalCode;
        private Object orderType; // 根据实际类型替换
        private String packingSlipUrl;
        private ShippingInfo shippingInfo;
        private Warehouse warehouse;
        private List<Product> products;
        private ShipTo shipTo;

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class ShippingInfo {
            private String shipSpeed;
            private String carrierCode;
        }

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class Warehouse {
            private String id;
            private String name;
            private Address address;

            @NoArgsConstructor
            @Data
            @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
            public static class Address {
                private String name;
                private String address1;
                private String address2;
                private String address3;
                private String city;
                private String state;
                private String country;
                private String postalCode;
            }
        }

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class Product {
            private String partNumber;
            private Integer quantity;
            private Double price;
        }

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class ShipTo {
            private String name;
            private String address1;
            private String address2;
            private String address3;
            private String city;
            private String state;
            private String country;
            private String postalCode;
            private String phoneNumber;
        }
    }
}

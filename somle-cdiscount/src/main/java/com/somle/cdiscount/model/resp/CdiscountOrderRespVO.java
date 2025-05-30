package com.somle.cdiscount.model.resp;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CdiscountOrderRespVO {
    private Integer itemsPerPage;
    private List<Order> items;

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Order {
        private String orderId;
        private String reference;
        private Boolean businessOrder;
        private SalesChannel salesChannel;
        private Seller seller;
        private Customer customer;
        private LocalDateTime purchasedAt;
        private LocalDateTime updatedAt;
        private LocalDateTime createdAt;
        private LocalDateTime shippedAtMax;
        private String status;
        private Payment payment;
        private String currencyCode;
        private BillingAddress billingAddress;
        private TotalPrice totalPrice;
        private List<OrderLine> lines;
        private List<ServiceFee> serviceFees;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SalesChannel {
        private String id;
        private String name;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Seller {
        private String id;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Customer {
        private String reference;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Payment {
        private String method;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class BillingAddress {
        private String civility;
        private String firstName;
        private String lastName;
        private String addressLine1;
        private String postalCode;
        private String city;
        private String countryCode;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class TotalPrice {
        private BigDecimal offerPrice;
        private BigDecimal sellingPrice;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class OrderLine {
        private String orderLineId;
        private String status;
        private Integer quantity;
        private PriceInfo totalPrice;
        private OfferPrice offerPrice;
        private SellingPrice sellingPrice;
        private Offer offer;
        private Delivery delivery;
        private ShippingAddress shippingAddress;
        private List<Parcel> parcels;
        private Object productAttributes; // 根据实际数据结构优化
        private List<Object> statusEvents; // 根据实际数据结构优化
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PriceInfo {
        private BigDecimal offerPrice;
        private BigDecimal sellingPrice;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class OfferPrice {
        private BigDecimal unitSalesPrice;
        private BigDecimal shippingCost;
        private Commission commission;
        private List<Tax> taxes;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SellingPrice {
        private BigDecimal unitSalesPrice;
        private BigDecimal shippingCost;
        private List<Tax> taxes;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Commission {
        private BigDecimal amountWithTax;
        private BigDecimal amountWithoutTax;
        private BigDecimal rate;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Tax {
        private String code;
        private String target;
        private BigDecimal amount;
        private BigDecimal rate;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Offer {
        private String id;
        private String sellerProductId;
        private String supplyMode;
        private String productId;
        private String productTitle;
        private String productGtin;
        private String condition;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Delivery {
        private String mode;
        private LocalDateTime promisedAtMin;
        private LocalDateTime promisedAtMax;
        private LocalDateTime shippedAtMax;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ShippingAddress {
        private String civility;
        private String firstName;
        private String lastName;
        private String pickupName;
        private String pickupId;
        private String addressLine1;
        private String addressLine3;
        private String postalCode;
        private String city;
        private String countryCode;
        private String phone;
        private String email;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Parcel {
        private String parcelNumber;
        private String carrierName;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ServiceFee {
        private String label;
        private BigDecimal amount;
    }

}

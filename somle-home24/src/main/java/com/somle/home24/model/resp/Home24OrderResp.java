package com.somle.home24.model.resp;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class Home24OrderResp {

    private Integer totalCount;
    private List<Order> orders;

    @Data
    @NoArgsConstructor
    public static class Order {
        private LocalDateTime acceptanceDecisionDate;
        private Boolean canCancel;
        private Boolean canEvaluate;
        private Boolean canShopShip;
        private Channel channel;
        private String commercialId;
        private LocalDateTime createdDate;
        private String currencyIsoCode;
        private Customer customer;
        private LocalDateTime customerDebitedDate;
        private Boolean customerDirectlyPaysSeller;
        private String orderState;
        private String orderStateReasonCode;
        private String orderStateReasonLabel;
        private String orderTaxMode;
        private Object orderTaxes; // 可为 null
        private String paymentType;
        private String paymentWorkflow;
        private Double price;
        private Promotions promotions;
        private String quoteId;
        private String shippingCarrierCode;
        private String shippingCarrierStandardCode;
        private String shippingCompany;
        private String shippingDeadline;
        private Double shippingPrice;
        private Object shippingPudoId; // 可为 null
        private String shippingTracking;
        private List<OrderLine> orderLines;
        private List<Refund> orderRefunds;
    }

    @Data
    @NoArgsConstructor
    public static class Channel {
        private String code;
        private String label;
    }

    @Data
    @NoArgsConstructor
    public static class Customer {
        private BillingAddress billingAddress;
        private ShippingAddress shippingAddress;
        private String civility;
        private String customerId;
        private String firstname;
        private String lastname;
        private String locale;
        private String customerNotificationEmail;
    }

    @Data
    @NoArgsConstructor
    public static class BillingAddress {
        private String city;
        private String company;
        private String company2;
        private String country;
        private String countryIsoCode;
        private String firstname;
        private String lastname;
        private String phone;
        private String state;
        private String street1;
        private String street2;
        private String zipCode;
    }

    @Data
    @NoArgsConstructor
    public static class ShippingAddress {
        private String additionalInfo;
        private String city;
        private String company;
        private String company2;
        private String country;
        private String countryIsoCode;
        private String firstname;
        private String lastname;
        private String phone;
        private String state;
        private String street1;
        private String street2;
        private String zipCode;
    }

    @Data
    @NoArgsConstructor
    public static class Promotions {
        private List<AppliedPromotion> appliedPromotions;
        private Double totalDeducedAmount;
    }

    @Data
    @NoArgsConstructor
    public static class AppliedPromotion {
        // 按需扩展字段
    }

    @Data
    @NoArgsConstructor
    public static class OrderLine {
        private List<OrderLineAdditionalField> orderLineAdditionalFields;
        private String orderLineId;
        private Integer orderLineIndex;
        private String orderLineState;
        private String orderLineStateReasonCode;
        private String orderLineStateReasonLabel;
        private Double originUnitPrice;
        private Double price;
        private String priceAdditionalInfo;
        private PriceBreakdown priceAmountBreakdown;
        private Double priceUnit;
        private List<ProductMedia> productMedias;
        private Integer quantity;
        private String productShopSku;
        private String offerSku;
        private Long offerId;
    }

    @Data
    @NoArgsConstructor
    public static class OrderLineAdditionalField {
        private String code;
        private String type;
        private String value;
    }

    @Data
    @NoArgsConstructor
    public static class ProductMedia {
        private String mediaUrl;
        private String mimeType;
        private String type;
    }

    @Data
    @NoArgsConstructor
    public static class PriceBreakdown {
        private List<PricePart> parts;
    }

    @Data
    @NoArgsConstructor
    public static class PricePart {
        private Double amount;
        private Boolean commissionable;
        private Boolean debitableFromCustomer;
        private Boolean payableToShop;
    }

    @Data
    @NoArgsConstructor
    public static class Refund {
        private String id;
        private String orderRefundId;
        private Integer quantity;
        private String reasonCode;
        private Double shippingAmount;
        private PriceBreakdown shippingAmountBreakdown;
        private List<Object> shippingTaxes;
        private List<Tax> taxes;
        private String transactionDate;
        private String transactionNumber;
        private Double totalPrice;
        private CommissionDetails commissionDetails;
    }

    @Data
    @NoArgsConstructor
    public static class Tax {
        private Double amount;
        private PriceBreakdown amountBreakdown;
        private String code;
        private Double rate;
        private String taxCalculationRule;
    }

    @Data
    @NoArgsConstructor
    public static class CommissionDetails {
        private Double commissionAmount;
        private Double commissionTaxAmount;
        private List<CommissionTax> commissionTaxes;
        private Double commissionTotalAmount;
        private Double totalCommission;
    }

    @Data
    @NoArgsConstructor
    public static class CommissionTax {
        private Double amount;
        private String code;
    }
}
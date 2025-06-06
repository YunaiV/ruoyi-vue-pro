package com.somle.bestbuy.resp;

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
public class BestbuyOrderRespVO {
    private List<Order> orders;
    private Integer totalCount;

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Order {

        private LocalDateTime acceptanceDecisionDate;
        private Boolean canCancel;
        private Boolean canShopShip;
        private String channel;
        private String commercialId;


        private LocalDateTime createdDate;
        private String currencyIsoCode;
        private Customer customer;


        private LocalDateTime customerDebitedDate;
        private Boolean customerDirectlyPaysSeller;
        private String customerNotificationEmail;

        private DeliveryDate deliveryDate;
        private Fulfillment fulfillment;
        private Boolean fullyRefunded;
        private Boolean hasCustomerMessage;
        private Boolean hasIncident;
        private Boolean hasInvoice;

        private InvoiceDetails invoiceDetails;

        private LocalDateTime lastUpdatedDate;
        private Integer leadtimeToShip;
        private List<Object> orderAdditionalFields; // 根据实际类型替换Object
        private String orderId;
        private List<OrderLine> orderLines;
        private String orderState;
        private String orderStateReasonCode;
        private String orderStateReasonLabel;
        private String orderTaxMode;
        private List<OrderTax> orderTaxes;

        private String paymentType;
        private String paymentWorkflow;
        private BigDecimal price;

        private Promotions promotions;
        private String quoteId;
        private References references;
        private String shippingCarrierCode;
        private String shippingCarrierStandardCode;
        private String shippingCompany;

        private LocalDateTime shippingDeadline;

        private BigDecimal shippingPrice;
        private String shippingPudoId;
        private String shippingTracking;
        private String shippingTrackingUrl;
        private String shippingTypeCode;
        private String shippingTypeLabel;
        private String shippingTypeStandardCode;
        private String shippingZoneCode;
        private String shippingZoneLabel;

        private BigDecimal totalCommission;
        private BigDecimal totalPrice;

        private LocalDateTime transactionDate;

        private String transactionNumber;
    }

    @Data
    @NoArgsConstructor
    public static class Customer {
        private Address billingAddress;
        private String civility;
        private String customerId;
        private String firstname;
        private String lastname;
        private String locale;
        private Address shippingAddress;
    }

    @Data
    @NoArgsConstructor
    public static class Address {
        private String city;
        private String company;
        private String company2;
        private String country;
        private String countryIsoCode;
        private String firstname;
        private String lastname;
        private String phone;
        private String phoneSecondary;
        private String state;
        private String street1;
        private String street2;
        private String zipCode;
    }

    @Data
    @NoArgsConstructor
    public static class Fulfillment {
        private Center center;
    }

    @Data
    @NoArgsConstructor
    public static class Center {
        private String code;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class OrderLine {
        private Boolean canRefund;
        private List<Cancelation> cancelations;
        private String categoryCode;
        private String categoryLabel;
        private BigDecimal commissionFee;
        private BigDecimal commissionRateVat;
        private List<TaxDetail> commissionTaxes;
        private BigDecimal commissionVat;
        private LocalDateTime createdDate;
        private LocalDateTime debitedDate;

        private String description;
        private List<Fee> fees;
        private String offerId;
        private String offerSku;
        private String offerStateCode;
        private String orderLineId;
        private Integer orderLineIndex;
        private String orderLineState;
        private String orderLineStateReasonCode;
        private String orderLineStateReasonLabel;
        private BigDecimal originUnitPrice;
        private BigDecimal price;
        private String priceAdditionalInfo;
        private BigDecimal priceUnit;
        private String productShopSku;
        private String productSku;
        private String productTitle;
        private List<Object> promotions;
        private Integer quantity;
        private LocalDateTime receivedDate;
        private LocalDateTime shippedDate;
        private BigDecimal shippingPrice;
        private String taxLegalNotice;
        private List<TaxDetail> taxes;

        private BigDecimal totalPrice;
        private BigDecimal totalCommission;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class FeeObject {
        private BigDecimal amount;
        private String code;
    }

    @Data
    @NoArgsConstructor
    public static class TaxDetail {
        private BigDecimal amount;
        private String code;
        private BigDecimal rate;
        private AmountBreakdown amountBreakdown;
    }

    @Data
    @NoArgsConstructor
    public static class AmountBreakdown {
        private List<AmountPart> parts;
    }

    @Data
    @NoArgsConstructor
    public static class AmountPart {
        private BigDecimal amount;
        private Boolean commissionable;
        private Boolean debitableFromCustomer;
        private Boolean payableToShop;
    }

    @Data
    public class References {
        private String orderReferenceForCustomer;
        private String orderReferenceForSeller;
    }

    @Data
    public class Promotions {
        private List<Object> appliedPromotions;
        private BigDecimal totalDeducedAmount;
    }

    @Data
    public class OrderTax {
        private String code;
        private BigDecimal rate;
        private BigDecimal totalAmount;
    }

    @Data
    public class InvoiceDetails {
        private List<DocumentDetail> documentDetails;
        private PaymentTerms paymentTerms;
    }

    @Data
    public class DocumentDetail {
        private String format;
    }

    @Data
    public class PaymentTerms {
        private Integer days;
        private String type;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public class DeliveryDate {
        private LocalDateTime earliest;
        private LocalDateTime latest;
    }

    @Data
    public class Cancelation {
        private BigDecimal amount;
        private AmountBreakdown amountBreakdown;
        private BigDecimal commissionAmount;
        private List<TaxDetail> commissionTaxes;
        private BigDecimal commissionTotalAmount;

        private LocalDateTime createdDate;
        private List<Fee> fees;
        private String id;
        private Integer quantity;
        private String reasonCode;
        private BigDecimal shippingAmount;
        private AmountBreakdown shippingAmountBreakdown;
        private List<TaxDetail> shippingTaxes;
        private List<TaxDetail> taxes;
    }

    @Data
    public class Fee {
        private BigDecimal amount;
        private String code;
    }
}
package com.somle.otto.model.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OttoReceipt {
    private String receiptType;
    private boolean isRealReceipt;
    private String receiptNumber;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private String creationDate;
    private String salesOrderId;
    private String orderNumber;
    private String orderDate;
    private String shipmentDate;
    private Shipment shipment;
    private Payment payment;
    private Partner partner;
    private Customer customer;
    private DeliveryAddress deliveryAddress;
    private LineItems lineItems;
    private List<Totals> totals = new ArrayList<>();
    private Amount amountDue;
    private TotalsGrossAmount totalsGrossAmount;
    private Object totalsReductions = new ArrayList<>();
    @JsonProperty("itemPartialRefundPositions")
    private Object itemPartialRefundPositions;
    private Object servicePositions = new ArrayList<>();
    private Object servicePartialRefundPositions = new ArrayList<>();
    private Object deliveryCosts = new ArrayList<>();
    private Object deliveryFeePartialRefundPositions = new ArrayList<>();

    @Data
    public static class Shipment {
        private ShipFromAddress shipFromAddress;

        @Data
        public static class ShipFromAddress {
            private String zipCode;
            private String countryCode;
        }
    }

    @Data
    public static class Payment {
        private String paymentProvider;
        private String paymentMethod;
        private boolean paymentBreak;
    }

    @Data
    public static class Partner {
        private String partnerName;
        private String street;
        private String zipCode;
        private String city;
        private String countryCode;
        private String vatId;
        private String representedBy;
        private String registerCourt;
        private String commercialRegisterNumber;
        private String shopName;
    }

    @Data
    public static class Customer {
        private InvoiceAddress invoiceAddress;

        @Data
        public static class InvoiceAddress {
            private String firstName;
            private String lastName;
            private String street;
            private String houseNumber;
            private String zipCode;
            private String city;
            private String countryCode;
        }
    }

    @Data
    public static class DeliveryAddress {
        private String firstName;
        private String lastName;
        private String street;
        private String houseNumber;
        private String addition;
        private String zipCode;
        private String city;
        private String countryCode;
    }

    @Data
    public static class LineItems {
        private List<ItemPositions> itemPositions;

        @Data
        public static class ItemPositions {
            private int lineNumber;
            private String sku;
            private String articleNumber;
            private String productTitle;
            private String variationAttributes;
            private List<String> positionItemIds;
            private int quantity;
            private Price unitPrice;
            private List<PriceToPayPerPositionItems> priceToPayPerPositionItems;
            private PositionSum positionSum;
            private List<Object> priceModifications;

            @Data
            public static class Price {
                private String taxType;
                private String taxRate;
                private Amount gross;
                private Amount net;
                private Amount tax;
            }

            @Data
            public static class PriceToPayPerPositionItems {
                private List<String> positionItemIds;
                private Price priceToPay;
            }

            @Data
            public static class PositionSum {
                private String taxType;
                private String taxRate;
                private Amount gross;
                private Amount net;
                private Amount tax;
            }
        }
    }

    @Data
    public static class Totals {
        private String taxType;
        private String taxRate;
        private Amount gross;
        private Amount net;
        private Amount tax;
    }

    @Data
    public static class Amount {
        private double amount;
        private String currency;
    }

    @Data
    public static class TotalsGrossAmount {
        private double amount;
        private String currency;
    }
}

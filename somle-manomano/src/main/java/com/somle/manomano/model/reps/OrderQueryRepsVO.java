package com.somle.manomano.model.reps;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OrderQueryRepsVO {

    private List<Order> content;
    private Pagination pagination;

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Pagination {
        private Integer items;
        private Integer limit;
        private Integer page;
        private Integer pages;
        private Links links;

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class Links {
            private String first;
            private String gotoX;
            private String last;
            private String next;
            private String previous;
        }
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Order {
        private String status;
        private Price totalPrice;
        private Price totalPriceExcludingVat;
        private Price totalPriceVat;
        private Price shippingPrice;
        private Price shippingPriceExcludingVat;
        private String shippingPriceVatRate;
        private Price productsPrice;
        private Price productsPriceExcludingVat;
        private Price productsPriceVat;
        private Price manomanoDiscount;
        private Price sellerDiscount;
        private String orderReference;
        private Price shippingDiscount;
        private List<Product> products;
        private Long sellerContractId;
        private String createdAt;
        private String statusUpdatedAt;
        private Price totalDiscount;
        private Addresses addresses;
        private Customer customer;
        private Boolean isMmf;
        private Boolean isProfessional;
        private String billingFiscalNumber;
        private Vat vat;

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class Price {
            private BigDecimal amount;
            private String currency;
        }

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class Product {
            private Integer quantity;
            private String vatRate;
            private String shippingVatRate;
            private String carrier;
            private String sellerSku;
            private Price price;
            private Price priceExcludingVat;
            private String title;
            private Price shippingPrice;
            private Price sumShippingPrice;
            private Price shippingPriceExcludingVat;
            private Price productPrice;
            private Price productPriceExcludingVat;
            private Price totalPrice;
            private Price totalPriceExcludingVat;
            private String productTitle;
        }

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class Addresses {
            private Address shipping;
            private Address billing;
            private Address relay;

            @NoArgsConstructor
            @Data
            @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
            public static class Address {
                private String phone;
                private String email;
                private String firstname;
                private String lastname;
                private String company;
                private String addressLine1;
                private String city;
                private String zipcode;
                private String country;
                private String countryIso;
            }
        }

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class Customer {
            private String firstname;
            private String lastname;
        }

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class Vat {
            private String eoriNumber;
            private String intracoVatNumber;
            private String invoiceFiscalNumber;
            private Boolean isB2b;
            private String localVatNumber;
            private String recipientCode;
            private Boolean subjectedToVat;
            private String vatRate;
            private String vatLiability;
            private String invoiceLiability;
        }
    }
}
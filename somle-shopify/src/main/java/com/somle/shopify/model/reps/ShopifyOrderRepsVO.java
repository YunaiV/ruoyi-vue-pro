package com.somle.shopify.model.reps;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ShopifyOrderRepsVO {

    private Long id;

    private String adminGraphqlApiId;

    private Integer appId;

    private String browserIp;

    private Boolean buyerAcceptsMarketing;

    private Object cancelReason;

    private Object cancelledAt;

    private String cartToken;

    private Long checkoutId;

    private String checkoutToken;

    private ClientDetailsDTO clientDetails;

    private Object closedAt;

    private Object company;

    private String confirmationNumber;

    private Boolean confirmed;

    private String contactEmail;

    private String createdAt;

    private String currency;

    private String currentSubtotalPrice;

    private CurrentSubtotalPriceSetDTO currentSubtotalPriceSet;

    private Object currentTotalAdditionalFeesSet;

    private String currentTotalDiscounts;

    private CurrentTotalDiscountsSetDTO currentTotalDiscountsSet;

    private Object currentTotalDutiesSet;

    private String currentTotalPrice;

    private CurrentTotalPriceSetDTO currentTotalPriceSet;

    private String currentTotalTax;

    private CurrentTotalTaxSetDTO currentTotalTaxSet;

    private String customerLocale;

    private Object deviceId;

    private List<?> discountCodes;

    private Boolean dutiesIncluded;
    //客户邮箱
    private String email;

    private Boolean estimatedTaxes;

    private String financialStatus;

    private Object fulfillmentStatus;

    private String landingSite;

    private Object landingSiteRef;

    private Object locationId;

    private String merchantBusinessEntityId;

    private Object merchantOfRecordAppId;

    private String name;

    private Object note;

    private List<?> noteAttributes;

    private Integer number;

    private Integer orderNumber;

    private String orderStatusUrl;

    private Object originalTotalAdditionalFeesSet;

    private Object originalTotalDutiesSet;

    private List<String> paymentGatewayNames;

    private Object phone;

    private Object poNumber;

    private String presentmentCurrency;

    private String processedAt;

    private Object reference;

    private String referringSite;

    private Object sourceIdentifier;

    private String sourceName;

    private Object sourceUrl;

    private String subtotalPrice;

    private SubtotalPriceSetDTO subtotalPriceSet;

    private String tags;

    private Boolean taxExempt;

    private List<TaxLinesDTO> taxLines;

    private Boolean taxesIncluded;

    private Boolean test;

    private String token;

    private TotalCashRoundingPaymentAdjustmentSetDTO totalCashRoundingPaymentAdjustmentSet;

    private TotalCashRoundingRefundAdjustmentSetDTO totalCashRoundingRefundAdjustmentSet;

    private String totalDiscounts;

    private TotalDiscountsSetDTO totalDiscountsSet;

    private String totalLineItemsPrice;

    private TotalLineItemsPriceSetDTO totalLineItemsPriceSet;

    private String totalOutstanding;

    private String totalPrice;

    private TotalPriceSetDTO totalPriceSet;

    private TotalShippingPriceSetDTO totalShippingPriceSet;

    private String totalTax;

    private TotalTaxSetDTO totalTaxSet;

    private String totalTipReceived;

    private Integer totalWeight;

    private String updatedAt;

    private Object userId;

    private BillingAddressDTO billingAddress;

    private CustomerDTO customer;

    private List<?> discountApplications;

    private List<?> fulfillments;

    private List<LineItemsDTO> lineItems;

    private Object paymentTerms;

    private List<?> refunds;

    private ShippingAddressDTO shippingAddress;

    private List<ShippingLinesDTO> shippingLines;

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ClientDetailsDTO {

        private String acceptLanguage;

        private Object browserHeight;

        private String browserIp;

        private Object browserWidth;

        private Object sessionHash;

        private String userAgent;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CurrentSubtotalPriceSetDTO {

        private ShopMoneyDTO shopMoney;

        private PresentmentMoneyDTO presentmentMoney;

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class ShopMoneyDTO {

            private String amount;

            private String currencyCode;
        }

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class PresentmentMoneyDTO {

            private String amount;

            private String currencyCode;
        }
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CurrentTotalDiscountsSetDTO {

        private ShopMoneyDTO shopMoney;

        private PresentmentMoneyDTO presentmentMoney;

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class ShopMoneyDTO {

            private String amount;

            private String currencyCode;
        }

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class PresentmentMoneyDTO {

            private String amount;

            private String currencyCode;
        }
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CurrentTotalPriceSetDTO {

        private ShopMoneyDTO shopMoney;

        private PresentmentMoneyDTO presentmentMoney;

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class ShopMoneyDTO {

            private String amount;

            private String currencyCode;
        }

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class PresentmentMoneyDTO {

            private String amount;

            private String currencyCode;
        }
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CurrentTotalTaxSetDTO {

        private ShopMoneyDTO shopMoney;

        private PresentmentMoneyDTO presentmentMoney;

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class ShopMoneyDTO {

            private String amount;

            private String currencyCode;
        }

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class PresentmentMoneyDTO {

            private String amount;

            private String currencyCode;
        }
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SubtotalPriceSetDTO {

        private ShopMoneyDTO shopMoney;

        private PresentmentMoneyDTO presentmentMoney;

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class ShopMoneyDTO {

            private String amount;

            private String currencyCode;
        }

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class PresentmentMoneyDTO {

            private String amount;

            private String currencyCode;
        }
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class TotalCashRoundingPaymentAdjustmentSetDTO {

        private ShopMoneyDTO shopMoney;

        private PresentmentMoneyDTO presentmentMoney;

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class ShopMoneyDTO {

            private String amount;

            private String currencyCode;
        }

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class PresentmentMoneyDTO {

            private String amount;

            private String currencyCode;
        }
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class TotalCashRoundingRefundAdjustmentSetDTO {

        private ShopMoneyDTO shopMoney;

        private PresentmentMoneyDTO presentmentMoney;

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class ShopMoneyDTO {

            private String amount;

            private String currencyCode;
        }

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class PresentmentMoneyDTO {

            private String amount;

            private String currencyCode;
        }
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class TotalDiscountsSetDTO {

        private ShopMoneyDTO shopMoney;

        private PresentmentMoneyDTO presentmentMoney;

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class ShopMoneyDTO {

            private String amount;

            private String currencyCode;
        }

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class PresentmentMoneyDTO {

            private String amount;

            private String currencyCode;
        }
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class TotalLineItemsPriceSetDTO {

        private ShopMoneyDTO shopMoney;

        private PresentmentMoneyDTO presentmentMoney;

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class ShopMoneyDTO {

            private String amount;

            private String currencyCode;
        }

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class PresentmentMoneyDTO {

            private String amount;

            private String currencyCode;
        }
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class TotalPriceSetDTO {

        private ShopMoneyDTO shopMoney;

        private PresentmentMoneyDTO presentmentMoney;

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class ShopMoneyDTO {

            private String amount;

            private String currencyCode;
        }

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class PresentmentMoneyDTO {

            private String amount;

            private String currencyCode;
        }
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class TotalShippingPriceSetDTO {

        private ShopMoneyDTO shopMoney;

        private PresentmentMoneyDTO presentmentMoney;

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class ShopMoneyDTO {

            private String amount;

            private String currencyCode;
        }

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class PresentmentMoneyDTO {

            private String amount;

            private String currencyCode;
        }
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class TotalTaxSetDTO {

        private ShopMoneyDTO shopMoney;

        private PresentmentMoneyDTO presentmentMoney;

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class ShopMoneyDTO {

            private String amount;

            private String currencyCode;
        }

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class PresentmentMoneyDTO {

            private String amount;

            private String currencyCode;
        }
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class BillingAddressDTO {

        private String firstName;

        private String address1;

        private String phone;

        private String city;

        private String zip;

        private String province;

        private String country;

        private String lastName;

        private String address2;

        private Object company;

        private Double latitude;

        private Double longitude;

        private String name;

        private String countryCode;

        private String provinceCode;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CustomerDTO {

        private Long id;

        private String email;

        private String createdAt;

        private String updatedAt;

        private String firstName;

        private String lastName;

        private String state;

        private Object note;

        private Boolean verifiedEmail;

        private Object multipassIdentifier;

        private Boolean taxExempt;

        private String phone;

        private EmailMarketingConsentDTO emailMarketingConsent;

        private SmsMarketingConsentDTO smsMarketingConsent;

        private String tags;

        private String currency;

        private List<?> taxExemptions;

        private String adminGraphqlApiId;

        private DefaultAddressDTO defaultAddress;

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class EmailMarketingConsentDTO {

            private String state;

            private String optInLevel;

            private Object consentUpdatedAt;
        }

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class SmsMarketingConsentDTO {

            private String state;

            private String optInLevel;

            private Object consentUpdatedAt;

            private String consentCollectedFrom;
        }

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class DefaultAddressDTO {

            private Long id;

            private Long customerId;

            private String firstName;

            private String lastName;

            private Object company;

            private String address1;

            private Object address2;

            private String city;

            private String province;

            private String country;

            private String zip;

            private String phone;

            private String name;

            private String provinceCode;

            private String countryCode;

            private String countryName;

            private Boolean defaultX;
        }
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ShippingAddressDTO {

        private String firstName;

        private String address1;

        private String phone;

        private String city;

        private String zip;

        private String province;

        private String country;

        private String lastName;

        private Object address2;

        private Object company;

        private Double latitude;

        private Double longitude;

        private String name;

        private String countryCode;

        private String provinceCode;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class TaxLinesDTO {

        private String price;

        private Double rate;

        private String title;

        private PriceSetDTO priceSet;

        private Boolean channelLiable;

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class PriceSetDTO {

            private ShopMoneyDTO shopMoney;

            private PresentmentMoneyDTO presentmentMoney;

            @NoArgsConstructor
            @Data
            @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
            public static class ShopMoneyDTO {

                private String amount;

                private String currencyCode;
            }

            @NoArgsConstructor
            @Data
            @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
            public static class PresentmentMoneyDTO {

                private String amount;

                private String currencyCode;
            }
        }
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class LineItemsDTO {

        private Long id;

        private String adminGraphqlApiId;

        private List<?> attributedStaffs;

        private Integer currentQuantity;

        private Integer fulfillableQuantity;

        private String fulfillmentService;

        private Object fulfillmentStatus;

        private Boolean giftCard;

        private Integer grams;

        private String name;

        private String price;

        private PriceSetDTO priceSet;

        private Boolean productExists;

        private Long productId;

        private List<?> properties;

        private Integer quantity;

        private Boolean requiresShipping;

        private String sku;

        private Boolean taxable;

        private String title;

        private String totalDiscount;

        private TotalDiscountSetDTO totalDiscountSet;

        private Long variantId;

        private String variantInventoryManagement;

        private String variantTitle;

        private String vendor;

        private List<TaxLinesDTO> taxLines;

        private List<?> duties;

        private List<?> discountAllocations;

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class PriceSetDTO {

            private ShopMoneyDTO shopMoney;

            private PresentmentMoneyDTO presentmentMoney;

            @NoArgsConstructor
            @Data
            @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
            public static class ShopMoneyDTO {

                private String amount;

                private String currencyCode;
            }

            @NoArgsConstructor
            @Data
            @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
            public static class PresentmentMoneyDTO {

                private String amount;

                private String currencyCode;
            }
        }

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class TotalDiscountSetDTO {

            private ShopMoneyDTO shopMoney;

            private PresentmentMoneyDTO presentmentMoney;

            @NoArgsConstructor
            @Data
            @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
            public static class ShopMoneyDTO {

                private String amount;

                private String currencyCode;
            }

            @NoArgsConstructor
            @Data
            @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
            public static class PresentmentMoneyDTO {

                private String amount;

                private String currencyCode;
            }
        }

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class TaxLinesDTO {

            private Boolean channelLiable;

            private String price;

            private PriceSetDTO priceSet;

            private Double rate;

            private String title;

            @NoArgsConstructor
            @Data
            @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
            public static class PriceSetDTO {

                private ShopMoneyDTO shopMoney;

                private PresentmentMoneyDTO presentmentMoney;

                @NoArgsConstructor
                @Data
                @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
                public static class ShopMoneyDTO {

                    private String amount;

                    private String currencyCode;
                }

                @NoArgsConstructor
                @Data
                @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
                public static class PresentmentMoneyDTO {

                    private String amount;

                    private String currencyCode;
                }
            }
        }
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ShippingLinesDTO {

        private Long id;

        private Object carrierIdentifier;

        private String code;

        private String discountedPrice;

        private DiscountedPriceSetDTO discountedPriceSet;

        private Boolean isRemoved;

        private Object phone;

        private String price;

        private PriceSetDTO priceSet;

        private Object requestedFulfillmentServiceId;

        private String source;

        private String title;

        private List<TaxLinesDTO> taxLines;

        private List<?> discountAllocations;

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class DiscountedPriceSetDTO {

            private ShopMoneyDTO shopMoney;

            private PresentmentMoneyDTO presentmentMoney;

            @NoArgsConstructor
            @Data
            @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
            public static class ShopMoneyDTO {

                private String amount;

                private String currencyCode;
            }

            @NoArgsConstructor
            @Data
            @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
            public static class PresentmentMoneyDTO {

                private String amount;

                private String currencyCode;
            }
        }

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class PriceSetDTO {

            private ShopMoneyDTO shopMoney;

            private PresentmentMoneyDTO presentmentMoney;

            @NoArgsConstructor
            @Data
            @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
            public static class ShopMoneyDTO {

                private String amount;

                private String currencyCode;
            }

            @NoArgsConstructor
            @Data
            @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
            public static class PresentmentMoneyDTO {

                private String amount;

                private String currencyCode;
            }
        }

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class TaxLinesDTO {

            private Boolean channelLiable;

            private String price;

            private PriceSetDTO priceSet;

            private Double rate;

            private String title;

            @NoArgsConstructor
            @Data
            @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
            public static class PriceSetDTO {

                private ShopMoneyDTO shopMoney;

                private PresentmentMoneyDTO presentmentMoney;

                @NoArgsConstructor
                @Data
                @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
                public static class ShopMoneyDTO {

                    private String amount;

                    private String currencyCode;
                }

                @NoArgsConstructor
                @Data
                @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
                public static class PresentmentMoneyDTO {

                    private String amount;

                    private String currencyCode;
                }
            }
        }
    }
}

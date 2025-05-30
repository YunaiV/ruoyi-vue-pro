package com.somle.home24.model.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Home24ShopResp {
    private List<ApplicableTax> applicableTaxes;
    private Long approvalDelay;
    private Double approvalRate;
    private Object banner; // 根据实际类型替换
    private BillingInfo billingInfo;
    private List<String> channels;
    private Object closedFrom;
    private Object closedTo;
    private ContactInformations contactInformations;
    private String currencyIsoCode;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime dateCreated;

    private DefaultBillingInformation defaultBillingInformation;
    private List<String> domains;
    private Integer evaluationsCount;
    private Boolean freeShipping;
    private Double grade;
    private Boolean isProfessional;
    private Kyc kyc;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime lastUpdatedDate;

    private String logo;
    private String model;
    private Integer offersCount;
    private Long orderMessagesResponseDelay;
    private Integer ordersCount;
    private PaymentDetails paymentDetails;
    private PaymentInfo paymentInfo;
    private Boolean paymentMethodMandatory;
    private Boolean premium;
    private ProDetails proDetails;
    private List<Object> producerIdentifiers; // 根据实际类型替换
    private String returnPolicy;
    private Shipping shipping;
    private String shippingCountry;
    private List<String> shippingTypes;
    private List<String> shippingZones;
    private List<ShippingDetail> shippings;
    private List<ShopAdditionalField> shopAdditionalFields;
    private Integer shopId;
    private String shopName;
    private String shopState;
    private List<SpecificBillingInformation> specificBillingInformations;
    private Object suspensionType;

    @Data
    @NoArgsConstructor
    public static class ApplicableTax {
        private String code;
        private List<String> feeTypes; // 建议使用枚举类型
        private String label;
    }

    @Data
    @NoArgsConstructor
    public static class BillingInfo {
        private String bankCity;
        private String bankName;
        private String bankStreet;
        private String bic;
        private String iban;
        private String owner;
        private String zipCode;
    }

    @Data
    @NoArgsConstructor
    public static class ContactInformations {
        private String city;
        private String civility;
        private String country;
        private String email;
        private String fax;
        private String firstname;
        private String lastname;
        private String phone;
        private String phoneSecondary;
        private String state;
        private String street1;
        private String street2;
        private String webSite;
        private String zipCode;
    }

    @Data
    @NoArgsConstructor
    public static class DefaultBillingInformation {
        private CorporateInformation corporateInformation;
        private String defaultLanguage;
        private FiscalInformation fiscalInformation;
        private String id;
        private RegistrationAddress registrationAddress;

        @Data
        @NoArgsConstructor
        public static class CorporateInformation {
            private String companyRegistrationName;
            private String companyRegistrationNumber;
            private ComplementaryInformation complementaryInformation;

            @Data
            @NoArgsConstructor
            public static class ComplementaryInformation {
                private Boolean isInLiquidation;
                private Boolean soleTrader;
            }
        }

        @Data
        @NoArgsConstructor
        public static class FiscalInformation {
            private String localTaxNumber;
            private String taxIdentificationCountry;
            private String taxIdentificationNumber;
        }

        @Data
        @NoArgsConstructor
        public static class RegistrationAddress {
            private String city;
            private String countryIsoCode;
            private String state;
            private String street1;
            private String street2;
            private String zipCode;
        }
    }

    @Data
    @NoArgsConstructor
    public static class Kyc {
        private String reason;
        private String status; // 建议使用枚举类型
    }

    @Data
    @NoArgsConstructor
    public static class PaymentDetails {
        private Double paidBalance;
        private Double payableBalance;
        private Double pendingBalance;
    }

    @Data
    @NoArgsConstructor
    public static class PaymentInfo {
        @JsonProperty("@type")
        private String type;
        private String bankName;
        private String bic;
        private String iban;
        private String owner;
    }

    @Data
    @NoArgsConstructor
    public static class ProDetails {
        @JsonProperty("VAT_number")
        private String vatNumber;
        private String corporateName;
        private String identificationNumber;
        private String taxIdentificationNumber;
    }

    @Data
    @NoArgsConstructor
    public static class Shipping {
        private Long leadTimeToShip;
    }

    @Data
    @NoArgsConstructor
    public static class ShippingDetail {
        private List<Object> additionalFields;
        private Double shippingFreeAmount;
        private String shippingTypeCode;
        private String shippingTypeLabel;
        private String shippingTypeStandardCode;
        private String shippingZoneCode;
        private String shippingZoneLabel;
    }

    @Data
    @NoArgsConstructor
    public static class ShopAdditionalField {
        private String code;
        private String type; // 建议使用枚举类型
        private String value;
    }

    @Data
    @NoArgsConstructor
    public static class SpecificBillingInformation {
        private CorporateInformation corporateInformation;
        private FiscalInformation fiscalInformation;
        private String id;
        private RegistrationAddress registrationAddress;

        @Data
        @NoArgsConstructor
        public static class CorporateInformation {
            private String companyRegistrationName;
            private String companyRegistrationNumber;
            private ComplementaryInformation complementaryInformation;

            @Data
            @NoArgsConstructor
            public static class ComplementaryInformation {
                private Boolean isInLiquidation;
                private Double shareCapital;
                private String shareCapitalCurrency;
                private Boolean soleTrader;
            }
        }

        @Data
        @NoArgsConstructor
        public static class FiscalInformation {
            private String localTaxNumber;
            private String taxIdentificationCountry;
            private String taxIdentificationNumber;
        }

        @Data
        @NoArgsConstructor
        public static class RegistrationAddress {
            private String city;
            private String countryIsoCode;
            private String state;
            private String street1;
            private String street2;
            private String zipCode;
        }
    }
}
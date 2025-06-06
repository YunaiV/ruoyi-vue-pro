package com.somle.bestbuy.resp;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BestbuyShopRespVO {
    private List<ApplicableTax> applicableTaxes;
    private Integer approvalDelay;
    private Double approvalRate;
    private Object banner;  // 根据实际类型替换
    private BillingInfo billingInfo;
    private List<String> channels;
    private Date closedFrom;
    private Date closedTo;
    private ContactInformations contactInformations;
    private String currencyIsoCode;
    private LocalDateTime dateCreated;
    private DefaultBillingInformation defaultBillingInformation;
    private String description;
    private List<String> domains;
    private Integer evaluationsCount;
    private Boolean freeShipping;
    private Double grade;
    private Boolean isProfessional;
    private LocalDateTime lastUpdatedDate;

    private Object logo;
    private String model;
    private Integer offersCount;
    private Integer orderMessagesResponseDelay;
    private Integer ordersCount;
    private PaymentDetails paymentDetails;
    private PaymentInfo paymentInfo;
    private Boolean paymentMethodMandatory;
    private Boolean premium;
    private ProDetails proDetails;
    private String returnPolicy;
    private Shipping shipping;
    private String shippingCountry;
    private List<String> shippingTypes;
    private List<String> shippingZones;
    private List<Shipping> shippings;
    private List<ShopAdditionalField> shopAdditionalFields;
    private Integer shopId;
    private String shopName;
    private String shopState;
    private String suspensionType;

    /* 内部类定义 */
    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ApplicableTax {
        private String code;
        private List<String> feeTypes;
        private String label;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
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
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
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
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class DefaultBillingInformation {
        private CorporateInformation corporateInformation;
        private String defaultLanguage;
        private FiscalInformation fiscalInformation;
        private String id;
        private RegistrationAddress registrationAddress;

        @Data
        @NoArgsConstructor
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class CorporateInformation {
            private String companyRegistrationName;
            private String companyRegistrationNumber;
        }

        @Data
        @NoArgsConstructor
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class FiscalInformation {
            private String taxIdentificationCountry;
        }

        @Data
        @NoArgsConstructor
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
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
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PaymentDetails {
        private Double paidBalance;
        private Double payableBalance;
        private Double pendingBalance;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PaymentInfo {
        private String type;  // @type字段映射
        private String bankAccountNumber;
        private String bankName;
        private String bankStreet;
        private String bic;
        private String owner;
        private String routingNumber;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ProDetails {
        private String vatNumber;
        private String corporateName;
        private String identificationNumber;
        private String taxIdentificationNumber;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Shipping {
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
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ShopAdditionalField {
        private String code;
        private String type;
        private String value;
    }
}

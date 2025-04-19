package com.somle.shopify.model.reps;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ShopifyShopRepsVO {


    private Long id;

    private String name;

    private String email;

    private String domain;

    private String province;

    private String country;

    private String address1;

    private String zip;

    private String city;

    private String source;

    private String phone;

    private Double latitude;

    private Double longitude;

    private String primaryLocale;

    private String address2;

    private String createdAt;

    private String updatedAt;

    private String countryCode;

    private String countryName;

    private String currency;

    private String customerEmail;

    private String timezone;

    private String ianaTimezone;

    private String shopOwner;

    private String moneyFormat;

    private String moneyWithCurrencyFormat;

    private String weightUnit;

    private String provinceCode;

    private Boolean taxesIncluded;

    private Boolean autoConfigureTaxInclusivity;

    private Object taxShipping;

    private Boolean countyTaxes;

    private String planDisplayName;

    private String planName;

    private Boolean hasDiscounts;

    private Boolean hasGiftCards;

    private String myshopifyDomain;

    private Object googleAppsDomain;

    private Object googleAppsLoginEnabled;

    private String moneyInEmailsFormat;

    private String moneyWithCurrencyInEmailsFormat;

    private Boolean eligibleForPayments;

    private Boolean requiresExtraPaymentsAgreement;

    private Boolean passwordEnabled;

    private Boolean hasStorefront;

    private Boolean finances;

    private Long primaryLocationId;

    private Boolean checkoutApiSupported;

    private Boolean multiLocationEnabled;

    private Boolean setupRequired;

    private Boolean preLaunchEnabled;

    private List<String> enabledPresentmentCurrencies;

    private Boolean marketingSmsConsentEnabledAtCheckout;

    private Boolean transactionalSmsDisabled;
}

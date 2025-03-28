package com.somle.shopify.model.reps;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class ShopifyShopRepsDTO {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("email")
    private String email;
    @JsonProperty("domain")
    private String domain;
    @JsonProperty("province")
    private String province;
    @JsonProperty("country")
    private String country;
    @JsonProperty("address1")
    private String address1;
    @JsonProperty("zip")
    private String zip;
    @JsonProperty("city")
    private String city;
    @JsonProperty("source")
    private String source;
    @JsonProperty("phone")
    private String phone;
    @JsonProperty("latitude")
    private Double latitude;
    @JsonProperty("longitude")
    private Double longitude;
    @JsonProperty("primary_locale")
    private String primaryLocale;
    @JsonProperty("address2")
    private String address2;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("updated_at")
    private String updatedAt;
    @JsonProperty("country_code")
    private String countryCode;
    @JsonProperty("country_name")
    private String countryName;
    @JsonProperty("currency")
    private String currency;
    @JsonProperty("customer_email")
    private String customerEmail;
    @JsonProperty("timezone")
    private String timezone;
    @JsonProperty("iana_timezone")
    private String ianaTimezone;
    @JsonProperty("shop_owner")
    private String shopOwner;
    @JsonProperty("money_format")
    private String moneyFormat;
    @JsonProperty("money_with_currency_format")
    private String moneyWithCurrencyFormat;
    @JsonProperty("weight_unit")
    private String weightUnit;
    @JsonProperty("province_code")
    private String provinceCode;
    @JsonProperty("taxes_included")
    private Boolean taxesIncluded;
    @JsonProperty("auto_configure_tax_inclusivity")
    private Boolean autoConfigureTaxInclusivity;
    @JsonProperty("tax_shipping")
    private Object taxShipping;
    @JsonProperty("county_taxes")
    private Boolean countyTaxes;
    @JsonProperty("plan_display_name")
    private String planDisplayName;
    @JsonProperty("plan_name")
    private String planName;
    @JsonProperty("has_discounts")
    private Boolean hasDiscounts;
    @JsonProperty("has_gift_cards")
    private Boolean hasGiftCards;
    @JsonProperty("myshopify_domain")
    private String myshopifyDomain;
    @JsonProperty("google_apps_domain")
    private Object googleAppsDomain;
    @JsonProperty("google_apps_login_enabled")
    private Object googleAppsLoginEnabled;
    @JsonProperty("money_in_emails_format")
    private String moneyInEmailsFormat;
    @JsonProperty("money_with_currency_in_emails_format")
    private String moneyWithCurrencyInEmailsFormat;
    @JsonProperty("eligible_for_payments")
    private Boolean eligibleForPayments;
    @JsonProperty("requires_extra_payments_agreement")
    private Boolean requiresExtraPaymentsAgreement;
    @JsonProperty("password_enabled")
    private Boolean passwordEnabled;
    @JsonProperty("has_storefront")
    private Boolean hasStorefront;
    @JsonProperty("finances")
    private Boolean finances;
    @JsonProperty("primary_location_id")
    private Integer primaryLocationId;
    @JsonProperty("checkout_api_supported")
    private Boolean checkoutApiSupported;
    @JsonProperty("multi_location_enabled")
    private Boolean multiLocationEnabled;
    @JsonProperty("setup_required")
    private Boolean setupRequired;
    @JsonProperty("pre_launch_enabled")
    private Boolean preLaunchEnabled;
    @JsonProperty("enabled_presentment_currencies")
    private List<String> enabledPresentmentCurrencies;
    @JsonProperty("marketing_sms_consent_enabled_at_checkout")
    private Boolean marketingSmsConsentEnabledAtCheckout;
    @JsonProperty("transactional_sms_disabled")
    private Boolean transactionalSmsDisabled;
}

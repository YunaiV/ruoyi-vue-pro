package com.somle.matomo.model;


import java.util.List;

import lombok.Data;

@Data
public class MatomoVisit {
    private Integer idSite;
    private Integer idVisit;
    private String visitIp;
    private String visitorId;
    private String fingerprint;
    private List<MatomoAction> actionDetails;
    private Integer goalConversions;
    private String siteCurrency;
    private String siteCurrencySymbol;
    private String serverDate;
    private String visitServerHour;
    private long lastActionTimestamp;
    private String lastActionDateTime;
    private String siteName;
    private long serverTimestamp;
    private long firstActionTimestamp;
    private String serverTimePretty;
    private String serverDatePretty;
    private String serverDatePrettyFirstAction;
    private String serverTimePrettyFirstAction;
    private String userId;
    private String visitorType;
    private String visitorTypeIcon;
    private Integer visitConverted;
    private String visitConvertedIcon;
    private Integer visitCount;
    private String visitEcommerceStatus;
    private String visitEcommerceStatusIcon;
    private Integer daysSinceFirstVisit;
    private long secondsSinceFirstVisit;
    private Integer daysSinceLastEcommerceOrder;
    private long secondsSinceLastEcommerceOrder;
    private Integer visitDuration;
    private String visitDurationPretty;
    private Integer searches;
    private Integer actions;
    private Integer interactions;
    private String referrerType;
    private String referrerTypeName;
    private String referrerName;
    private String referrerKeyword;
    private Integer  referrerKeywordPosition;
    private String referrerUrl;
    private String referrerSearchEngineUrl;
    private String referrerSearchEngineIcon;
    private String referrerSocialNetworkUrl;
    private String referrerSocialNetworkIcon;
    private String languageCode;
    private String language;
    private String deviceType;
    private String deviceTypeIcon;
    private String deviceBrand;
    private String deviceModel;
    private String operatingSystem;
    private String operatingSystemName;
    private String operatingSystemIcon;
    private String operatingSystemCode;
    private String operatingSystemVersion;
    private String browserFamily;
    private String browserFamilyDescription;
    private String browser;
    private String browserName;
    private String browserIcon;
    private String browserCode;
    private String browserVersion;
    private Integer totalEcommerceRevenue;
    private Integer totalEcommerceConversions;
    private Integer totalEcommerceItems;
    private Integer totalAbandonedCartsRevenue;
    private Integer totalAbandonedCarts;
    private Integer totalAbandonedCartsItems;
    private Integer events;
    private String continent;
    private String continentCode;
    private String country;
    private String countryCode;
    private String countryFlag;
    private String region;
    private String regionCode;
    private String city;
    private String location;
    private String latitude;
    private String longitude;
    private String visitLocalTime;
    private String visitLocalHour;
    private Integer daysSinceLastVisit;
    private long secondsSinceLastVisit;
    private String resolution;
    private String plugins;
    private List<MatomoPlugin> pluginsIcons;
    private String dimension1;
    private List<MatomoExperiment> experiments;
    private String adClickId;
    private String adProviderId;
    private String adProviderName;
    private Integer crashes;
    private List<MatomoVariable> customVariables;
    private Integer formConversions;
    private String sessionReplayUrl;
    private String campaignId;
    private String campaignContent;
    private String campaignKeyword;
    private String campaignMedium;
    private String campaignName;
    private String campaignSource;
    private String campaignGroup;
    private String campaignPlacement;

    // getters and setters omitted for brevity

    @Data
    public static class MatomoAction {
        private String type;
        private String url;
        private String pageTitle;
        private Integer pageIdAction;
        private String idpageview;
        private String serverTimePretty;
        private Integer pageId;
        private Integer timeSpent;
        private String timeSpentPretty;
        private Integer pageviewPosition;
        private String title;
        private String subtitle;
        private String icon;
        private String iconSVG;
        private long timestamp;
        private String dimension2;
        private String dimension4;
        private String dimension5;

        // getters and setters omitted for brevity
    }

    @Data
    public static class MatomoPlugin {
        private String pluginIcon;
        private String pluginName;

        // getters and setters omitted for brevity
    }

    @Data
    public static class MatomoExperiment {
        // Placeholder for experiments if needed
    }

    @Data
    public static class MatomoVariable {
        // Placeholder for custom variables if needed
    }
}
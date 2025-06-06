package com.somle.autonomous.resp;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AutonomousProductResp {

    private Integer status;
    private ProductDataWrapper data;

    @Data
    @NoArgsConstructor
    public static class ProductDataWrapper {
        private List<Product> data;
        private Integer total;
    }

    @Data
    @NoArgsConstructor
    public static class Product {
        private String id;
        private LocalDateTime dateCreated;
        private LocalDateTime dateModified;
        private String modifiedUserId;
        private String createdUserId;
        private String seoH1;
        private String seoName;
        private String seoTitle;
        private String seoDescription;
        private String seoKeywords;
        private String seoImage;
        private String seoText;
        private String seoUrl;
        private String seoParagraph;
        private String seoSummary;
        private String seoCanonical;
        private String schemaMarkup;
        private String seoImageProductFeed;
        private String seoGtin;
        private Boolean noIndex;
        private String name;
        private String code;
        private String slug;
        private String sequenceCode;
        private String image;
        private List<String> categoryCodes;
        private List<String> categoryNames;
        private String description;
        private Boolean isFreeShipping;
        private String specialInfo;
        private String headerImage;
        private Integer label;
        private Integer priority;
        private String rawName;
        private String shareMessage;
        private String shortDescription;
        private Integer showTrialDay;
        private Map<String, Integer> trialDay;
        private Integer status;
        private String summary;
        private Integer warrantyMonth;
        private Map<String, Integer> warrantyMonthByRegion;
        private List<Video> videos;
        private List<Gallery> galleries;
        private List<String> packageCodes;
        private List<ProductUrl> productUrls;
        private List<HiddenOption> hiddenOptions;
        private String vendorCode;
        private String parentSequenceCode;
        private List<String> usageSlugs;
        private Boolean excludeApplyCredit;
        private List<String> aliasSkus;
        private List<Object> packageOptions; // 需要根据实际结构细化
        private List<Object> productOptions; // 需要根据实际结构细化
        private String categorySlug;
        private List<String> regions;
    }

    @Data
    @NoArgsConstructor
    public static class Video {
        // 根据实际视频数据结构添加字段
    }

    @Data
    @NoArgsConstructor
    public static class Gallery {
        private String alt;
        private String imageUrl;
        private String imageMobileUrl;
        private String imageHover;
        private Integer priority;
    }

    @Data
    @NoArgsConstructor
    public static class ProductUrl {
        private String seoH1;
        private String seoName;
        private String seoTitle;
        private String seoDescription;
        private String seoKeywords;
        private String seoImage;
        private String seoText;
        private String seoUrl;
        private String seoParagraph;
        private String seoSummary;
        private String seoCanonical;
        private String schemaMarkup;
        private String seoImageProductFeed;
        private String seoGtin;
        private Boolean noIndex;
        private String name;
        private String code;
        private String sequenceCode;
        private String slug;
        private String productDescription;
        private Price price;
        private Object shippingFee; // 需要根据实际结构细化
        private Price strikeThroughPrice;
        private Integer status;
        private Integer priority;
        private Map<String, PriorityByWarehouse> priorityByWarehouse;
        private List<Gallery> galleries;
        private String image;
        private Map<String, Integer> warrantyMonthByRegion;
        private List<String> inventoryCodes;
        private String gtin;
    }

    @Data
    @NoArgsConstructor
    public static class Price {
        private Integer usd;
        private Integer usdHawaii;
        private Integer usdPRico;
        private Integer usdAlaska;
        private Integer cad;
        private Integer gbp;
        private Integer eur;
        private Integer intl;
    }

    @Data
    @NoArgsConstructor
    public static class PriorityByWarehouse {
        private Integer selected;
        private Integer inStock;
        private LocalDateTime shippingDate;
        private Object shippingDateByWarehouse; // 需要根据实际结构细化
        private Integer transitDays;
        private Object transitDaysByWarehouse; // 需要根据实际结构细化
    }

    @Data
    @NoArgsConstructor
    public static class HiddenOption {
        private String packageCode;
        private String optionCode;
        private String valueCode;
        private Integer hiddenType;
        private List<String> locations;
    }
}
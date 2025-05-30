package com.somle.walmart.model.reps;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class WalmartItemDetailResp {

    private ItemResponse itemResponse;

    @Data
    @NoArgsConstructor
    public static class ItemResponse {
        private String gtin;
        private String sku;
        private String variantGroupId;
        private String productName;
        private String brand;
        private String mainImageUrl;
        private String shortDescription;
        private String productType;
        private String shelfName;
        private String publishedStatus;
        private List<String> unpublishedReasons;
        private String itemPageURL;
        private Price price;
        private String onePBuyBox;
        private SiteDates siteDates;
        private Double customerRating;
        private Integer customerReviewCount;
        private Double contentQualityScore;
        private WalmartOrderableAttributes walmartOrderableAttributes;
        private List<ItemConfigurations> itemConfigurations;
        private List<AttributeContentInsights> attributeContentInsights;
        private VariantGroupInfo variantGroupInfo;
        private Map<String, Object> additionalProductAttributes;
        //库存数量
        private Integer sellableQty;
        //售价
        private Double salePrice;
        //币种
        private String currency;
    }

    @Data
    @NoArgsConstructor
    public static class Price {
        private Double amount;
        private String currency;
    }

    @Data
    @NoArgsConstructor
    public static class SiteDates {
        private String siteStartDate;
        private String siteEndDate;
    }

    @Data
    @NoArgsConstructor
    public static class WalmartOrderableAttributes {
        private Double minimumAdvertisedPrice;
        private String signingDescription;
        private String shopDescription;
        private String goodsAndServicesType;
        private List<String> sustainabilityFeatureCode;
        private String flexibleSpendingAccountEligible;
        private List<StateRestrictions> stateRestrictions;
        private String chemicalAerosolPesticide;
        private String electronicsIndicator;
        private String batteryTechnologyType;
        private String hasFuelContainer;
        private String containsPaperWood;
        private String containsMercury;
        private String isCpscRegulatedInd;
        private String multipackIndicator;
        private String shipsInOriginalPackaging;
        private String packagingMarkedReturnableIndicator;
        private String isVarietyInd;
    }

    @Data
    @NoArgsConstructor
    public static class StateRestrictions {
        private String states;
        private String zipCodes;
        private String stateRestrictionsText;
    }

    @Data
    @NoArgsConstructor
    public static class ItemConfigurations {
        private String walmartItemNumber;
        private String channelSold;
        private String fulfillmentMethod;
        private String tradeItemSyncStatusCode;
        private String supplyItemEffectiveDate;
        private String supplyItemStatusCode;
        private String supplyItemStatusChangeDate;
        private String walmartDepartmentNumber;
        private String buyingRegionCode;
        private Integer finelineNbr;
        private String accountingDepartmentNbr;
        private String supplyItemCreateDate;
        private Integer seasonYear;
        private String seasonCode;
        private Double unitCost;
        private String countryOfOriginAssembly;
        private String batchNumberIndicator;
        private List<String> factoryId;
        private ShippingDimensions shippingDimensions;
        private String mustShipAlone;
        private Integer supplierMinimumOrderQuantity;
        private Integer replenishItemSubType;
        private String supplyItemExpireDate;
        private String informationProviderGLN;
        private String supplierStockID;
        private String consumerItemNumber;
        private String nationalDrugCode10;
        private String nationalDrugCode11;
        private String nationalDrugCode12;
        private Double markupPercentage;
        private Double prePriceLabelAmount;
        private List<String> countryOfOriginComponents;
        private Integer allowedTimeInWarehouseQty;
        private Double palletRoundingPercent;
        private Integer warehouseMinLifeRemainingtoReceiveQuantity;
        private String isConveyable;
        private String specialHandlingInstructionCode;
        private String idealTemperatureRangeLowQty;
        private String idealTemperatureRangeHighQty;
        private String isTemperatureSensitive;
        private String acceptableTemperatureRangeLowQty;
        private String acceptableTemperatureRangeHighQty;
        private String isOversized;
        private Integer exclusiveSupplyDCNbr;
        private String isMasterCarton;
        private Integer orderSizingFactorQty;
        private String rppc;
        private Integer warehousePackQuantity;
        private String warehousePackGtin;
        private String isBreakPack;
        private Integer palletTi;
        private Integer palletHi;
        private String orderableGTIN;
        private Integer vendorPackQuantity;
        private Each each;
        private InnerPack innerPack;
        private Case aCase;
        private Pallet pallet;
        private String publishedGTIN;
        private String palletSizeType;
        private Double tradeItemNetWeight;
        private String isDEAReported;
        private String orderablePackWeightFormat;
        private Integer storeMinimumLifeRemainingToReceive;
        private Integer plu;
        private String orderablePackType;
        private String sendTraitCodes;
        private String omitTraitCodes;
        private String destinationFormatCode;
        private String isCancelWhenOutInd;
        private String specialConsiderationCode;
        private String storeReceiptDescription;
        private String storeLabelDescription;
        private String shelf1Color;
        private String shelf2Size;
        private String isWeighableAtRegister;
        private String isBackroomScaleInd;
        private String hasRFID;
        private String hasSecurityTag;
        private String supplyItemSecondaryDescription;
        private String isShelfRotationInd;
        private String isCorporateReplenishable;
        private String isReplenishedByUnitInd;
        private String mbmTypeCode;
        private String replenishmentItemType;
        private String crushFactorCode;
        private String warehouseAlignmentCode;
        private String segregationCode;
        private String commodityId;
        private String warehouseAreaCode;
        private Integer warehouseRotationType;
        private List<EachDataCarriers> eachDataCarriers;
    }

    @Data
    @NoArgsConstructor
    public static class ShippingDimensions {
        private Double shippingDimensionsHeight;
        private Double shippingDimensionsWidth;
        private Double shippingDimensionsDepth;
        private Double shippingWeight;
    }

    @Data
    @NoArgsConstructor
    public static class Each {
        private String eachGTIN;
        private Double eachDepth;
        private Double eachWidth;
        private Double eachHeight;
        private Double eachWeight;
    }

    @Data
    @NoArgsConstructor
    public static class InnerPack {
        private String innerPackGTIN;
        private Double innerPackDepth;
        private Double innerPackWidth;
        private Double innerPackHeight;
        private Double innerPackWeight;
        private Integer qtySellableItemsInnerPack;
    }

    @Data
    @NoArgsConstructor
    public static class Case {
        private String caseGTIN;
        private Double caseDepth;
        private Double caseWidth;
        private Double caseHeight;
        private Double caseWeight;
        private Integer qtySellableItemsCase;
    }

    @Data
    @NoArgsConstructor
    public static class Pallet {
        private String palletGTIN;
        private Double palletDepth;
        private Double palletWidth;
        private Double palletHeight;
        private Double palletWeight;
        private Integer qtySellableItemsPallet;
    }

    @Data
    @NoArgsConstructor
    public static class EachDataCarriers {
        private String dataCarrierFamilyTypeCode;
        private String applicationIdentifierTypeCode;
    }

    @Data
    @NoArgsConstructor
    public static class AttributeContentInsights {
        private String attributeName;
        private List<ContentImprovements> contentImprovements;
    }

    @Data
    @NoArgsConstructor
    public static class ContentImprovements {
        private String issueType;
        private String issueDescription;
    }

    @Data
    @NoArgsConstructor
    public static class VariantGroupInfo {
        private Boolean isPrimary;
        private List<Items> items;
    }

    @Data
    @NoArgsConstructor
    public static class Items {
        private String name;
        private String value;
    }
}
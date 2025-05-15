package com.somle.amazon.model.reps;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AmazonSpListingRepsVO {

    private Integer numberOfResults;
    private Pagination pagination;
    private List<ProductItem> items;

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Pagination {
        private String nextToken;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ProductItem {
        private String sku;
        private List<ProductSummary> summaries;
        private ProductAttributes attributes;
        private List<Offer> offers;
        private List<FulfillmentAvailability> fulfillmentAvailability;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ProductAttributes {
        private List<FulfillmentAvailability> fulfillmentAvailability;
        private List<ItemWeight> itemWeight;
        private List<BulletPoint> bulletPoints;
        private List<ItemDimensions> itemDimensions;
        private List<ProductDescription> productDescriptions;
        private List<Brand> brand;
        private List<GenericKeyword> genericKeywords;
        private List<DeskDesign> deskDesigns;
        private List<CountryOfOrigin> countryOfOrigin;
        private List<ExternallyAssignedProductIdentifier> externallyAssignedProductIdentifiers;
        private List<GiftOptions> giftOptions;
        private List<ConditionType> conditionTypes;
        private List<ItemPackageDimensions> itemPackageDimensions;
        private List<Size> sizes;
        private List<PartNumber> partNumbers;
        private List<Style> styles;
        private List<Color> colors;
        private List<VariationTheme> variationThemes;
        private List<ItemPackageWeight> itemPackageWeights;
        private List<Manufacturer> manufacturers;
        private List<NumberOfBoxes> numberOfBoxes;
        private List<RecommendedBrowseNodes> recommendedBrowseNodes;
        private List<MaterialComposition> materialCompositions;
        private List<ModelNumber> modelNumbers;
        private List<SupplierDeclaredDgHzRegulation> supplierDeclaredDgHzRegulations;
        private List<GdprRisk> gdprRisks;
        private List<WeightCapacity> weightCapacities;
        private List<ItemName> itemNames;
        private List<MerchantShippingGroup> merchantShippingGroups;
        private List<ListPrice> listPrices;
        private List<BatteriesRequired> batteriesRequired;
        private List<ProductSiteLaunchDate> productSiteLaunchDates;
        private List<MerchantSuggestedAsin> merchantSuggestedAsins;
        private List<Material> materials;
        private List<MaxOrderQuantity> maxOrderQuantities;
        private List<IsStainResistant> isStainResistant;
        private List<BatteriesIncluded> batteriesIncluded;
        private List<ImageLocator> imageLocatorPs02;
        private List<ImageLocator> imageLocatorPs01;
        private List<ImageLocator> otherProductImageLocator6;
        private List<ImageLocator> otherProductImageLocator5;
        private List<ImageLocator> otherProductImageLocator4;
        private List<ImageLocator> otherProductImageLocator3;
        private List<ImageLocator> otherProductImageLocator2;
        private List<ImageLocator> otherProductImageLocator1;
        private List<ImageLocator> mainProductImageLocator;
        private List<ImageLocator> swatchProductImageLocator;
        private List<PurchasableOffer> purchasableOffers;
        private List<ParentageLevel> parentageLevels;
        private List<ChildParentSkuRelationship> childParentSkuRelationships;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ProductSummary {
        private String marketplaceId;
        private String asin;
        private String productType;
        private String conditionType;
        private List<String> status;
        private String fnSku;
        private String itemName;
        private String createdDate;
        private String lastUpdatedDate;
        private MainImage mainImage;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class MainImage {
        private String link;
        private Integer height;
        private Integer width;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class FulfillmentAvailability {
        private String fulfillmentChannelCode;
        private Integer quantity;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ItemWeight {
        private String unit;
        private String value;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class BulletPoint {
        private String languageTag;
        private String value;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ItemDimensions {
        private Dimension height;
        private Dimension length;
        private Dimension width;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Dimension {
        private String unit;
        private String value;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ProductDescription {
        private String languageTag;
        private String value;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Brand {
        private String languageTag;
        private String value;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class GenericKeyword {
        private String languageTag;
        private String value;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class DeskDesign {
        private String value;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CountryOfOrigin {
        private String value;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ExternallyAssignedProductIdentifier {
        private String value;
        private String type;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class GiftOptions {
        private String canBeMessaged;
        private String canBeWrapped;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ConditionType {
        private String value;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ItemPackageDimensions {
        private Dimension height;
        private Dimension length;
        private Dimension width;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Size {
        private String languageTag;
        private String value;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PartNumber {
        private String value;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Style {
        private String languageTag;
        private String value;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Color {
        private String languageTag;
        private List<String> standardizedValues;
        private String value;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class VariationTheme {
        private String name;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ItemPackageWeight {
        private String unit;
        private String value;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Manufacturer {
        private String languageTag;
        private String value;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class NumberOfBoxes {
        private Integer value;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class RecommendedBrowseNodes {
        private String value;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class MaterialComposition {
        private String languageTag;
        private String value;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ModelNumber {
        private String value;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SupplierDeclaredDgHzRegulation {
        private String value;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class GdprRisk {
        private String value;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class WeightCapacity {
        private List<Maximum> maximum;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Maximum {
        private String unit;
        private String value;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ItemName {
        private String languageTag;
        private String value;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class MerchantShippingGroup {
        private String value;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ListPrice {
        private String currency;
        private Double valueWithTax;
        private Double value;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class BatteriesRequired {
        private String value;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ProductSiteLaunchDate {
        private String value;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class MerchantSuggestedAsin {
        private String value;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Material {
        private String languageTag;
        private String value;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class MaxOrderQuantity {
        private String value;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class IsStainResistant {
        private String value;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class BatteriesIncluded {
        private String value;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ImageLocator {
        private String mediaLocation;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PurchasableOffer {
        private String currency;
        private DateValue startAt;
        private DateValue endAt;
        private String audience;
        private List<DiscountedPrice> discountedPrice;
        private List<OurPrice> ourPrice;
        private List<QuantityDiscountPlan> quantityDiscountPlan;
        private String marketplaceId;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class DateValue {
        private String value;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class DiscountedPrice {
        private List<PriceSchedule> schedule;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class OurPrice {
        private List<PriceSchedule> schedule;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PriceSchedule {
        private String endAt;
        private String startAt;
        private Double valueWithTax;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class QuantityDiscountPlan {
        private List<DiscountSchedule> schedule;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class DiscountSchedule {
        private String discountType;
        private List<DiscountLevel> levels;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class DiscountLevel {
        private Integer lowerBound;
        private Double value;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ParentageLevel {
        private String marketplaceId;
        private String value;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ChildParentSkuRelationship {
        private String marketplaceId;
        private String childRelationshipType;
        private String parentSku;
    }

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Offer {
        private String marketplaceId;
        private String offerType;
        private Price price;
        private Audience audience;

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class Price {
            private String currency;
            private String currencyCode;
            private String amount;
        }

        @NoArgsConstructor
        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class Audience {
            private String value;
            private String displayName;
        }
    }
}

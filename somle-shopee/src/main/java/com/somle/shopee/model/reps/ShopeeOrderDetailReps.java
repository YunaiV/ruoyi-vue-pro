package com.somle.shopee.model.reps;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ShopeeOrderDetailReps {
    private String error;
    private String message;
    private Response response;
    private String requestId;

    @Data
    @NoArgsConstructor
    public static class Response {
        private List<Order> orderList;
    }

    @Data
    @NoArgsConstructor
    public static class Order {
        private Boolean actualShippingFeeConfirmed;
        private String buyerCancelReason;
        private Long buyerCpfId;
        private Long buyerUserId;
        private String buyerUsername;
        private String cancelBy;
        private String cancelReason;
        private Boolean cod;
        private Long createTime;
        private String currency;
        private Integer daysToShip;
        private String dropshipper;
        private String dropshipperPhone;
        private BigDecimal estimatedShippingFee;
        private String fulfillmentFlag;
        private Boolean goodsToDeclare;
        private Object invoiceData; // 根据实际数据结构替换
        private List<Item> itemList;
        private String messageToSeller;
        private String note;
        private Long noteUpdateTime;
        private String orderSn;
        private String orderStatus;
        private List<OrderPackage> packageList;
        private Long payTime;
        private String paymentMethod;
        private Long pickupDoneTime;
        private RecipientAddress recipientAddress;
        private String region;
        private BigDecimal reverseShippingFee;
        private Long shipByDate;
        private String shippingCarrier;
        private Boolean splitUp;
        private BigDecimal totalAmount;
        private Long updateTime;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Item {
        private Boolean addOnDeal;
        private Long addOnDealId;
        private ImageInfo imageInfo;
        private Boolean isB2cOwnedItem;
        private Boolean isPrescriptionItem;
        private Long itemId;
        private String itemName;
        private String itemSku;
        private Boolean mainItem;
        private BigDecimal modelDiscountedPrice;
        private Long modelId;
        private String modelName;
        private BigDecimal modelOriginalPrice;
        private Integer modelQuantityPurchased;
        private String modelSku;
        private Long orderItemId;
        private List<String> productLocationId;
        private Long promotionGroupId;
        private Long promotionId;
        private String promotionType;
        private BigDecimal weight;
        private Boolean wholesale;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ImageInfo {
        private String imageUrl;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class OrderPackage {
        private String groupShipmentId;
        private List<PackageItem> itemList;
        private String logisticsStatus;
        private String packageNumber;
        private Integer parcelChargeableWeightGram;
        private String shippingCarrier;
        private Long logisticsChannelId;
        private Boolean allowSelfDesignAwb;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PackageItem {
        private Long itemId;
        private Long modelId;
        private Integer modelQuantity;
        private Long orderItemId;
        private String productLocationId;
        private Long promotionGroupId;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class RecipientAddress {
        private String city;
        private String district;
        private String fullAddress;
        private String name;
        private String phone;
        private String region;
        private String state;
        private String town;
        private String zipcode;
    }
}
package com.somle.rakuten.model.reps;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RakutenOrderRepsVO {

    private Integer version;
    private List<MessageModel> MessageModelList;
    private List<OrderModel> OrderModelList;

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class MessageModel {
        private String messageType;
        private String messageCode;
        private String message;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class OrderModel {
        private String orderNumber;
        private Integer orderProgress;
        private Integer subStatusId;
        private String subStatusName;
        private LocalDateTime orderDatetime;
        private String shopOrderCfmDatetime;
        private String orderFixDatetime;
        private String shippingInstDatetime;
        private String shippingCmplRptDatetime;
        private String cancelDueDate;
        private String deliveryDate;
        private Integer shippingTerm;
        private String remarks;
        private Integer giftCheckFlag;
        private Integer severalSenderFlag;
        private Integer equalSenderFlag;
        private Integer isolatedIslandFlag;
        private Integer rakutenMemberFlag;
        private Integer carrierCode;
        private Integer emailCarrierCode;
        private Integer orderType;
        private String reserveNumber;
        private Integer reserveDeliveryCount;
        private Integer cautionDisplayType;
        private Integer cautionDisplayDetailType;
        private Integer rakutenConfirmFlag;
        private Integer goodsPrice;
        private Integer postagePrice;
        private Integer deliveryPrice;
        private Integer paymentCharge;
        private Double paymentChargeTaxRate;
        private Integer totalPrice;
        private Integer requestPrice;
        private Integer couponAllTotalPrice;
        private Integer couponShopPrice;
        private Integer couponOtherPrice;
        private Integer additionalFeeOccurAmountToUser;
        private Integer additionalFeeOccurAmountToShop;
        private Integer asurakuFlag;
        private Integer drugFlag;
        private Integer dealFlag;
        private Integer deliveryCertPrgFlag;
        private Integer oneDayOperationFlag;
        private Integer membershipType;
        private String memo;
        private String operator;
        private String mailPlugSentence;
        private Integer modifyFlag;
        private Integer receiptIssueCount;
        private List<?> receiptIssueHistoryList;

        private OrdererModel OrdererModel;
        private SettlementModel SettlementModel;
        private DeliveryModel DeliveryModel;
        private PointModel PointModel;
        private WrappingModel WrappingModel1;
        private WrappingModel WrappingModel2;
        private List<PackageModel> PackageModelList;
        private List<CouponModel> CouponModelList;
        private List<ChangeReasonModel> ChangeReasonModelList;
        private List<DueDateModel> DueDateModelList;
        private List<TaxSummaryModel> TaxSummaryModelList;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class OrdererModel {
        private String zipCode1;
        private String zipCode2;
        private String prefecture;
        private String city;
        private String subAddress;
        private String familyName;
        private String firstName;
        private String familyNameKana;
        private String firstNameKana;
        private String phoneNumber1;
        private String phoneNumber2;
        private String phoneNumber3;
        private String emailAddress;
        private String sex;
        private Integer birthYear;
        private Integer birthMonth;
        private Integer birthDay;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SettlementModel {
        private Integer settlementMethodCode;
        private String settlementMethod;
        private Integer rpaySettlementFlag;
        private String cardName;
        private String cardNumber;
        private String cardOwner;
        private String cardYm;
        private Integer cardPayType;
        private String cardInstallmentDesc;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class DeliveryModel {
        private String deliveryName;
        private String deliveryClass;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PointModel {
        private Integer usedPoint;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class WrappingModel {
        private Integer title;
        private String name;
        private Integer price;
        private Integer includeTaxFlag;
        private Integer deleteWrappingFlag;
        private Double taxRate;
        private Integer taxPrice;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PackageModel {
        private String basketId;
        private Integer postagePrice;
        private Double postageTaxRate;
        private Integer deliveryPrice;
        private Double deliveryTaxRate;
        private Integer goodsPrice;
        private Integer totalPrice;
        private String noshi;
        private String defaultDeliveryCompanyCode;
        private Integer dropOffFlag;
        private String dropOffLocation;
        private Integer packageDeleteFlag;

        private SenderModel SenderModel;
        private List<ItemModel> ItemModelList;
        private List<ShippingModel> ShippingModelList;
        private DeliveryCvsModel DeliveryCvsModel;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SenderModel {
        private String zipCode1;
        private String zipCode2;
        private String prefecture;
        private String city;
        private String subAddress;
        private String familyName;
        private String firstName;
        private String familyNameKana;
        private String firstNameKana;
        private String phoneNumber1;
        private String phoneNumber2;
        private String phoneNumber3;
        private Integer isolatedIslandFlag;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ItemModel {
        private String itemDetailId;
        private String itemName;
        private String itemId;
        private String itemNumber;
        private String manageNumber;
        private Integer price;
        private Integer units;
        private Integer includePostageFlag;
        private Integer includeTaxFlag;
        private Integer includeCashOnDeliveryPostageFlag;
        private String selectedChoice;
        private Integer pointRate;
        private Integer pointType;
        private Integer inventoryType;
        private String delvdateInfo;
        private Integer restoreInventoryFlag;
        private Integer dealFlag;
        private Integer drugFlag;
        private Integer deleteItemFlag;
        private Double taxRate;
        private Integer priceTaxIncl;
        private Integer isSingleItemShipping;

        private List<SkuModel> SkuModelList;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SkuModel {
        private String variantId;
        private String merchantDefinedSkuId;
        private String skuInfo;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ShippingModel {
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class DeliveryCvsModel {
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CouponModel {
        private String couponCode;
        private String itemId;
        private String couponName;
        private String couponSummary;
        private String couponCapital;
        private Integer couponCapitalCode;
        private String expiryDate;
        private Integer couponPrice;
        private Integer couponUnit;
        private Integer couponTotalPrice;
        private String itemDetailId;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ChangeReasonModel {
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class DueDateModel {
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class TaxSummaryModel {
        private Double taxRate;
        private Integer reqPrice;
        private Integer reqPriceTax;
        private Integer totalPrice;
        private Integer paymentCharge;
        private Integer couponPrice;
        private Integer point;
    }
}
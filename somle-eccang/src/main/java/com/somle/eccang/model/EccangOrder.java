package com.somle.eccang.model;

import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.annotation.JSONType;

import lombok.Data;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EccangOrder {
    private String productCount;
    private String platformShipStatus;
    private String sellerRebate;
    private String createType;
    private String buyerName;
    private String outboundBatchCarrier;
    private String billNo;
    private String buyerId;
    private String dateCreateSys;
    private String customerServiceNote;
    private String shippingMethod;
    private String fbaFee;
    private String trackStatus;
    private String orderWeight;
    private String systemTagZh;
    private String orderType;
    private String referenceNo;
    private String platformPaidDate;
    private String processAgain;
    private String dateCreatePlatform;
    private String customTagEn;
    private String paypalAccount;
    private String tax;
    private String updateDate;
    private String referenceNoSys;
    private String orderCode;
    private String shipFee;
    private String countryCode;
    private String fulfillmentType;
    private String trackDeliveredTime;
    private String subtotal;
    private String amountPaid;
    private String orderId;
    private String status;
    private String paypalTransactionId;
    private String platformShipDate;
    private String shippingMethodNo;
    private String platformFeeTotal;
    private String warehouseCode;
    private String abnormalType;
    private String isCod;
    private List<EccangOrderDetail> orderDetails;
    private String platform;
    private String customTagZh;
    private EccangAddress orderAddress;
    private String companyCode;
    private String currency;
    private String creatorUserCode;
    private String isMark;
    private String otherFee;
    private String buyerMail;
    private String systemTagEn;
    private String platformUserName;
    private String isTransferFbaDelivery;
    private String discountVal;
    private String platformLatestShipDate;
    private String shippingMethodPlatform;
    private String orderDesc;
    private String creatorUserNameEn;
    private String finalValueFeeTotal;
    private String creatorUserName;
    private String site;
    private String dateCreate;
    private String outboundBatchTrackingNo;
    private String userAccount;
    private String costShipFee;
    private String systemNote;
    private String warehouseId;

    public EccangOrderDetail getOrderDetail() {
        return getOrderDetails().get(0);
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class EccangOrderDetail {
        private String opRefItemLocation;
        private String productSkuInfo;
        private List<WarehouseSku> warehouseSkuList;
        private String productUrl;
        private String unitFinalValueFee;
        private String opSite;
        private String orderCodeOrg;
        private String productSkuOrg;
        private String opId;
        private String pic;
        private String opRefItemId;
        private String unitPrice;
        private String unitPlatformFee;
        private String productTitle;
        private String orderCode;
        private String updateTime;
        private String productSkuList;
        private String opRefTnx;
        private String qty;
        private String action;
        private String platformSku;
        private String productSkuQtyList;
        private String productSkuOrgQty;

        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class WarehouseSku {
            private String productLevel;
            private String warehouseSku;
            private int warehouseSkuQty;
        }
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class EccangAddress {
        private String shippingAddressId;
        private String updateDate;
        private String countryCode;
        private String cityName;
        private String phone;
        private String companyName;
        private String district;
        private String name;
        private String countryName;
        private String state;
        private String createdDate;
        private String line3;
        private String postalCode;
        private String line2;
        private String line1;
    }
}

package com.somle.amazon.controller.vo;


import lombok.Data;

import java.util.List;

@Data
public class AmazonSpOrderRespVO {
    private Payload payload;

    @Data
    public static class Payload {
        private List<Order> Orders;
        private String NextToken;
        private String CreatedBefore;
    }

    @Data
    public static class Order {
        private BuyerInfo BuyerInfo;
        private String AmazonOrderId;
        private String EarliestShipDate;
        private String SalesChannel;
        private String OrderStatus;
        private Integer NumberOfItemsShipped;
        private String OrderType;
        private Boolean IsPremiumOrder;
        private Boolean IsPrime;
        private String FulfillmentChannel;
        private Integer NumberOfItemsUnshipped;
        private Boolean HasRegulatedItems;
        private String IsReplacementOrder;
        private Boolean IsSoldByAB;
        private String LatestShipDate;
        private String ShipServiceLevel;
        private Boolean IsISPU;
        private String MarketplaceId;
        private String PurchaseDate;
        private ShippingAddress ShippingAddress;
        private Boolean IsAccessPointOrder;
        private String SellerOrderId;
        private String PaymentMethod;
        private Boolean IsBusinessOrder;
        private OrderTotal OrderTotal;
        private List<String> PaymentMethodDetails;
        private Boolean IsGlobalExpressEnabled;
        private String LastUpdateDate;
        private String ShipmentServiceLevelCategory;
        private List<AmazonSpOrderItemRespVO.OrderItem> orderItems;
    }

    @Data
    public static class BuyerInfo {
        private String BuyerEmail;
    }

    @Data
    public static class ShippingAddress {
        private String StateOrRegion;
        private String PostalCode;
        private String CountryCode;
        private String City; // 可选字段，部分地址包含
    }

    @Data
    public static class OrderTotal {
        private String CurrencyCode;
        private String Amount;
    }


}

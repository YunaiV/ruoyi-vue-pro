package com.somle.amazon.controller.vo;


import lombok.Data;

import java.time.LocalDateTime;
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
        private LocalDateTime EarliestShipDate;
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
        private LocalDateTime LatestShipDate;
        private String ShipServiceLevel;
        private Boolean IsISPU;
        private String MarketplaceId;
        //最迟送达时间
        private LocalDateTime LatestDeliveryDate;
        private LocalDateTime PurchaseDate;
        private ShippingAddress ShippingAddress;
        private Boolean IsAccessPointOrder;
        private String SellerOrderId;
        private String PaymentMethod;
        private Boolean IsBusinessOrder;
        private OrderTotal OrderTotal;
        private List<String> PaymentMethodDetails;
        private Boolean IsGlobalExpressEnabled;
        private LocalDateTime LastUpdateDate;
        private String ShipmentServiceLevelCategory;
        private List<AmazonSpOrderItemRespVO.OrderItem> orderItems;
    }

    @Data
    public static class BuyerInfo {
        private String BuyerEmail;
        private String BuyerName;
        private BuyerTaxInfo BuyerTaxInfo;
        private String PurchaseOrderNumber;
    }


    @Data
    public class BuyerTaxInfo {
        private String CompanyLegalName;
    }

    @Data
    public static class ShippingAddress {
        private String StateOrRegion;
        private String PostalCode;
        private String CountryCode;
        private String City;
        private String Name;
        private String AddressLine1;
    }

    @Data
    public static class OrderTotal {
        private String CurrencyCode;
        private String Amount;
    }


}

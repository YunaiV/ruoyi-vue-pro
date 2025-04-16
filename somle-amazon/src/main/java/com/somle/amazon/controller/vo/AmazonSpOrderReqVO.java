package com.somle.amazon.controller.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
// All time is UTC
public class AmazonSpOrderReqVO {
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
    private LocalDateTime lastUpdatedAfter;
    private LocalDateTime lastUpdatedBefore;
    private List<OrderStatus> orderStatuses;
    private List<String> marketplaceIds;
    private List<FulfillmentChannel> fulfillmentChannels;
    private List<PaymentMethod> paymentMethods;
    private String buyerEmail;
    private String sellerOrderId;
    private Integer maxResultsPerPage = 100;
    private List<EasyShipShipmentStatus> easyShipShipmentStatuses;
    private List<ElectronicInvoiceStatus> electronicInvoiceStatuses;
    private String nextToken;
    private List<String> amazonOrderIds;
    private String actualFulfillmentSupplySourceId;
    private Boolean isISPU;
    private String storeChainStoreId;
    private LocalDateTime earliestDeliveryDateBefore;
    private LocalDateTime earliestDeliveryDateAfter;
    private LocalDateTime latestDeliveryDateBefore;
    private LocalDateTime latestDeliveryDateAfter;

//    private LocalDateTime CreatedAfter;
//    private LocalDateTime CreatedBefore;
//    private LocalDateTime LastUpdatedAfter;
//    private LocalDateTime LastUpdatedBefore;
//    private List<OrderStatus> OrderStatuses;
//    private List<String> MarketplaceIds;
//    private List<FulfillmentChannel> FulfillmentChannels;
//    private List<PaymentMethod> PaymentMethods;
//    private String BuyerEmail;
//    private String SellerOrderId;
//    private Integer MaxResultsPerPage = 100;
//    private List<EasyShipShipmentStatus> EasyShipShipmentStatuses;
//    private List<ElectronicInvoiceStatus> ElectronicInvoiceStatuses;
//    private String NextToken;
//    private List<String> AmazonOrderIds;
//    private String ActualFulfillmentSupplySourceId;
//    private Boolean IsISPU;
//    private String StoreChainStoreId;
//    private LocalDateTime EarliestDeliveryDateBefore;
//    private LocalDateTime EarliestDeliveryDateAfter;
//    private LocalDateTime LatestDeliveryDateBefore;
//    private LocalDateTime LatestDeliveryDateAfter;


    // Enums for various statuses
    public enum OrderStatus {
        PENDING_AVAILABILITY,
        PENDING,
        UNSHIPPED,
        PARTIALLY_SHIPPED,
        SHIPPED,
        INVOICE_UNCONFIRMED,
        CANCELED,
        UNFULFILLABLE
    }

    public enum FulfillmentChannel {
        AFN, // Fulfilled by Amazon
        MFN  // Fulfilled by Seller
    }

    public enum PaymentMethod {
        COD,     // Cash on Delivery
        CVS,     // Convenience Store
        OTHER
    }

    public enum EasyShipShipmentStatus {
        PENDING_SCHEDULE,
        PENDING_PICKUP,
        PENDING_DROPOFF,
        LABEL_CANCELED,
        PICKED_UP,
        DROPPED_OFF,
        AT_ORIGIN_FC,
        AT_DESTINATION_FC,
        DELIVERED,
        REJECTED_BY_BUYER,
        UNDELIVERABLE,
        RETURNING_TO_SELLER,
        RETURNED_TO_SELLER,
        LOST,
        OUT_FOR_DELIVERY,
        DAMAGED
    }

    public enum ElectronicInvoiceStatus {
        NOT_REQUIRED,
        NOT_FOUND,
        PROCESSING,
        ERRORED,
        ACCEPTED
    }
}

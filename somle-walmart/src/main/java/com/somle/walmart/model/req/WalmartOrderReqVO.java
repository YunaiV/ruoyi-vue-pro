package com.somle.walmart.model.req;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class WalmartOrderReqVO {
    private String sku; // A seller-provided Product ID
    private String customerOrderId; // The customer order ID
    private String purchaseOrderId; // The purchase order ID. One customer may have multiple purchase orders.
    private String status; // Status of purchase order line. Valid statuses are: Created, Acknowledged, Shipped, Delivered, and Cancelled.
    private LocalDateTime createdStartDate; // Fetches all purchase orders created after this date. Default: current date - 7 days.
    private LocalDateTime createdEndDate; // Fetches all purchase orders created before this date. Default: current date.
    private LocalDateTime fromExpectedShipDate; // Fetches all purchase orders with order lines expected to ship after this date.
    private LocalDateTime toExpectedShipDate; // Fetches all purchase orders with order lines expected to ship before this date.
    private LocalDateTime lastModifiedStartDate; // Fetches all purchase orders modified after this date.
    private LocalDateTime lastModifiedEndDate; // Fetches all purchase orders modified before this date.
    private Integer limit; // Default: "100". The number of orders to be returned. Max: 200.
    private String productInfo = "false"; // Default: "false". Provides image URL and product weight in response if available.
    private String shipNodeType = "SellerFulfilled"; // Default: "SellerFulfilled". Specifies shipNode type. Values: SellerFulfilled, WFSFulfilled, 3PLFulfilled.
    private String shippingProgramType; // Specifies the type of program. Allowed values: TWO_DAY, ONE_DAY.
    private String replacementInfo = "false"; // Default: "false". Provides attributes related to Replacement order if available.
    private String orderType; // Specifies if the order is REGULAR, REPLACEMENT, or PREORDER. Depends on replacementInfo=true.
}

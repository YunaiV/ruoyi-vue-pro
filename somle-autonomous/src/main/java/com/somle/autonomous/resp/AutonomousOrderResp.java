package com.somle.autonomous.resp;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AutonomousOrderResp {

    private int status;
    private OrderData data;

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class OrderData {
        private List<OrderDetail> orderDetails;
        private int totalRecords;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class OrderDetail {
        // 主订单字段
        private String orderId;
        private String orderCode;
        private String orderDetailId;
        private String orderDetailCode;
        private String status;
        private Integer statusCode;
        private BigDecimal discountPrice;
        private BigDecimal tax;
        private BigDecimal amount;
        private String currency;
        private String currencySymbol;
        private LocalDateTime dateCreated;

        // 地址信息
        private String apartmentOrSuite;
        private String shippingAddress;
        private String stateRegion;
        private String postalCode;
        private String country;
        private String city;
        private String shippingName;

        // 联系信息
        private String phone;
        private String email;
        private String fullName;

        // 商品信息
        private Integer quantity;
        private List<PackageOptionValue> packageOptionValues;
        private Map<String, String> skus;
        private String optionName;
        private String productUrlCode;
        private String productCode;
        private String productName;
        private BigDecimal productPrice;

        // 分类信息
        private String categoryName;
        private String categorySlug;
        private String productSlug;
        private String categoryCode;

        // 业务标识
        private Boolean isBulkOrder;
        private Boolean isGift;
        private String vendorCode;

        // 嵌套对象定义
        @Data
        @NoArgsConstructor
        public static class PackageOptionValue {
            private String optionName;
            private String optionValue;
        }
    }

}
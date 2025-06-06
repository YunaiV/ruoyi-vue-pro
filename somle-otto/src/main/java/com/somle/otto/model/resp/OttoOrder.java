package com.somle.otto.model.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

// 主类，表示销售订单
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OttoOrder {
    private String salesOrderId; // 销售订单ID
    private String orderNumber;   // 订单编号
    private LocalDateTime orderDate;     // 订单日期
    private String lastModifiedDate; // 最后修改日期
    private List<PositionItem> positionItems; // 订单项列表
    private OrderLifecycleInformation orderLifecycleInformation; // 订单生命周期信息
    private List<DeliveryFee> initialDeliveryFees; // 初始配送费用列表
    private Address deliveryAddress; // 配送地址
    private Address invoiceAddress; // 发票地址
    private Payment payment; // 支付信息
    private List<Link> links; // 链接列表 下一页

    // 嵌套类，表示订单项
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PositionItem {
        private String positionItemId; // 订单项ID
        private String fulfillmentStatus; // 履行状态
        private ItemValueGrossPrice itemValueGrossPrice; // 商品总价（含税）
        private Product product; // 产品信息
        private String expectedDeliveryDate; // 预计交货日期
        private String cancellationDate; // 取消日期
        private String cancellationReason; // 取消原因
        private String processableDate; // 可处理日期
        private boolean weeePickup; // 是否需要WEEE回收
    }

    // 嵌套类，表示商品总价（含税）
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemValueGrossPrice {
        private double amount; // 金额
        private String currency; // 货币
    }

    // 嵌套类，表示产品信息
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Product {
        private String sku; // SKU编码
        private String productTitle; // 产品标题
        private String articleNumber; // 文章编号
        private String ean; // EAN码
        private double vatRate; // 增值税率
        private List<Dimension> dimensions; // 产品维度列表
        private String shopUrl; // 产品链接
    }

    // 嵌套类，表示产品维度
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Dimension {
        private String type; // 维度类型
        private String displayName; // 显示名称
        private String value; // 维度值
    }

    // 嵌套类，表示订单生命周期信息
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderLifecycleInformation {
        private String lifecycleChangeDate; // 生命周期变更日期
    }

    // 嵌套类，表示配送费用
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryFee {
        private String name; // 费用名称
        private ItemValueGrossPrice deliveryFeeAmount; // 配送费用金额
        private List<String> positionItemIds; // 相关订单项ID列表
        private double vatRate; // 增值税率
    }

    // 嵌套类，表示地址信息
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {
        private String salutation; // 称谓
        private String lastName; // 姓
        private String firstName; // 名
        private String street; // 街道
        private String addition; // 附加信息
        private String houseNumber; // 门牌号
        private String city; // 城市
        private String zipCode; // 邮政编码
        private String countryCode; // 国家代码
        private String email; // 电子邮件（可选字段）
    }

    // 嵌套类，表示支付信息
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payment {
        private String paymentMethod; // 支付方式
    }

    // 嵌套类，表示链接信息
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Link {
        private String rel; // 关系
        private String href; // 链接地址
    }
}
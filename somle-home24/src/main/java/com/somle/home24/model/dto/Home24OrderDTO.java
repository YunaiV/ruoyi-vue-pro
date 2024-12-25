package com.somle.home24.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// 订单类
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Home24OrderDTO {

    // 订单接受决定时间
    @JsonProperty("acceptance_decision_date")
    private String acceptanceDecisionDate;

    // 是否可以取消
    @JsonProperty("can_cancel")
    private boolean canCancel;

    // 是否可以直接发货
    @JsonProperty("can_shop_ship")
    private boolean canShopShip;

    // 渠道信息
    private Channel channel;

    // 商业ID
    @JsonProperty("commercial_id")
    private String commercialId;

    // 创建时间
    @JsonProperty("created_date")
    private String createdDate;

    // 货币ISO代码
    @JsonProperty("currency_iso_code")
    private String currencyIsoCode;

//    // 顾客信息
//    private Customer customer;

    // 顾客扣费时间
    @JsonProperty("customer_debited_date")
    private String customerDebitedDate;

    // 顾客是否直接向卖家付款
    @JsonProperty("customer_directly_pays_seller")
    private boolean customerDirectlyPaysSeller;

    // 顾客通知的电子邮件
    @JsonProperty("customer_notification_email")
    private String customerNotificationEmail;

    // 交货日期
    private DeliveryDate deliveryDate;

    // 履约信息
    private Fulfillment fulfillment;

    // 是否已完全退款
    @JsonProperty("fully_refunded")
    private boolean fullyRefunded;

    // 是否有顾客留言
    @JsonProperty("has_customer_message")
    private boolean hasCustomerMessage;

    // 是否有事故
    @JsonProperty("has_incident")
    private boolean hasIncident;

    // 是否有发票
    @JsonProperty("has_invoice")
    private boolean hasInvoice;

    // 最后更新时间
    @JsonProperty("last_updated_date")
    private String lastUpdatedDate;

    // 发货准备时间（天）
    @JsonProperty("leadtime_to_ship")
    private int leadtimeToShip;

    // 订单附加字段
    @JsonProperty("order_additional_fields")
    private List<String> orderAdditionalFields;

    // 订单ID
    @JsonProperty("order_id")
    private String orderId;

    // 订单行信息
    @JsonProperty("order_lines")
    @JsonIgnoreProperties(ignoreUnknown = true)
    private List<OrderLine> orderLines;

    // 订单退款信息
    @JsonProperty("order_refunds")
    private String orderRefunds;

    // 订单状态
    @JsonProperty("order_state")
    private String orderState;

    // 订单状态原因代码
    @JsonProperty("order_state_reason_code")
    private String orderStateReasonCode;

    // 订单状态原因标签
    @JsonProperty("order_state_reason_label")
    private String orderStateReasonLabel;

    // 订单税务模式
    @JsonProperty("order_tax_mode")
    private String orderTaxMode;

    // 订单税务信息
    @JsonProperty("order_taxes")
    private String orderTaxes;

    // 支付方式
    @JsonProperty("payment_type")
    private String paymentType;

    // 支付方式标签
    @JsonProperty("payment_type_label")
    private String paymentTypeLabel;

    // 支付工作流
    @JsonProperty("payment_workflow")
    private String paymentWorkflow;

    // 订单价格
    @JsonProperty("price")
    private double price;

    // 促销信息
    private Promotions promotions;

    // 报价ID
    @JsonProperty("quote_id")
    private String quoteId;

    // 运输承运人代码
    @JsonProperty("shipping_carrier_code")
    private String shippingCarrierCode;

    // 运输承运人标准代码
    @JsonProperty("shipping_carrier_standard_code")
    private String shippingCarrierStandardCode;

    // 运输公司
    @JsonProperty("shipping_company")
    private String shippingCompany;

    // 运输截止日期
    @JsonProperty("shipping_deadline")
    private String shippingDeadline;

    // 运输费用
    @JsonProperty("shipping_price")
    private double shippingPrice;

    // 运输PUDO ID
    @JsonProperty("shipping_pudo_id")
    private String shippingPudoId;

    // 运输追踪号
    @JsonProperty("shipping_tracking")
    private String shippingTracking;

    // 运输追踪URL
    @JsonProperty("shipping_tracking_url")
    private String shippingTrackingUrl;

    // 运输类型代码
    @JsonProperty("shipping_type_code")
    private String shippingTypeCode;

    // 运输类型标签
    @JsonProperty("shipping_type_label")
    private String shippingTypeLabel;

    // 运输类型标准代码
    @JsonProperty("shipping_type_standard_code")
    private String shippingTypeStandardCode;

    // 运输区域代码
    @JsonProperty("shipping_zone_code")
    private String shippingZoneCode;

    // 运输区域标签
    @JsonProperty("shipping_zone_label")
    private String shippingZoneLabel;

    // 总佣金
    @JsonProperty("total_commission")
    private double totalCommission;

    // 总价格
    @JsonProperty("total_price")
    private double totalPrice;

    // 交易日期
    @JsonProperty("transaction_date")
    private String transactionDate;

    // 交易编号
    @JsonProperty("transaction_number")
    private String transactionNumber;

    // 渠道类
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Channel {
        // 渠道代码
        @JsonProperty("code")
        private String code;

        // 渠道标签
        @JsonProperty("label")
        private String label;
    }


    // 交货日期类
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DeliveryDate {
        // 最早交货日期
        @JsonProperty("earliest")
        private String earliest;

        // 最迟交货日期
        @JsonProperty("latest")
        private String latest;
    }

    // 履约类
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Fulfillment {
        private Center center;

        // 履约中心类
        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Center {
            private String code;
        }
    }

    // 订单行类
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderLine {
        private String orderLineId;
        private String productSku;
        private String productTitle;
        private double price;
        private int quantity;
    }

    // 促销类
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Promotions {
        private String code;
        private String description;
    }
}

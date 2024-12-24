package com.somle.home24.model.req;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Home24OrderReq {

    // 订单ID（多个订单可以用逗号分隔）
    private String orderIds;

    // 顾客的订单参考号（多个参考号可以用逗号分隔）
    private String orderReferencesForCustomer;

    // 卖家的订单参考号（多个参考号可以用逗号分隔）
    private String orderReferencesForSeller;

    // 订单状态代码（多个状态代码可以用逗号分隔）
    private String orderStateCodes;

    // 渠道代码（最多20个渠道代码）
    private String channelCodes;

    // 是否只查询空的渠道（如果为true，表示只查询空的渠道）
    private Boolean onlyNullChannel;

    // 查询的起始日期（ISO 8601格式）
    private String startDate;

    // 查询的结束日期（ISO 8601格式）
    private String endDate;

    // 查询的开始更新时间（ISO 8601格式）
    private String startUpdateDate;

    // 查询的结束更新时间（ISO 8601格式）
    private String endUpdateDate;

    // 顾客扣款日期（ISO 8601格式）
    private String customerDebited;

    // 支付工作流（例如付款处理的工作流标识）
    private String paymentWorkflow;

    // 是否发生了事件（例如订单相关的事件）
    private String hasIncident;

    // 履约中心代码（对应的履约中心标识）
    private String fulfillmentCenterCode;

    // 订单的税务模式（例如：标准税务、简化税务等）
    private String orderTaxMode;

    // 商店ID（对应的商店标识）
    private String shopId;

    // 最大数量（查询结果的最大数量）
    private Integer max ;

    // 偏移量（用于分页查询，指定从哪个位置开始查询）
    private Integer offset;
}

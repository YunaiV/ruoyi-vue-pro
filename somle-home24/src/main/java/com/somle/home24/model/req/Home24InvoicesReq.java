package com.somle.home24.model.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Home24InvoicesReq {

    // 货币类型（如USD, EUR等）
    @JsonProperty("currency")
    private String currency;

    // 查询的结束日期（ISO 8601格式）
    @JsonProperty("end_date")
    private String endDate;

    // 发起的用户类型（支持多个用户类型）
    @JsonProperty("issuing_user_types")
    private String issuingUserTypes;

    // 支付状态（例如：已支付、待支付等）
    @JsonProperty("payment_status")
    private String paymentStatus;

    // 查询的开始日期（ISO 8601格式）
    @JsonProperty("start_date")
    private String startDate;

    // 查询的类型（例如：订单类型，支付类型等）
    @JsonProperty("type")
    private String type;

    // 商店ID（商店的唯一标识符）
    @JsonProperty("shop_id")
    private String shopId;

    // 最大数量（查询结果的最大数量，默认20）
    @JsonProperty("max")
    private Integer max;

    // 偏移量（分页查询的起始位置）
    @JsonProperty("offset")
    private Integer offset;
}

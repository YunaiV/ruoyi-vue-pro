package com.somle.eccang.model.req;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EccangRmaRefaReqVO {

    /**
     * rma状态
     */
    private String rmaStatus;

    /**
     * 平台，例如：aliexpress
     */
    private String platform;

    /**
     * 原始订单状态，见表2
     */
    private String orderStatus;

    /**
     * 买家ID
     */
    private String buyerId;

    /**
     * 店铺账号
     */
    private List<String> userAccount;

    /**
     * 原订单号
     */
    private String backOrderCode;

    /**
     * SKU
     */
    private String rmaSku;

    /**
     * 重发单号
     */
    private String orderCodeList;

    /**
     * rma创建时间开始 (YYYY-mm-dd HH:mm:ss)
     */
    private LocalDateTime createDateStart;

    /**
     * rma创建时间结束 (YYYY-mm-dd HH:mm:ss)
     */
    private LocalDateTime createDateEnd;

    /**
     * rma审核时间开始 (YYYY-mm-dd HH:mm:ss)
     */
    private LocalDateTime verifyDateStart;

    /**
     * rma审核时间结束 (YYYY-mm-dd HH:mm:ss)
     */
    private LocalDateTime verifyDateEnd;

    /**
     * 原订单创建时间开始 (YYYY-mm-dd HH:mm:ss)
     */
    private LocalDateTime dateCreatePlatformStart;

    /**
     * 原订单创建时间结束 (YYYY-mm-dd HH:mm:ss)
     */
    private LocalDateTime dateCreatePlatformEnd;

    /**
     * 订单付款时间开始 (YYYY-mm-dd HH:mm:ss)
     */
    private LocalDateTime datePaidPlatformStart;

    /**
     * 订单付款时间结束 (YYYY-mm-dd HH:mm:ss)
     */
    private LocalDateTime datePaidPlatformEnd;

    /**
     * 重发单发货时间开始 (YYYY-mm-dd HH:mm:ss)
     */
    private LocalDateTime dateWarehouseShippingStart;

    /**
     * 重发单发货时间结束 (YYYY-mm-dd HH:mm:ss)
     */
    private LocalDateTime dateWarehouseShippingEnd;

    /**
     * 重发单审核时间开始 (YYYY-mm-dd HH:mm:ss)
     */
    private LocalDateTime resendDateReleaseStart;

    /**
     * 重发单审核时间结束 (YYYY-mm-dd HH:mm:ss)
     */
    private LocalDateTime resendDateReleaseEnd;

    /**
     * 页码，默认值为1
     */
    private Integer page;

    /**
     * 每页数量，默认值为50，最大值为100
     */
    private Integer pageSize;

}
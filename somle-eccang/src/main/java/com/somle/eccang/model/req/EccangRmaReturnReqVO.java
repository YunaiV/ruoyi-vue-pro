package com.somle.eccang.model.req;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EccangRmaReturnReqVO {
    /**
     * 平台
     */
    private String platform;

    /**
     * 创建方式：1、仓配推送 0、客服创建
     */
    private String roCreateType;

    /**
     * ReturnLabel：1、有 0、无
     */
    private String isReturnLabel;

    /**
     * 状态：0、已作废 1、待确认 2、待处理 3、到货异常 4、处理完成
     */
    private String status;

    /**
     * 退件类型：1、物流退回 4、买家退件 5、截单退件
     */
    private String roType;

    /**
     * 仓库配送方式
     */
    private String shippingMethod;

    /**
     * 订单搜索类型：0、订单号 6、仓库单号 1、参考编号 2、跟踪号 10、退件单号 11、退件跟踪号
     */
    private String type;

    /**
     * 订单搜索值，对应type字段
     */
    private String code;

    /**
     * 发货仓库id
     */
    private String shipWarehouse;

    /**
     * 店铺账号
     */
    private String userAccount;

    /**
     * 买家搜索类型：buyer_id、买家ID buyer_name:买家姓名 buyer_desc、买家留言 buyer_mail:email
     */
    private String buyerType;

    /**
     * 买家搜索值，对应buyer_type字段
     */
    private String buyerId;

    /**
     * 创建时间起始时间（格式：YYYY-MM-dd HH:mm:ss）
     */
    private String createDateStart;

    /**
     * 创建时间结束时间（格式：YYYY-MM-dd HH:mm:ss）
     */
    private String createDateEnd;

    /**
     * 完成时间起始时间（格式：YYYY-MM-dd HH:mm:ss）
     */
    private String completeDateStart;

    /**
     * 完成时间结束时间（格式：YYYY-MM-dd HH:mm:ss）
     */
    private String completeDateEnd;

    /**
     * 审核时间起始时间（格式：YYYY-MM-dd HH:mm:ss）
     */
    private String verifyDateStart;

    /**
     * 审核时间结束时间（格式：YYYY-MM-dd HH:mm:ss）
     */
    private String verifyDateEnd;

    /**
     * SKU搜索类型：1、模糊搜索 2、精确搜索
     */
    private String skuType;

    /**
     * SKU搜索值，对应sku_type字段
     */
    private String skuStr;

    /**
     * 页码
     */
    private int page;

    /**
     * 每页数量（最大100）
     */
    private int pageSize;
}

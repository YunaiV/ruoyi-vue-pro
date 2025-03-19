package cn.iocoder.yudao.module.pay.api.order.dto;

import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支付单信息 Response DTO
 *
 * @author 芋道源码
 */
@Data
public class PayOrderRespDTO {

    /**
     * 订单编号，数据库自增
     */
    private Long id;
    /**
     * 渠道编码
     *
     * 枚举 PayChannelEnum
     */
    private String channelCode;

    // ========== 商户相关字段 ==========
    /**
     * 商户订单编号
     * 例如说，内部系统 A 的订单号。需要保证每个 PayMerchantDO 唯一
     */
    private String merchantOrderId;

    // ========== 订单相关字段 ==========
    /**
     * 支付金额，单位：分
     */
    private Integer price;
    /**
     * 支付状态
     *
     * 枚举 {@link PayOrderStatusEnum}
     */
    private Integer status;

    /**
     * 订单支付成功时间
     */
    private LocalDateTime successTime;

    // ========== 渠道相关字段 ==========

}

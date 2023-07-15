package cn.iocoder.yudao.framework.pay.core.client.dto.order;

import cn.iocoder.yudao.framework.pay.core.enums.order.PayOrderStatusRespEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 渠道支付订单 Response DTO
 *
 * @author 芋道源码
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayOrderRespDTO {

    /**
     * 支付状态
     *
     * 枚举：{@link PayOrderStatusRespEnum}
     */
    private Integer status;

    /**
     * 外部订单号
     *
     * 对应 PayOrderExtensionDO 的 no 字段
     */
    private String outTradeNo;

    /**
     * 支付渠道编号
     */
    private String channelOrderNo;
    /**
     * 支付渠道用户编号
     */
    private String channelUserId;

    /**
     * 支付成功时间
     */
    private LocalDateTime successTime;

    /**
     * 原始的异步通知结果
     */
    private Object rawData;

}

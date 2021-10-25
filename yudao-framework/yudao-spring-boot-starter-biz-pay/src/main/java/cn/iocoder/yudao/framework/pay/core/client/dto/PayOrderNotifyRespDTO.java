package cn.iocoder.yudao.framework.pay.core.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 支付通知 Response DTO
 *
 * @author 芋道源码
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayOrderNotifyRespDTO {

    /**
     * 支付订单号（支付模块的）
     */
    private String orderExtensionNo;
    /**
     * 支付渠道
     */
    private String channelOrderNo;
    /**
     * 支付渠道
     */
    private Date successTime;

    /**
     * 通知的原始数据
     *
     * 主要用于持久化，方便后续修复数据，或者排错
     */
    private String data;

}

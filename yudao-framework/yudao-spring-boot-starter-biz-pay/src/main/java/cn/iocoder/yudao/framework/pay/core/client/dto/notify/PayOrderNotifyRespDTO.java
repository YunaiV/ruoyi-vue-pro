package cn.iocoder.yudao.framework.pay.core.client.dto.notify;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 支付通知 Response DTO
 *
 * @author 芋道源码
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayOrderNotifyRespDTO {

    /**
     * 支付订单号（支付模块的）
     */
    private String orderExtensionNo;
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

}

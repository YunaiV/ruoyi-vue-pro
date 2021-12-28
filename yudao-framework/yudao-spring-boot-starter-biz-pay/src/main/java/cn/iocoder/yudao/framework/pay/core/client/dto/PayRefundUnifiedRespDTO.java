package cn.iocoder.yudao.framework.pay.core.client.dto;

import cn.iocoder.yudao.framework.pay.core.enums.PayChannelRefundRespEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
/**
 * 统一退款 Response DTO
 *
 * @author jason
 */
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PayRefundUnifiedRespDTO {
    /**
     * 渠道的退款结果
     */
    private PayChannelRefundRespEnum channelResp;

    /**
     * 渠道返回码
     */
    private String channelCode;

    /**
     * 渠道返回信息
     */
    private String channelMsg;

    //TODO 退款资金渠 ？？？
}

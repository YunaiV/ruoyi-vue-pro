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

    // TODO @jason：可以合并下。退款处理中、成功，都是成功；其它就业务失败。这样，可以复用 PayCommonResult；这个 RespDTO 可以返回渠道的退款编号
    /**
     * 渠道的退款结果
     */
    private PayChannelRefundRespEnum channelResp;

    // TODO @json：channelReturnCode 和 channelReturnMsg 放到 PayCommonResult 里噶
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

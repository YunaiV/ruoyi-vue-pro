package cn.iocoder.yudao.module.trade.service.message.bo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 订单发货时通知创建 Req BO
 *
 * @author HUIHUI
 */
@Data
public class TradeOrderMessageWhenDeliveryOrderReqBO {

    /**
     * 订单编号
     */
    @NotNull(message = "订单编号不能为空")
    private Long orderId;
    /**
     * 用户编号
     */
    @NotNull(message = "用户编号不能为空")
    private Long userId;
    /**
     * 消息
     */
    @NotEmpty(message = "发送消息不能为空")
    private String message;

}

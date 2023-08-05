package cn.iocoder.yudao.module.trade.service.message.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

// TODO @puhui999：改成 ReqBO 哈；包名也换了；service 我们还是同一用 bo 对象
/**
 * 订单发货时 Req DTO
 *
 * @author HUIHUI
 */
@Data
public class TradeOrderMessageWhenDeliveryOrderReqDTO {

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

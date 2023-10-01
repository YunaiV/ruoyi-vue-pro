package cn.iocoder.yudao.module.trade.service.order.bo;

import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单支付后 Request BO
 *
 * @author HUIHUI
 */
@Data
public class TradeAfterPayOrderReqBO {

    /**
     * 订单编号
     */
    @Schema(description = "订单编号", example = "6")
    private Long orderId;

    /**
     * 订单类型
     *
     * 枚举 {@link TradeOrderTypeEnum}
     */
    @Schema(description = "订单类型", example = "3")
    private Integer orderType;

    /**
     * 用户编号
     */
    @Schema(description = "用户编号", example = "11")
    private Long userId;

    /**
     * 订单支付时间
     */
    @Schema(description = "订单支付时间", example = "2023-08-15 10:00:00")
    private LocalDateTime payTime;

}

package cn.iocoder.yudao.module.trade.service.order.bo;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * 订单日志的创建 Request BO
 *
 * @author 陈賝
 * @since 2023/7/6 15:27
 */
@Data
public class TradeOrderLogCreateReqBO {

    /**
     * 用户编号
     */
    @NotNull(message = "用户编号不能为空")
    private Long userId;
    /**
     * 用户类型
     */
    @NotNull(message = "用户类型不能为空")
    private Integer userType;

    /**
     * 订单编号
     */
    @NotNull(message = "订单编号不能为空")
    private Long orderId;
    /**
     * 操作前状态
     */
    private Integer beforeStatus;
    /**
     * 操作后状态
     */
    @NotNull(message = "操作后的状态不能为空")
    private Integer afterStatus;

    /**
     * 操作类型
     */
    @NotNull(message = "操作类型不能为空")
    private Integer operateType;
    /**
     * 操作明细
     */
    @NotEmpty(message = "操作明细不能为空")
    private String content;

}

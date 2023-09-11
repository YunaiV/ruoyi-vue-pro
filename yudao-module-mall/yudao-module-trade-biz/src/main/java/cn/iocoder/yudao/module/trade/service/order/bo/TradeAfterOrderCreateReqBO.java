package cn.iocoder.yudao.module.trade.service.order.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 订单创建之后 Request BO
 *
 * @author HUIHUI
 */
@Data
public class TradeAfterOrderCreateReqBO {

    // ========== 拼团活动相关字段 ==========

    @Schema(description = "拼团活动编号", example = "1024")
    private Long combinationActivityId;

    @Schema(description = "拼团团长编号", example = "2048")
    private Long combinationHeadId;

    @NotNull(message = "SPU 编号不能为空")
    private Long spuId;

    @NotNull(message = "SKU 编号活动商品不能为空")
    private Long skuId;

    @NotNull(message = "订单编号不能为空")
    private Long orderId;

    @NotNull(message = "用户编号不能为空")
    private Long userId;

    @NotNull(message = "支付金额不能为空")
    private Integer payPrice;

}

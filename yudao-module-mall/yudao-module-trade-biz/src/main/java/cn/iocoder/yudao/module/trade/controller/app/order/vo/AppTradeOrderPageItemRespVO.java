package cn.iocoder.yudao.module.trade.controller.app.order.vo;

import cn.iocoder.yudao.module.trade.controller.app.order.vo.item.AppTradeOrderItemRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Schema(description = "用户 App - 订单交易的分页项 Response VO")
@Data
public class AppTradeOrderPageItemRespVO {

    @Schema(description = "订单编号", required = true, example = "1024")
    private Long id;

    @Schema(description = "订单流水号", required = true, example = "1146347329394184195")
    private String no;

    @Schema(description = "订单类型", required = true, example = "0")
    private Integer type;

    @Schema(description = "订单状态", required = true, example = "1")
    private Integer status;

    @Schema(description = "购买的商品数量", required = true, example = "10")
    private Integer productCount;

    @Schema(description = "是否评价", required = true, example = "true")
    private Boolean commentStatus;

    @Schema(description = "创建时间", required = true)
    private Date createTime;

    // ========== 价格 + 支付基本信息 ==========

    @Schema(description = "应付金额，单位：分", required = true, example = "1000")
    private Integer payPrice;

    // ========== 收件 + 物流基本信息 ==========

    @Schema(description = "配送方式", required = true, example = "1")
    private Integer deliveryType;

    /**
     * 订单项数组
     */
    private List<AppTradeOrderItemRespVO> items;

}

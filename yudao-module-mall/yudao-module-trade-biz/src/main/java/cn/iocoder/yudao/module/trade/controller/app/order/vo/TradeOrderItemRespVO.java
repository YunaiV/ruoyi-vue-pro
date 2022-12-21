package cn.iocoder.yudao.module.trade.controller.app.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "交易订单项 Response VO")
@Data
public class TradeOrderItemRespVO {

    @Schema(description = "id自增长", required = true)
    private Integer id;
    @Schema(description = "订单编号", required = true)
    private Integer orderId;
    @Schema(description = "订单项状态", required = true)
    private Integer status;
    @Schema(description = "商品 SKU 编号", required = true)
    private Integer skuId;
    @Schema(description = "商品 SPU 编号", required = true)
    private Integer spuId;
    @Schema(description = "商品名字", required = true)
    private String skuName;
    @Schema(description = "图片名字", required = true)
    private String skuImage;
    @Schema(description = "商品数量", required = true)
    private Integer quantity;
    @Schema(description = "原始单价，单位：分", required = true)
    private Integer originPrice;
    @Schema(description = "购买单价，单位：分", required = true)
    private Integer buyPrice;
    @Schema(description = "最终价格，单位：分", required = true)
    private Integer presentPrice;
    @Schema(description = "购买总金额，单位：分", required = true)
    private Integer buyTotal;
    @Schema(description = "优惠总金额，单位：分", required = true)
    private Integer discountTotal;
    @Schema(description = "最终总金额，单位：分", required = true)
    private Integer presentTotal;
    @Schema(description = "退款总金额，单位：分", required = true)
    private Integer refundTotal;
    @Schema(description = "物流id")
    private Integer logisticsId;
    @Schema(description = "售后状态", required = true)
    private Integer afterSaleStatus;
    @Schema(description = "售后订单编号")
    private Integer afterSaleOrderId;
    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;


}

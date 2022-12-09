package cn.iocoder.yudao.module.trade.controller.app.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(title = "交易订单项 Response VO")
@Data
public class TradeOrderItemRespVO {

    @Schema(title = "id自增长", required = true)
    private Integer id;
    @Schema(title = "订单编号", required = true)
    private Integer orderId;
    @Schema(title = "订单项状态", required = true)
    private Integer status;
    @Schema(title = "商品 SKU 编号", required = true)
    private Integer skuId;
    @Schema(title = "商品 SPU 编号", required = true)
    private Integer spuId;
    @Schema(title = "商品名字", required = true)
    private String skuName;
    @Schema(title = "图片名字", required = true)
    private String skuImage;
    @Schema(title = "商品数量", required = true)
    private Integer quantity;
    @Schema(title = "原始单价，单位：分", required = true)
    private Integer originPrice;
    @Schema(title = "购买单价，单位：分", required = true)
    private Integer buyPrice;
    @Schema(title = "最终价格，单位：分", required = true)
    private Integer presentPrice;
    @Schema(title = "购买总金额，单位：分", required = true)
    private Integer buyTotal;
    @Schema(title = "优惠总金额，单位：分", required = true)
    private Integer discountTotal;
    @Schema(title = "最终总金额，单位：分", required = true)
    private Integer presentTotal;
    @Schema(title = "退款总金额，单位：分", required = true)
    private Integer refundTotal;
    @Schema(title = "物流id")
    private Integer logisticsId;
    @Schema(title = "售后状态", required = true)
    private Integer afterSaleStatus;
    @Schema(title = "售后订单编号")
    private Integer afterSaleOrderId;
    @Schema(title = "创建时间", required = true)
    private LocalDateTime createTime;


}

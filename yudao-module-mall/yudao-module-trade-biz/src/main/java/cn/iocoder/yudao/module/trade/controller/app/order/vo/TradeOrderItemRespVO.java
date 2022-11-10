package cn.iocoder.yudao.module.trade.controller.app.order.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@ApiModel("交易订单项 Response VO")
@Data
public class TradeOrderItemRespVO {

    @ApiModelProperty(value = "id自增长", required = true)
    private Integer id;
    @ApiModelProperty(value = "订单编号", required = true)
    private Integer orderId;
    @ApiModelProperty(value = "订单项状态", required = true)
    private Integer status;
    @ApiModelProperty(value = "商品 SKU 编号", required = true)
    private Integer skuId;
    @ApiModelProperty(value = "商品 SPU 编号", required = true)
    private Integer spuId;
    @ApiModelProperty(value = "商品名字", required = true)
    private String skuName;
    @ApiModelProperty(value = "图片名字", required = true)
    private String skuImage;
    @ApiModelProperty(value = "商品数量", required = true)
    private Integer quantity;
    @ApiModelProperty(value = "原始单价，单位：分", required = true)
    private Integer originPrice;
    @ApiModelProperty(value = "购买单价，单位：分", required = true)
    private Integer buyPrice;
    @ApiModelProperty(value = "最终价格，单位：分", required = true)
    private Integer presentPrice;
    @ApiModelProperty(value = "购买总金额，单位：分", required = true)
    private Integer buyTotal;
    @ApiModelProperty(value = "优惠总金额，单位：分", required = true)
    private Integer discountTotal;
    @ApiModelProperty(value = "最终总金额，单位：分", required = true)
    private Integer presentTotal;
    @ApiModelProperty(value = "退款总金额，单位：分", required = true)
    private Integer refundTotal;
    @ApiModelProperty(value = "物流id")
    private Integer logisticsId;
    @ApiModelProperty(value = "售后状态", required = true)
    private Integer afterSaleStatus;
    @ApiModelProperty(value = "售后订单编号")
    private Integer afterSaleOrderId;
    @ApiModelProperty(value = "创建时间", required = true)
    private LocalDateTime createTime;


}

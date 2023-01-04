package cn.iocoder.yudao.module.trade.controller.admin.order.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 交易订单项 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class TradeOrderItemBaseVO {

    // ========== 订单项基本信息 ==========

    @ApiModelProperty(value = "编号", required = true, example = "1")
    private Long id;

    @ApiModelProperty(value = "用户编号", required = true, example = "1")
    private Long userId;

    @ApiModelProperty(value = "订单编号", required = true, example = "1")
    private Long orderId;

    // ========== 商品基本信息 ==========

    @ApiModelProperty(value = "商品 SPU 编号", required = true, example = "1")
    private Long spuId;

    @ApiModelProperty(value = "商品 SPU 名称", required = true, example = "芋道源码")
    private String spuName;

    @ApiModelProperty(value = "商品 SKU 编号", required = true, example = "1")
    private Long skuId;

    @ApiModelProperty(value = "商品图片", required = true, example = "https://www.iocoder.cn/1.png")
    private String picUrl;

    @ApiModelProperty(value = "购买数量", required = true, example = "1")
    private Integer count;

    // ========== 价格 + 支付基本信息 ==========

    @ApiModelProperty(value = "商品原价（总）", required = true, example = "100", notes = "单位：分")
    private Integer originalPrice;

    @ApiModelProperty(value = "商品原价（单）", required = true, example = "100", notes = "单位：分")
    private Integer originalUnitPrice;

    @ApiModelProperty(value = "商品优惠（总）", required = true, example = "100", notes = "单位：分")
    private Integer discountPrice;

    @ApiModelProperty(value = "商品实付金额（总）", required = true, example = "100", notes = "单位：分")
    private Integer payPrice;

    @ApiModelProperty(value = "子订单分摊金额（总）", required = true, example = "100", notes = "单位：分")
    private Integer orderPartPrice;

    @ApiModelProperty(value = "分摊后子订单实付金额（总）", required = true, example = "100", notes = "单位：分")
    private Integer orderDividePrice;

    // ========== 营销基本信息 ==========

    // TODO 芋艿：在捉摸一下

    // ========== 售后基本信息 ==========

    @ApiModelProperty(value = "售后状态", required = true, example = "1", notes = "参见 TradeOrderItemAfterSaleStatusEnum 枚举类")
    private Integer afterSaleStatus;

}

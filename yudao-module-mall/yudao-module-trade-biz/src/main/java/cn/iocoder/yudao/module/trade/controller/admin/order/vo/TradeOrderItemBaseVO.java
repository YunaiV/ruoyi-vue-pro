package cn.iocoder.yudao.module.trade.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 交易订单项 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class TradeOrderItemBaseVO {

    // ========== 订单项基本信息 ==========

    @Schema(description = "编号", required = true, example = "1")
    private Long id;

    @Schema(description = "用户编号", required = true, example = "1")
    private Long userId;

    @Schema(description = "订单编号", required = true, example = "1")
    private Long orderId;

    // ========== 商品基本信息 ==========

    @Schema(description = "商品 SPU 编号", required = true, example = "1")
    private Long spuId;

    @Schema(description = "商品 SPU 名称", required = true, example = "芋道源码")
    private String spuName;

    @Schema(description = "商品 SKU 编号", required = true, example = "1")
    private Long skuId;

    @Schema(description = "商品图片", required = true, example = "https://www.iocoder.cn/1.png")
    private String picUrl;

    @Schema(description = "购买数量", required = true, example = "1")
    private Integer count;

    // ========== 价格 + 支付基本信息 ==========

    @Schema(description = "商品原价（总）", required = true, example = "100")
    private Integer originalPrice;

    @Schema(description = "商品原价（单）", required = true, example = "100")
    private Integer originalUnitPrice;

    @Schema(description = "商品优惠（总）", required = true, example = "100")
    private Integer discountPrice;

    @Schema(description = "商品实付金额（总）", required = true, example = "100")
    private Integer payPrice;

    @Schema(description = "子订单分摊金额（总）", required = true, example = "100")
    private Integer orderPartPrice;

    @Schema(description = "分摊后子订单实付金额（总）", required = true, example = "100")
    private Integer orderDividePrice;

    // ========== 营销基本信息 ==========

    // TODO 芋艿：在捉摸一下

    // ========== 售后基本信息 ==========

    @Schema(description = "售后状态", required = true, example = "1")
    private Integer afterSaleStatus;

}

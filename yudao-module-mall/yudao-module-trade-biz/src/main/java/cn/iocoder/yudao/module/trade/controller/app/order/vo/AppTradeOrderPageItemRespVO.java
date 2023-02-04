package cn.iocoder.yudao.module.trade.controller.app.order.vo;

import cn.iocoder.yudao.module.trade.controller.app.base.property.AppProductPropertyValueDetailRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "用户 App - 订单交易的分页项 Response VO")
@Data
public class AppTradeOrderPageItemRespVO {

    @Schema(description = "订单编号", required = true, example = "1024")
    private Long id;

    @Schema(description = "订单流水号", required = true, example = "1146347329394184195")
    private String no;

    @Schema(description = "订单状态", required = true, example = "1")
    private Integer status;

    @Schema(description = "购买的商品数量", required = true, example = "10")
    private Integer productCount;

    /**
     * 订单项数组
     */
    private List<Item> items;

    @Schema(description = "用户 App - 交易订单的明细的订单项目")
    @Data
    public static class Item {

        @Schema(description = "编号", required = true, example = "1")
        private Long id;

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

        @Schema(description = "商品原价（总）", required = true, example = "100")
        private Integer originalPrice;

        @Schema(description = "商品原价（单）", required = true, example = "100")
        private Integer originalUnitPrice;

        /**
         * 属性数组
         */
        private List<AppProductPropertyValueDetailRespVO> properties;

    }

}

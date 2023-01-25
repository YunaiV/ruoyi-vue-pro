package cn.iocoder.yudao.module.trade.controller.app.order.vo;

import cn.iocoder.yudao.module.trade.controller.app.base.property.AppProductPropertyValueDetailRespVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel("用户 App - 订单交易的分页项 Response VO")
@Data
public class AppTradeOrderPageItemRespVO {

    @ApiModelProperty(value = "订单编号", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "订单流水号", required = true, example = "1146347329394184195")
    private String no;

    @ApiModelProperty(value = "订单状态", required = true, example = "1", notes = "参见 TradeOrderStatusEnum 枚举")
    private Integer status;

    @ApiModelProperty(value = "购买的商品数量", required = true, example = "10")
    private Integer productCount;

    /**
     * 订单项数组
     */
    private List<Item> items;

    @ApiModel("用户 App - 交易订单的明细的订单项目")
    @Data
    public static class Item {

        @ApiModelProperty(value = "编号", required = true, example = "1")
        private Long id;

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

        @ApiModelProperty(value = "商品原价（总）", required = true, example = "100", notes = "单位：分")
        private Integer originalPrice;

        @ApiModelProperty(value = "商品原价（单）", required = true, example = "100", notes = "单位：分")
        private Integer originalUnitPrice;

        /**
         * 属性数组
         */
        private List<AppProductPropertyValueDetailRespVO> properties;

    }

}

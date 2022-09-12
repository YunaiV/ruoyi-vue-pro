package cn.iocoder.yudao.module.trade.controller.app.cart.vo;

import cn.iocoder.yudao.module.trade.controller.app.base.sku.AppProductSkuBaseRespVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel(value = "用户 App - 用户的购物车明细 Response VO")
@Data
public class AppTradeCartDetailRespVO {

    /**
     * 商品分组数组
     */
    private List<ItemGroup> itemGroups;

    /**
     * 费用
     */
    private Order order;

    @ApiModel(value = "商品分组", description = "多个商品，参加同一个活动，从而形成分组")
    @Data
    public static class ItemGroup {

        /**
         * 商品数组
         */
        private List<Sku> items;
        /**
         * 营销活动，订单级别
         */
        private Promotion promotion;

    }

    @ApiModel(value = "商品 SKU")
    @Data
    public static class Sku extends AppProductSkuBaseRespVO {

        /**
         * SPU 信息
         */
        private AppProductSkuBaseRespVO spu;

        // ========== 购物车相关的字段 ==========

        @ApiModelProperty(value = "商品数量", required = true, example = "1")
        private Integer count;
        @ApiModelProperty(value = "是否选中", required = true, example = "true")
        private Boolean selected;

        // ========== 价格相关的字段，对应 PriceCalculateRespDTO.OrderItem 的属性 ==========

        // TODO 芋艿：后续可以去除一些无用的字段

        @ApiModelProperty(value = "商品原价（单）", required = true, example = "100")
        private Integer originalPrice;
        @ApiModelProperty(value = "商品原价（总）", required = true, example = "200")
        private Integer totalOriginalPrice;
        @ApiModelProperty(value = "商品级优惠（总）", required = true, example = "300")
        private Integer totalPromotionPrice;
        @ApiModelProperty(value = "最终购买金额（总）", required = true, example = "400")
        private Integer totalPresentPrice;
        @ApiModelProperty(value = "最终购买金额（单）", required = true, example = "500")
        private Integer presentPrice;
        @ApiModelProperty(value = "应付金额（总）", required = true, example = "600")
        private Integer totalPayPrice;

        // ========== 营销相关的字段 ==========
        /**
         * 营销活动，商品级别
         */
        private Promotion promotion;

    }

    @ApiModel(value = "订单", description = "对应 PriceCalculateRespDTO.Order 类，用于费用（合计）")
    @Data
    public static class Order {

        // TODO 芋艿：后续可以去除一些无用的字段

        @ApiModelProperty(value = "商品原价（总）", required = true, example = "100")
        private Integer skuOriginalPrice;
        @ApiModelProperty(value = "商品优惠（总）", required = true, example = "200")
        private Integer skuPromotionPrice;
        @ApiModelProperty(value = "订单优惠（总）", required = true, example = "300")
        private Integer orderPromotionPrice;
        @ApiModelProperty(value = "运费金额", required = true, example = "400")
        private Integer deliveryPrice;
        @ApiModelProperty(value = "应付金额（总）", required = true, example = "500")
        private Integer payPrice;

    }

    @ApiModel(value = "营销活动", description = "对应 PriceCalculateRespDTO.Promotion 类的属性")
    @Data
    public static class Promotion {

        @ApiModelProperty(value = "营销编号", required = true, example = "1024", notes = "营销活动的编号、优惠劵的编号")
        private Long id;
        @ApiModelProperty(value = "营销名字", required = true, example = "xx 活动")
        private String name;
        @ApiModelProperty(value = "营销类型", required = true, example = "1", notes = "参见 PromotionTypeEnum 枚举类")
        private Integer type;

        // ========== 匹配情况 ==========
        @ApiModelProperty(value = "是否满足优惠条件", required = true, example = "true")
        private Boolean meet;
        @ApiModelProperty(value = "满足条件的提示", required = true, example = "圣诞价:省 150.00 元")
        private String meetTip;

    }

}

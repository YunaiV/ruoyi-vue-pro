package cn.iocoder.yudao.module.trade.controller.app.cart.vo;

import cn.iocoder.yudao.module.trade.controller.app.base.sku.AppProductSkuBaseRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "用户 App - 用户的购物车明细 Response VO")
@Data
public class AppCartDetailRespVO {

    /**
     * 商品分组数组
     */
    private List<ItemGroup> itemGroups;

    /**
     * 费用
     */
    private Order order;

    @Schema(description = "商品分组") // 多个商品，参加同一个活动，从而形成分组
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

    @Schema(description = "商品 SKU")
    @Data
    public static class Sku extends AppProductSkuBaseRespVO {

        /**
         * SPU 信息
         */
        private AppProductSkuBaseRespVO spu;

        // ========== 购物车相关的字段 ==========

        @Schema(description = "商品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Integer count;
        @Schema(description = "是否选中", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
        private Boolean selected;

        // ========== 价格相关的字段，对应 PriceCalculateRespDTO.OrderItem 的属性 ==========

        // TODO 芋艿：后续可以去除一些无用的字段

        @Schema(description = "商品原价（单）", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
        private Integer originalPrice;
        @Schema(description = "商品原价（总）", requiredMode = Schema.RequiredMode.REQUIRED, example = "200")
        private Integer totalOriginalPrice;
        @Schema(description = "商品级优惠（总）", requiredMode = Schema.RequiredMode.REQUIRED, example = "300")
        private Integer totalPromotionPrice;
        @Schema(description = "最终购买金额（总）", requiredMode = Schema.RequiredMode.REQUIRED, example = "400")
        private Integer totalPresentPrice;
        @Schema(description = "最终购买金额（单）", requiredMode = Schema.RequiredMode.REQUIRED, example = "500")
        private Integer presentPrice;
        @Schema(description = "应付金额（总）", requiredMode = Schema.RequiredMode.REQUIRED, example = "600")
        private Integer totalPayPrice;

        // ========== 营销相关的字段 ==========
        /**
         * 营销活动，商品级别
         */
        private Promotion promotion;

    }

    @Schema(description = "订单") // 对应 PriceCalculateRespDTO.Order 类，用于费用（合计）
    @Data
    public static class Order {

        // TODO 芋艿：后续可以去除一些无用的字段

        @Schema(description = "商品原价（总）", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
        private Integer skuOriginalPrice;
        @Schema(description = "商品优惠（总）", requiredMode = Schema.RequiredMode.REQUIRED, example = "200")
        private Integer skuPromotionPrice;
        @Schema(description = "订单优惠（总）", requiredMode = Schema.RequiredMode.REQUIRED, example = "300")
        private Integer orderPromotionPrice;
        @Schema(description = "运费金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "400")
        private Integer deliveryPrice;
        @Schema(description = "应付金额（总）", requiredMode = Schema.RequiredMode.REQUIRED, example = "500")
        private Integer payPrice;

    }

    @Schema(description = "营销活动") // 对应 PriceCalculateRespDTO.Promotion 类的属性
    @Data
    public static class Promotion {

        @Schema(description = "营销编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024") // 营销活动的编号、优惠劵的编号
        private Long id;
        @Schema(description = "营销名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "xx 活动")
        private String name;
        @Schema(description = "营销类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Integer type;

        // ========== 匹配情况 ==========
        @Schema(description = "是否满足优惠条件", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
        private Boolean meet;
        @Schema(description = "满足条件的提示", requiredMode = Schema.RequiredMode.REQUIRED, example = "圣诞价:省 150.00 元")
        private String meetTip;

    }

}

package cn.iocoder.yudao.module.market.api.price.dto;

import cn.iocoder.yudao.module.market.enums.common.PromotionLevelEnum;
import cn.iocoder.yudao.module.market.enums.common.PromotionTypeEnum;
import lombok.Data;

import java.util.List;

/**
 * 价格计算 Response DTO
 *
 * @author 芋道源码
 */
@Data
public class PriceCalculateRespDTO {

    /**
     * 订单
     */
    private Order order;

    /**
     * 商品 SKU 数组
     */
    private List<Item> items;

    /**
     * 营销活动数组
     *
     * 只对应 {@link #items} 商品匹配的活动
     */
    private List<Promotion> promotions;

    /**
     * 订单
     */
    @Data
    public static class Order {

        /**
         * 商品原价（总），单位：分
         *
         * 基于 {@link Item#getTotalOriginalPrice()} 求和
         */
        private Integer skuOriginalPrice;
        /**
         * 商品优惠（总），单位：分
         *
         * 基于 {@link Item#getTotalPromotionPrice()} 求和
         */
        private Integer skuPromotionPrice;
        /**
         * 订单优惠（总），单位：分
         *
         * 例如说：满减折扣；不包括优惠劵、商品优惠
         */
        private Integer orderPromotionPrice;
        /**
         * 运费金额，单位：分
         */
        private Integer deliveryPrice;
        /**
         * 应付金额（总），单位：分
         *
         * = {@link #skuOriginalPrice}
         * + {@link #deliveryPrice}
         * - {@link #skuPromotionPrice}
         * - {@link #orderPromotionPrice}
         */
        //         * - {@link #couponPrice}  // TODO 芋艿：靠营销表记录
        private Integer payPrice;

        // ========== 营销基本信息 ==========
        /**
         * 优惠劵编号
         */
        private Long couponId;
//        /**
//         * 优惠劵减免金额，单位：分
//         *
//         *   // TODO 芋艿：靠营销表记录
//         */
//        private Integer couponPrice;

    }

    /**
     * 商品 SKU
     */
    @Data
    public static class Item extends PriceCalculateReqDTO.Item {

        /**
         * 商品原价（单），单位：分
         *
         * 对应 ProductSkuDO 的 price 字段
         */
        private Integer originalPrice;
        /**
         * 商品原价（总），单位：分
         *
         * = {@link #originalPrice} * {@link #getCount()}
         */
        private Integer totalOriginalPrice;
        /**
         * 商品级优惠（总），单位：分
         *
         * 例如说“限时折扣”：商品原价的 8 折；商品原价的减 50 元
         */
        private Integer totalPromotionPrice;
        /**
         * 最终购买金额（总），单位：分。
         *
         * = {@link #totalOriginalPrice}
         * - {@link #totalPromotionPrice}
         */
        private Integer totalPresentPrice;
        /**
         * 最终购买金额（单），单位：分。
         *
         * = {@link #totalPresentPrice} / {@link #getCount()}
         */
        private Integer presentPrice;
        /**
         * 应付金额（总），单位：分
         */
        private Integer totalPayPrice;

    }

    /**
     * 营销活动
     */
    @Data
    public static class Promotion {

        /**
         * 营销编号
         *
         * 例如说：营销活动的编号、优惠劵的编号
         */
        private Long id;
        /**
         * 营销类型
         *
         * 枚举 {@link PromotionTypeEnum}
         */
        private Integer type;
        /**
         * 营销级别
         *
         * 枚举 {@link PromotionLevelEnum}
         */
        private Integer level;
        /**
         * 匹配的商品 SKU 数组
         */
        private List<Item> items;
        /**
         * 计算时的原价（总），单位：分
         */
        private Integer totalOriginalPrice;
        /**
         * 计算时的优惠（总），单位：分
         */
        private Integer totalPromotionPrice;
        /**
         * 是否满足优惠条件
         */
        private Boolean meet;
        /**
         * 满足条件的提示
         *
         * 如果 {@link #meet} = true 满足，则提示“圣诞价:省 150.00 元”
         * 如果 {@link #meet} = false 不满足，则提示“购满 85 元，可减 40 元”
         */
        private String meetTip;

        /**
         * 匹配的商品 SKU
         */
        @Data
        public static class Item {

            /**
             * 商品 SKU 编号
             */
            private Long skuId;
            /**
             * 计算时的原价（总），单位：分
             */
            private Integer totalOriginalPrice;
            /**
             * 计算时的优惠（总），单位：分
             */
            private Integer totalPromotionPrice;

        }

    }

}

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
     * 营销活动数组
     *
     * 只对应 {@link Order#items} 商品匹配的活动
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
         * 基于 {@link OrderItem#getOriginalPrice()} 求和
         */
        private Integer originalPrice;
        /**
         * 活动减免优惠（总），单位：分
         *
         * 基于 {@link OrderItem#getActivityPrice()} 求和
         */
        private Integer activityPrice;
        /**
         * 优惠劵减免金额（总），单位：分
         *
         * 基于 {@link OrderItem#getCouponPrice()} 求和
         */
        private Integer couponPrice;
        /**
         * 积分减免金额（总），单位：分
         *
         * 基于 {@link OrderItem#getPointPrice()} 求和
         */
        private Integer pointPrice;
        /**
         * 会员减免金额（总），单位：分
         *
         * 基于 {@link OrderItem#getMemberPrice()} 求和
         */
        private Integer memberPrice;
        /**
         * 运费金额，单位：分
         */
        private Integer deliveryPrice;
        /**
         * 最终购买金额（总），单位：分
         *
         * = {@link OrderItem#getPayPrice()} 求和
         * + {@link #deliveryPrice}
         */
        private Integer payPrice;
        /**
         * 商品 SKU 数组
         */
        private List<OrderItem> items;

        // ========== 营销基本信息 ==========
        /**
         * 优惠劵编号
         */
        private Long couponId;

    }

    /**
     * 订单商品 SKU
     */
    @Data
    public static class OrderItem extends PriceCalculateReqDTO.Item {

        /**
         * 购买数量
         */
        private Integer count;

        /**
         * 商品原价（总），单位：分
         *
         * = {@link #originalUnitPrice} * {@link #getCount()}
         */
        private Integer originalPrice;
        /**
         * 商品原价（单），单位：分
         *
         * 对应 ProductSkuDO 的 price 字段
         */
        private Integer originalUnitPrice;
        /**
         * 活动减免优惠（总），单位：分
         *
         * 例如说，限时折扣、满减送等营销活动
         */
        private Integer activityPrice;
        /**
         * 优惠劵减免金额（总），单位：分
         *
         * 一个优惠劵会作用到的多个 SKU 商品，按照计算时的 {@link #payPrice} 的比例分摊
         */
        private Integer couponPrice;
        /**
         * 积分减免金额（总），单位：分
         */
        private Integer pointPrice;
        /**
         * 会员减免金额（总），单位：分
         */
        private Integer memberPrice;
        /**
         * 最终购买金额（总），单位：分。
         *
         * = {@link #originalPrice}
         * - {@link #activityPrice}
         * - {@link #couponPrice}
         * - {@link #pointPrice}
         * - {@link #memberPrice}
         */
        private Integer payPrice;
        /**
         * 最终购买金额（单），单位：分。
         *
         * = {@link #payPrice} / {@link #getCount()}
         */
        private Integer payUnitPrice;

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
         * 营销名字
         */
        private String name;
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
         * 计算时的原价（总），单位：分
         */
        private Integer totalOriginalPrice;
        /**
         * 计算时的优惠（总），单位：分
         */
        private Integer totalPromotionPrice;
        /**
         * 匹配的商品 SKU 数组
         */
        private List<PromotionItem> items;

        // ========== 匹配情况 ==========

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

    }

    /**
     * 营销匹配的商品 SKU
     */
    @Data
    public static class PromotionItem {

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

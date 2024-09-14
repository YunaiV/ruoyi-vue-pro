package cn.iocoder.yudao.module.trade.service.price.bo;

import cn.iocoder.yudao.module.product.api.property.dto.ProductPropertyValueDetailRespDTO;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 价格计算 Response BO
 *
 * 整体设计，参考 taobao 的技术文档：
 * 1. <a href="https://developer.alibaba.com/docs/doc.htm?treeId=1&articleId=1029&docType=1">订单管理</a>
 * 2. <a href="https://open.taobao.com/docV3.htm?docId=108471&docType=1">常用订单金额说明</a>
 *
 * @author 芋道源码
 */
@Data
public class TradePriceCalculateRespBO {

    /**
     * 订单类型
     *
     * 枚举 {@link TradeOrderTypeEnum}
     */
    private Integer type;

    /**
     * 订单价格
     */
    private Price price;

    /**
     * 订单项数组
     */
    private List<OrderItem> items;

    /**
     * 营销活动数组
     *
     * 只对应 {@link Price#items} 商品匹配的活动
     */
    private List<Promotion> promotions;

    /**
     * 优惠劵编号
     */
    private Long couponId;

    /**
     * 会员剩余积分
     */
    private Integer totalPoint;
    /**
     * 使用的积分
     */
    private Integer usePoint;

    /**
     * 赠送的积分
     */
    private Integer givePoint;

    /**
     * 砍价活动编号
     */
    private Long bargainActivityId;


    /**
     * 是否包邮
     */
    private Boolean freeDelivery;

    /**
     * 赠送的优惠劵
     *
     * key: 优惠劵模版编号
     * value：对应的优惠券数量
     *
     * 目的：用于订单支付后赠送优惠券
     */
    private Map<Long, Integer> giveCouponTemplateCounts;

    /**
     * 订单价格
     */
    @Data
    public static class Price {

        /**
         * 商品原价（总），单位：分
         *
         * 基于 {@link OrderItem#getPrice()} * {@link OrderItem#getCount()} 求和
         *
         * 对应 taobao 的 trade.total_fee 字段
         */
        private Integer totalPrice;
        /**
         * 订单优惠（总），单位：分
         *
         * 对应 taobao 的 order.discount_fee 字段
         */
        private Integer discountPrice;
        /**
         * 运费金额，单位：分
         */
        private Integer deliveryPrice;
        /**
         * 优惠劵减免金额（总），单位：分
         *
         * 对应 taobao 的 trade.coupon_fee 字段
         */
        private Integer couponPrice;
        /**
         * 积分抵扣的金额，单位：分
         *
         * 对应 taobao 的 trade.point_fee 字段
         */
        private Integer pointPrice;
        /**
         * VIP 减免金额，单位：分
         */
        private Integer vipPrice;
        /**
         * 最终购买金额（总），单位：分
         *
         * = {@link #totalPrice}
         * - {@link #couponPrice}
         * - {@link #pointPrice}
         * - {@link #discountPrice}
         * + {@link #deliveryPrice}
         * - {@link #vipPrice}
         */
        private Integer payPrice;

    }

    /**
     * 订单商品 SKU
     */
    @Data
    public static class OrderItem {

        /**
         * SPU 编号
         */
        private Long spuId;
        /**
         * SKU 编号
         */
        private Long skuId;
        /**
         * 购买数量
         */
        private Integer count;
        /**
         * 购物车项的编号
         */
        private Long cartId;
        /**
         * 是否选中
         */
        private Boolean selected;

        /**
         * 商品原价（单），单位：分
         *
         * 对应 ProductSkuDO 的 price 字段
         * 对应 taobao 的 order.price 字段
         */
        private Integer price;
        /**
         * 优惠金额（总），单位：分
         *
         * 对应 taobao 的 order.discount_fee 字段
         */
        private Integer discountPrice;
        /**
         * 运费金额（总），单位：分
         */
        private Integer deliveryPrice;
        /**
         * 优惠劵减免金额，单位：分
         *
         * 对应 taobao 的 trade.coupon_fee 字段
         */
        private Integer couponPrice;
        /**
         * 积分抵扣的金额，单位：分
         *
         * 对应 taobao 的 trade.point_fee 字段
         */
        private Integer pointPrice;
        /**
         * 使用的积分
         */
        private Integer usePoint;
        /**
         * VIP 减免金额，单位：分
         */
        private Integer vipPrice;
        /**
         * 应付金额（总），单位：分
         *
         * = {@link #price} * {@link #count}
         * - {@link #couponPrice}
         * - {@link #pointPrice}
         * - {@link #discountPrice}
         * + {@link #deliveryPrice}
         * - {@link #vipPrice}
         */
        private Integer payPrice;

        // ========== 商品 SPU 信息 ==========
        /**
         * 商品名
         */
        private String spuName;
        /**
         * 商品图片
         *
         * 优先级：SKU.picUrl > SPU.picUrl
         */
        private String picUrl;
        /**
         * 分类编号
         */
        private Long categoryId;

        // ========== 物流相关字段 =========

        /**
         * 配送方式数组
         *
         * 对应 DeliveryTypeEnum 枚举
         */
        private List<Integer> deliveryTypes;

        /**
         * 物流配置模板编号
         *
         * 对应 TradeDeliveryExpressTemplateDO 的 id 编号
         */
        private Long deliveryTemplateId;

        // ========== 商品 SKU 信息 ==========
        /**
         * 商品重量，单位：kg 千克
         */
        private Double weight;
        /**
         * 商品体积，单位：m^3 平米
         */
        private Double volume;

        /**
         * 商品属性数组
         */
        private List<ProductPropertyValueDetailRespDTO> properties;

        /**
         * 赠送的积分
         */
        private Integer givePoint;

    }

    /**
     * 营销明细
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
         * 计算时的原价（总），单位：分
         */
        private Integer totalPrice;
        /**
         * 计算时的优惠（总），单位：分
         */
        private Integer discountPrice;
        /**
         * 匹配的商品 SKU 数组
         */
        private List<PromotionItem> items;

        // ========== 匹配情况 ==========

        /**
         * 是否满足优惠条件
         */
        private Boolean match;
        /**
         * 满足条件的提示
         *
         * 如果 {@link #match} = true 满足，则提示“圣诞价:省 150.00 元”
         * 如果 {@link #match} = false 不满足，则提示“购满 85 元，可减 40 元”
         */
        private String description;

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
        private Integer totalPrice;
        /**
         * 计算时的优惠（总），单位：分
         */
        private Integer discountPrice;

    }

}

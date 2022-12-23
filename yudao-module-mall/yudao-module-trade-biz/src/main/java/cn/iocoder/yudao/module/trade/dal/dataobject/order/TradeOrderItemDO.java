package cn.iocoder.yudao.module.trade.dal.dataobject.order;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderItemRefundStatusEnum;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 交易订单项 DO
 *
 * @author 芋道源码
 */
@TableName(value = "trade_order_item", autoResultMap = true)
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class TradeOrderItemDO extends BaseDO {

    // ========== 订单项基本信息 ==========
    /**
     * 编号
     */
    private Long id;
    /**
     * 用户编号
     *
     * 关联 MemberUserDO 的 id 编号
     */
    private Long userId;
    /**
     * 订单编号
     *
     * 关联 {@link TradeOrderDO#getId()}
     */
    private Long orderId;

    // ========== 商品基本信息 ==========
    /**
     * 商品 SPU 编号
     *
     * 关联 ProductSkuDO 的 spuId 编号
     */
    private Long spuId;
    /**
     * 商品 SKU 编号
     *
     * 关联 ProductSkuDO 的 id 编号
     */
    private Long skuId;
    /**
     * 规格值数组，JSON 格式
     */
    @TableField(typeHandler = PropertyTypeHandler.class)
    private List<Property> properties;
    /**
     * 商品名称
     */
    private String name;
    /**
     * 商品图片
     */
    private String picUrl;
    /**
     * 购买数量
     */
    private Integer count;
//    /**
//     * 是否评论 TODO
//     *
//     * false - 未评论
//     * true - 已评论
//     */
//    private Boolean commented;

    // ========== 价格 + 支付基本信息 ==========

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
     * 对应 taobao 的 order.price 字段
     */
    private Integer originalUnitPrice;
    /**
     * 商品优惠（总），单位：分
     *
     * 商品级优惠：对单个商品的，常见如：商品原价的 8 折；商品原价的减 50 元
     *
     * 对应 taobao 的 order.discount_fee 字段
     */
    private Integer discountPrice;
    /**
     * 子订单实付金额，不算主订单分摊金额，单位：分
     *
     * = {@link #originalPrice}
     * - {@link #discountPrice}
     *
     * 对应 taobao 的 order.payment 字段
     */
    private Integer payPrice;

    /**
     * 子订单分摊金额（总），单位：分
     * 需要分摊 {@link TradeOrderDO#getDiscountPrice()}、{@link TradeOrderDO#getCouponPrice()}、{@link TradeOrderDO#getPointPrice()}
     *
     * 对应 taobao 的 order.part_mjz_discount 字段
     * 淘宝说明：子订单分摊优惠基础逻辑：一般正常优惠券和满减优惠按照子订单的金额进行分摊，特殊情况如果优惠券是指定商品使用的，只会分摊到对应商品子订单上不分摊。
     */
    private Integer orderPartPrice;
    /**
     * 分摊后子订单实付金额（总），单位：分
     *
     * = {@link #payPrice}
     * - {@link #orderPartPrice}
     *
     * 对应 taobao 的 divide_order_fee 字段
     */
    private Integer orderDividePrice;

    // ========== 营销基本信息 ==========

    // ========== 退款基本信息 ==========
    /**
     * 退款状态 TODO
     *
     * 枚举 {@link TradeOrderItemRefundStatusEnum}
     */
    private Integer refundStatus; // TODO 芋艿：可以考虑去查
    // 如上字段，举个例子：
    // 假设购买三个，即 stock = 3 。
    // originPrice = 15
    // 使用限时折扣（单品优惠）8 折，buyPrice = 12
    // 开始算总的价格
    // buyTotal = buyPrice * stock = 12 * 3 = 36
    // discountTotal ，假设有满减送（分组优惠）满 20 减 10 ，并且使用优惠劵满 1.01 减 1 ，则 discountTotal = 10 + 1 = 11
    // presentTotal = buyTotal - discountTotal = 24 - 11 = 13
    // 最终 presentPrice = presentTotal / stock = 13 / 3 = 4.33
    /**
     * 退款总金额，单位：分 TODO
     */
    private Integer refundTotal;

    /**
     * 商品属性
     */
    @Data
    public static class Property {

        /**
         * 属性编号
         *
         * 关联 ProductPropertyDO 的 id 编号
         */
        private Long propertyId;
        /**
         * 属性值编号
         *
         * 关联 ProductPropertyValueDO 的 id 编号
         */
        private Long valueId;

    }

    // TODO @芋艿：可以找一些新的思路
    public static class PropertyTypeHandler extends AbstractJsonTypeHandler<List<Property>> {

        @Override
        protected List<Property> parse(String json) {
            return JsonUtils.parseArray(json, Property.class);
        }

        @Override
        protected String toJson(List<Property> obj) {
            return JsonUtils.toJsonString(obj);
        }

    }

}


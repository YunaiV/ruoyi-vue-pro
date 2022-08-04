package cn.iocoder.yudao.module.trade.dal.dataobject.order;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderItemRefundStatusEnum;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 交易订单项 DO
 *
 * @author 芋道源码
 */
@TableName(value = "trade_order_item")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class TradeOrderItemDO extends BaseDO {

    // ========== 订单项基本信息 ==========
    /**
     * 编号
     */
    private Integer id;
//    /**
//     * 店铺编号
//     *
//     * 关联 {@link ShopDO#getId()} TODO 芋艿：多店铺，暂不考虑
//     */
//    private Long shopId;
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
     * 关联 ProductSpuDO 的 id 编号
     */
    private Long spuId;
    /**
     * 商品 SKU 编号
     *
     * 关联 ProductSkuDO 的 id 编号
     */
    private Integer skuId;
    /**
     * 规格值数组，JSON 格式
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
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
    private Integer stock;
    /**
     * 是否评论
     *
     * false - 未评论
     * true - 已评论
     */
    private Boolean commented;

    // ========== 价格 + 支付基本信息 ==========
    /**
     * 原始单价，单位：分。
     */
    private Integer originPrice; // price
    /**
     * 购买单价，单位：分
     */
    private Integer buyPrice;
    /**
     * 最终单价，单位：分。
     */
    private Integer presentPrice;
    /**
     * 购买总金额，单位：分
     *
     * 用途类似 {@link #presentTotal}
     */
    private Integer buyTotal;
    /**
     * 优惠总金额，单位：分。
     */
    private Integer discountTotal;
    /**
     * 最终总金额，单位：分。
     *
     * 注意，presentPrice * stock 不一定等于 presentTotal 。
     * 因为，存在无法整除的情况。
     * 举个例子，presentPrice = 8.33 ，stock = 3 的情况，presentTotal 有可能是 24.99 ，也可能是 25 。
     * 所以，需要存储一个该字段。
     */
    private Integer presentTotal; // product_total_amount

    // ========== 退款基本信息 ==========
    /**
     * 退款状态
     *
     * 枚举 {@link TradeOrderItemRefundStatusEnum}
     */
    private Integer refundStatus;
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
     * 退款总金额，单位：分
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

    // TODO 芋艿：basket_date 加入购物车时间；
    // TODO 芋艿：distribution_card_no 推广员使用的推销卡号

    // TODO 待确定：mf
    // TODO give_integral：赠送积分
    // TODO is_reply：是否评价，0-未评价，1-已评价
    // TODO is_sub：是否单独分佣,0-否，1-是
    // TODO vip_price：会员价
    // TODO product_type：商品类型:0-普通，1-秒杀，2-砍价，3-拼团，4-视频号

    // TODO 待确定：lf
    // TODO integral_price：积分抵扣的金额
    // TODO member_price：会员价格
    // TODO is_member：是否为会员折扣;0-不是;1-是
    // TODO member_discount：会员折扣(百分比)

    // TODO goods_info 商品信息

    // TODO integral_price：积分抵扣的金额
}

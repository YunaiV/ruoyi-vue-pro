package cn.iocoder.yudao.module.trade.dal.dataobject.order;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.product.enums.delivery.DeliveryModeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderCloseTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.enums.refund.TradeRefundStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.util.Date;

/**
 * 交易订单 DO
 *
 * @author 芋道源码
 */
@TableName("trade_order")
@KeySequence("trade_order_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeOrderDO extends BaseDO {

    // ========== 订单基本信息 ==========
    /**
     * 订单编号，主键自增
     */
    private Integer id;
    /**
     * 订单流水号
     *
     * 例如说，1146347329394184195
     */
    private String sn;
    // TODO 芋艿：order_type 订单类型
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
     * 订单状态
     *
     * 枚举 {@link TradeOrderStatusEnum}
     */
    private Integer status;
    /**
     * 关闭类型
     *
     * 枚举 {@link TradeOrderCloseTypeEnum}
     */
    private Integer closeType;
    // TODO 芋艿：要不要存储 prod_name 购买的商品名门？
    /**
     * 购买的商品数量
     */
    private Integer productCount; // total_num
    /**
     * 备注
     */
    private String remark;
    /**
     * 订单结束时间
     *
     * 即交易订单状态变成 {@link TradeOrderStatusEnum#COMPLETED} 或 {@link TradeOrderStatusEnum#CLOSED} 的时间
     */
    private Date endTime;

    // ========== 价格 + 支付基本信息 ==========
    /**
     * 是否已支付
     *
     * true - 已经支付过
     * false - 没有支付过
     */
    private Boolean payed;
    /**
     * 付款时间
     */
    private Date payTime;

    // TODO 芋艿：delete_status 用户订单删除状态；0 - 未删除；1 - 回收站；2 - 永久删除

    // ========== 价格 + 支付基本信息 ==========
    /**
     * 购买（商品）总金额，单位：分
     */
    private Integer buyPrice; // total
    /**
     * 优惠总金额，单位：分。
     */
    private Integer discountPrice; // reduce_amount
    /**
     * 物流金额 (分)
     */
    private Integer logisticsPrice; // freight_amount; freight_price
    /**
     * 最终金额，单位：分
     *
     * buyPrice + logisticsPrice -  discountPrice = presentPrice
     */
    private Integer presentPrice; // actual_total
    /**
     * 实际已支付金额，单位：分
     *
     * 初始时，金额为 0 。等到支付成功后，会进行更新。
     */
    private Integer payPrice;
    /**
     * 退款金额，单位：分
     *
     * 注意，退款并不会影响 {@link #payPrice} 实际支付金额
     * 也就说，一个订单最终产生多少金额的收入 = payPrice - refundPrice
     */
    @Deprecated
    private Integer refundPrice;
    /**
     * 支付订单编号
     *
     * 对接 pay-module-biz 支付服务的支付订单编号，即 PayOrderDO 的 id 编号
     */
    private Long payOrderId;
    /**
     * 支付成功的支付渠道
     */
    private Integer payType;

    // ========== 收件 + 物流基本信息 ==========
    /**
     * 配送方式
     *
     * 枚举 {@link DeliveryModeEnum}
     */
    private Integer deliveryMode;
    /**
     * 配置模板的编号
     *
     * 关联 DeliveryTemplateDO 的 id 编号
     */
    private Long deliveryTemplateId; // dvy_id
    /**
     * 物流公司单号
     */
    private String expressNo; // dvy_flow_id
    /**
     * 发货时间
     */
    private Date deliveryTime;
    /**
     * 收货时间
     */
    private Date receiveTime;
    /**
     * 收件人名称
     */
    private String receiverName;
    /**
     * 收件人手机
     */
    private String receiverMobile;
    /**
     * 收件人地区编号
     */
    private Integer receiverAreaId;
    /**
     * 收件人邮编
     */
    private Integer receiverPostCode;
    /**
     * 收件人详细地址
     */
    private String receiverDetailAddress;

    // ========== 退款基本信息 ==========
    /**
     * 退款状态
     *
     * 枚举 {@link TradeRefundStatusEnum}
     */
    private Integer refundStatus;

    // ========== 营销基本信息 ==========
    /**
     * 优惠劵编号
     */
    private Integer couponCardId;

    // TODO 芋艿，这块还要结合营销和价格计算，在去优化下。

    // TODO ========== 待定字段：yv =========
    // TODO cart_id：购物车 id
    // TODO total_postage：邮费
    // TODO pay_postage：支付邮费
    // TODO coupon_price：优惠劵金额；
    // TODO refund_status：0 未退款；1 申请中；2 已退款
    // TODO refund_reason_wap_img：退款图片
    // TODO refund_reason_wap_explain：退款用户说明
    // TODO refund_reason_time：退款时间
    // TODO refund_reason_wap：前台退款原因
    // TODO refund_reason：不退款的理由
    // TODO refund_price：退款金额

    // TODO gain_integral：消费赚取积分
    // TODO use_integral：使用积分
    // TODO pay_integral：实际支付积分
    // TODO back_integral：给用户退了多少积分

    // TODO combination_id：拼团产品id
    // TODO pink_id：拼团id
    // TODO seckill_id：秒杀产品ID
    // TODO bargain_id：砍价id

    // TODO cost：成本价
    // TODO verify_code：核销码
    // TODO store_id：门店id

}

package cn.iocoder.yudao.module.trade.dal.dataobject.order;

import cn.iocoder.yudao.framework.common.enums.TerminalEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.product.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderCloseTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderRefundStatusEnum;
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
    /**
     * 订单类型
     *
     * 枚举 {@link TradeOrderTypeEnum}
     */
    private Integer type;
    /**
     * 订单来源终端
     *
     * 枚举 {@link TerminalEnum}
     */
    private Integer terminal;
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
     * 确认收获时间
     */
    private Date confirmTakeTime;
    /**
     * 订单取消时间
     */
    private Date cancelTime;

    // ========== 价格 + 支付基本信息 ==========
    /**
     * 是否已支付
     *
     * true - 已经支付过
     * false - 没有支付过
     */
    private Boolean payed; // TODO payStatus 0 - 待付款；1 - 已付款；2 - 已退款
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
     * 枚举 {@link DeliveryTypeEnum}
     */
    private Integer deliveryType;
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
     * 枚举 {@link TradeOrderRefundStatusEnum}
     */
    private Integer refundStatus;
    /**
     * 退款金额，单位：分
     *
     * 注意，退款并不会影响 {@link #payPrice} 实际支付金额
     * 也就说，一个订单最终产生多少金额的收入 = payPrice - refundPrice
     */
    private Integer refundPrice;

    // ========== 营销基本信息 ==========
    /**
     * 优惠劵编号
     */
    private Integer couponId;

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

    // TODO ========== 待定字段：cf =========
    // TODO before_pay_price：改价前支付金额
    // TODO is_alter_price：是否改价
    // TODO out_trade_no：商户系统内部的订单号 String

    // TODO ========== 待定字段：lf =========
    // TODO integral_amount：积分抵扣金额
    // TODO shipping_status：发货状态
    // TODO shipping_time：最后新发货时间

    // TODO ========== 待定字段：lf =========
    // TODO settle_id：未结算
    // TODO settle_amount：结算金额
    // TODO use_integral：使用的积分
    // TODO team_found_id: 拼团id
    // TODO team_id: 拼团活动id
    // TODO delivery_id: 发货单ID
    // TODO attach_values: 附带的值(赠送时机，赠送积分成长值什么的)Json格式

}

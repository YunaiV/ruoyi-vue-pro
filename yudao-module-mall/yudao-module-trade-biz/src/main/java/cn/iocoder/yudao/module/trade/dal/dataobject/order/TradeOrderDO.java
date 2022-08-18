package cn.iocoder.yudao.module.trade.dal.dataobject.order;

import cn.iocoder.yudao.framework.common.enums.TerminalEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.product.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderCancelTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderRefundStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
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
    private Long id;
    /**
     * 订单流水号
     *
     * 例如说，1146347329394184195
     */
    private String sn;
    /**
     * 订单类型
     *
     * 枚举 {@link TradeOrderTypeEnum}
     */
    private Integer type; // TODO order_promotion_type
    /**
     * 订单来源终端
     *
     * 枚举 {@link TerminalEnum}
     */
    private Integer terminal;
    /**
     * 用户编号
     *
     * 关联 MemberUserDO 的 id 编号
     */
    private Long userId;
    /**
     * 用户 IP
     */
    private String userIp;
    /**
     * 用户备注
     */
    private String userRemark;
    /**
     * 订单状态
     *
     * 枚举 {@link TradeOrderStatusEnum}
     */
    private Integer status;
    // TODO 芋艿：要不要存储 prod_name 购买的商品名门？
    /**
     * 购买的商品数量
     */
    private Integer productCount; // total_num
    /**
     * 订单完成时间
     */
    private Date finishTime;
    /**
     * 订单取消时间
     */
    private Date cancelTime;
    /**
     * 取消类型
     *
     * 枚举 {@link TradeOrderCancelTypeEnum}
     */
    private Integer cancelType;
    /**
     * 商家备注
     */
    private String remark;

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

    // ========== 价格 + 支付基本信息 ==========
    // 价格文档 - 淘宝：https://open.taobao.com/docV3.htm?docId=108471&docType=1
    // 价格文档 - 京东到家：https://openo2o.jddj.com/api/getApiDetail/182/4d1494c5e7ac4679bfdaaed950c5bc7f.htm
    // 价格文档 - 有赞：https://doc.youzanyun.com/detail/API/0/906

//  TODO  promotion_details(订单优惠信息明细，商品和订单级优惠一般都在里面)

    /**
     * 商品原价（总），单位：分
     *
     * 基于 {@link TradeOrderItemDO#getTotalOriginalPrice()} 求和
     */
    // niu - goods_money；
    private Integer skuOriginalPrice;
    /**
     * 商品优惠（总），单位：分
     *
     * 基于 {@link TradeOrderItemDO#getTotalPromotionPrice()} 求和
     */
    private Integer skuPromotionPrice;
    /**
     * 订单优惠（总），单位：分
     *
     * 例如说：满减折扣；不包括优惠劵、商品优惠
     */
    // niu - promotion_money；taobao - discount_fee（主订单优惠）
    private Integer orderPromotionPrice;
    /**
     * 运费金额，单位：分
     */
    // niu - delivery_money；taobao - post_fee（订单邮费）
    private Integer deliveryPrice;
    // TODO 芋艿：taobao 的：trade.adjust_fee/order.adjust_fee（调整金额，如：卖家手动修改订单价格，官方数据修复等等）
    /**
     * 应付金额（总），单位：分
     *
     * = {@link #skuOriginalPrice}
     * + {@link #deliveryPrice}
     * - {@link #skuPromotionPrice}
     * - {@link #orderPromotionPrice}
     */
    // niu - pay_money；taobao - payment（主订单实付金额） | trade.total_fee（主订单应付金额，参考使用）；
//     * - {@link #couponPrice}  // TODO 芋艿：靠营销表记录
    private Integer payPrice;
    /**
     * 支付订单编号
     *
     * 对接 pay-module-biz 支付服务的支付订单编号，即 PayOrderDO 的 id 编号
     */
    private Long payOrderId;
    /**
     * 支付成功的支付渠道
     *
     * 对应 PayChannelEnum 枚举
     */
    private Integer payChannel;

    // ========== 收件 + 物流基本信息 ==========
    /**
     * 配送方式
     * 会员用户下单时，选择的配送方式
     *
     * 枚举 {@link DeliveryTypeEnum}
     */
    private Integer deliveryType;
    /**
     * 实际的配送方式
     * 管理后台发货时，选择的配送方式
     *
     * 0 - 无需物流
     * 枚举 {@link DeliveryTypeEnum}
     */
    private Integer actualDeliveryType; // like - shipping_status；
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
     * 发货状态
     *
     * true - 已发货
     * false - 未发货
     */
    private Boolean deliveryStatus;
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
    private Long couponId;
//    /**
//     * 优惠劵减免金额，单位：分  // TODO 芋艿：靠营销表记录
//     */
//    // niu - coupon_money；
//    private Integer couponPrice;
//    /**
//     * 积分抵扣的金额，单位：分
//     */
//    private Integer integralPrice;
//    /**
//     * 使用的积分
//     */
//    private Integer useIntegral;

    // TODO ========== 待定字段：yv =========
    // TODO cart_id：购物车 id
    // TODO refund_reason_wap_img：退款图片
    // TODO refund_reason_wap_explain：退款用户说明
    // TODO refund_reason_time：退款时间
    // TODO refund_reason_wap：前台退款原因
    // TODO refund_reason：不退款的理由

    // TODO gain_integral：消费赚取积分
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
    // TODO settle_id：未结算
    // TODO settle_amount：结算金额
    // TODO team_found_id: 拼团id
    // TODO team_id: 拼团活动id
    // TODO delivery_id: 发货单ID
    // TODO attach_values: 附带的值(赠送时机，赠送积分成长值什么的)Json格式

    // TODO ========== 待定字段：nf =========
    // TODO delivery_code：整体提货编码

    // TODO ========== 待定字段：niu =========
    // TODO adjust_money '订单调整金额'
    // TODO balance_money ''余额支付金额''

}

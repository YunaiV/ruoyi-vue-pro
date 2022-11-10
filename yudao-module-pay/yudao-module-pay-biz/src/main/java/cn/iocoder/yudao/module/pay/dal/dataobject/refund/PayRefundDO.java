package cn.iocoder.yudao.module.pay.dal.dataobject.refund;

import cn.iocoder.yudao.module.pay.dal.dataobject.merchant.PayAppDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.merchant.PayChannelDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.merchant.PayMerchantDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.module.pay.enums.refund.PayRefundStatusEnum;
import cn.iocoder.yudao.module.pay.enums.refund.PayRefundTypeEnum;
import cn.iocoder.yudao.framework.pay.core.enums.PayChannelEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 支付退款单 DO
 * 一个支付订单，可以拥有多个支付退款单
 *
 * 即 PayOrderDO : PayRefundDO = 1 : n
 *
 * @author 芋道源码
 */
@TableName("pay_refund")
@KeySequence("pay_refund_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayRefundDO extends BaseDO {

    /**
     * 退款单编号，数据库自增
     */
    @TableId
    private Long id;

    /**
     * 商户编号
     *
     * 关联 {@link PayMerchantDO#getId()}
     */
    private Long merchantId;
    /**
     * 应用编号
     *
     * 关联 {@link PayAppDO#getId()}
     */
    private Long appId;
    /**
     * 渠道编号
     *
     * 关联 {@link PayChannelDO#getId()}
     */
    private Long channelId;
    /**
     * 商户编码
     *
     * 枚举 {@link PayChannelEnum}
     */
    private String channelCode;
    /**
     * 订单编号
     *
     * 关联 {@link PayOrderDO#getId()}
     */
    private Long orderId;

    /**
     * 交易订单号，根据规则生成
     * 调用支付渠道时，使用该字段作为对接的订单号。
     * 1. 调用微信支付 https://api.mch.weixin.qq.com/v3/refund/domestic/refunds 时，使用该字段作为 out_trade_no
     * 2. 调用支付宝 https://opendocs.alipay.com/apis 时，使用该字段作为 out_trade_no
     *  这里对应 pay_extension 里面的 no
     * 例如说，P202110132239124200055
     */
    private String tradeNo;


    // ========== 商户相关字段 ==========
    /**
     * 商户订单编号
     */
    private String merchantOrderId;

    /**
     * 商户退款订单号, 由商户系统产生， 由他们保证唯一，不能为空，通知商户时会传该字段。
     * 例如说，内部系统 A 的退款订单号。需要保证每个 PayMerchantDO 唯一
     * 个商户退款订单，对应一条退款请求记录。可多次提交。 渠道保持幂等
     * 使用商户退款单，作为退款请求号
     * https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_1_9.shtml 中的 out_refund_no
     * https://opendocs.alipay.com/apis alipay.trade.refund 中的 out_request_no
     * 退款请求号。
     * 标识一次退款请求，需要保证在交易号下唯一，如需部分退款，则此参数必传。
     * 注：针对同一次退款请求，如果调用接口失败或异常了，重试时需要保证退款请求号不能变更，
     * 防止该笔交易重复退款。支付宝会保证同样的退款请求号多次请求只会退一次。
     * 退款单请求号，根据规则生成
     * 例如说，R202109181134287570000
     */
    // TODO @jason：merchantRefundNo =》merchantRefundOId
    private String merchantRefundNo;

    /**
     * 异步通知地址
     */
    private String notifyUrl;

    /**
     * 通知商户退款结果的回调状态
     * TODO 0 未发送 1 已发送
     */
    private Integer notifyStatus;

    // ========== 退款相关字段 ==========
    /**
     * 退款状态
     *
     * 枚举 {@link PayRefundStatusEnum}
     */
    private Integer status;

    /**
     * 退款类型(部分退款，全部退款)
     *
     * 枚举 {@link PayRefundTypeEnum}
     */
    private Integer type;
    /**
     * 支付金额，单位：分
     */
    private Long payAmount;
    /**
     * 退款金额，单位：分
     */
    private Long refundAmount;

    /**
     * 退款原因
     */
    private String reason;

    /**
     * 用户 IP
     */
    private String userIp;

    // ========== 渠道相关字段 ==========
    /**
     * 渠道订单号，pay_order 中的channel_order_no 对应
     */
    private String channelOrderNo;
    /**
     * 微信中的 refund_id
     * https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_1_9.shtml
     * 支付宝没有
     * 渠道退款单号，渠道返回
     */
    private String channelRefundNo;

    /**
     * 调用渠道的错误码
     */
    private String channelErrorCode;

    /**
     * 调用渠道报错时，错误信息
     */
    private String channelErrorMsg;


    /**
     * 支付渠道的额外参数
     * 参见 https://www.pingxx.com/api/Refunds%20退款概述.html
     */
    private String channelExtras;


    /**
     * TODO
     * 退款失效时间
     */
    private LocalDateTime expireTime;
    /**
     * 退款成功时间
     */
    private LocalDateTime successTime;
    /**
     * 退款通知时间
     */
    private LocalDateTime notifyTime;




}

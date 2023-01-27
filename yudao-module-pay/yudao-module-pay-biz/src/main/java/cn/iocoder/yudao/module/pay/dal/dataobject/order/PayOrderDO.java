package cn.iocoder.yudao.module.pay.dal.dataobject.order;

import cn.iocoder.yudao.module.pay.dal.dataobject.merchant.PayAppDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.merchant.PayChannelDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.merchant.PayMerchantDO;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderNotifyStatusEnum;
import cn.iocoder.yudao.module.pay.enums.refund.PayRefundTypeEnum;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.pay.core.enums.PayChannelEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 支付订单 DO
 *
 * @author 芋道源码
 */
@TableName("pay_order")
@KeySequence("pay_order_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayOrderDO extends BaseDO {

    /**
     * 订单编号，数据库自增
     */
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
     * 渠道编码
     *
     * 枚举 {@link PayChannelEnum}
     */
    private String channelCode;

    // ========== 商户相关字段 ==========

    /**
     * 商户订单编号
     * 例如说，内部系统 A 的订单号。需要保证每个 PayMerchantDO 唯一
     */
    private String merchantOrderId;
    /**
     * 商品标题
     */
    private String subject;
    /**
     * 商品描述信息
     */
    private String body;
    /**
     * 异步通知地址
     */
    private String notifyUrl;
    /**
     * 通知商户支付结果的回调状态
     *
     * 枚举 {@link PayOrderNotifyStatusEnum}
     */
    private Integer notifyStatus;
//    /**
//     * 商户拓展参数
//     */
//    private Map<String, String> merchantExtras;

    // ========== 订单相关字段 ==========

    /**
     * 支付金额，单位：分
     */
    private Integer amount;
    /**
     * 渠道手续费，单位：百分比
     *
     * 冗余 {@link PayChannelDO#getFeeRate()}
     */
    private Double channelFeeRate;
    /**
     * 渠道手续金额，单位：分
     */
    private Long channelFeeAmount;
    /**
     * 支付状态
     *
     * 枚举 {@link PayOrderStatusEnum}
     */
    private Integer status;
    /**
     * 用户 IP
     */
    private String userIp;
    /**
     * 订单失效时间
     */
    private LocalDateTime expireTime;
    /**
     * 订单支付成功时间
     */
    private LocalDateTime successTime;
    /**
     * 订单支付通知时间，即支付渠道的通知时间
     */
    private LocalDateTime notifyTime;
    /**
     * 支付成功的订单拓展单编号
     *
     * 关联 {@link PayOrderDO#getId()}
     */
    private Long successExtensionId;

    // ========== 退款相关字段 ==========
    /**
     * 退款状态
     *
     * 枚举 {@link PayRefundTypeEnum}
     */
    private Integer refundStatus;
    /**
     * 退款次数
     */
    private Integer refundTimes;
    /**
     * 退款总金额，单位：分
     */
    private Long refundAmount;

    // ========== 渠道相关字段 ==========
    /**
     * 渠道用户编号
     *
     * 例如说，微信 openid、支付宝账号
     */
    private String channelUserId;
    /**
     * 渠道订单号
     */
    private String channelOrderNo;

}

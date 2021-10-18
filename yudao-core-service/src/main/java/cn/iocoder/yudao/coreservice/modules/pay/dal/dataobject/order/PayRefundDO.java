package cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.order;

import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayAppDO;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayChannelDO;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayMerchantDO;
import cn.iocoder.yudao.coreservice.modules.pay.enums.merchant.PayChannelCodeEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;

import java.util.Date;

/**
 * 支付退款单 DO
 * 一个支付订单，可以拥有多个支付退款单
 *
 * 即 PayOrderDO : PayRefundDO = 1 : n
 *
 * @author 芋道源码
 */
@Data
public class PayRefundDO extends BaseDO {

    /**
     * 退款单编号，数据库自增
     */
    private Long id;
    /**
     * 退款单号，根据规则生成
     *
     * 例如说，R202109181134287570000
     */
    private String no;
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
     * 枚举 {@link PayChannelCodeEnum}
     */
    private String channelCode;
    /**
     * 订单编号
     *
     * 关联 {@link PayOrderDO#getId()}
     */
    private Long orderId;

    // ========== 商户相关字段 ==========
    /**
     * 商户退款订单号
     * 例如说，内部系统 A 的退款订单号。需要保证每个 PayMerchantDO 唯一 TODO 芋艿：需要在测试下
     */
    private String merchantRefundNo;
    /**
     * 商户拓展参数
     */
    private String merchantExtra;

    // ========== 退款相关字段 ==========
    /**
     * 退款状态
     *
     * TODO 芋艿：状态枚举
     */
    private Integer status;
    /**
     * 通知商户退款结果的回调状态
     * TODO 芋艿：0 未发送 1 已发送
     */
    private Integer notifyStatus;
    /**
     * 客户端 IP
     */
    private String clientIp;
    /**
     * 退款金额，单位：分
     */
    private Long amount;
    /**
     * 退款原因
     */
    private String reason;
    /**
     * 订单退款成功时间
     */
    private Date successTime;
    /**
     * 退款失效时间
     */
    private Date expireTime;
    /**
     * 支付渠道的额外参数
     *
     * 参见 https://www.pingxx.com/api/Refunds%20退款概述.html
     */
    private String channelExtra;
    /**
     * 异步通知地址
     */
    private String notifyUrl;

    // TODO 芋艿：可能要优化
    /**
     * 渠道支付错误码
     */
    private String errorCode;
    /**
     * 渠道支付错误消息
     */
    private String errorMessage;

    // ========== 渠道相关字段 ==========
    /**
     * 渠道订单号
     */
    private String channelOrderNo;
    /**
     * 渠道退款号
     */
    private String channelRefundNo;

}

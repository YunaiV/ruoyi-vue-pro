package cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.order;

import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayAppDO;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayChannelDO;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayMerchantDO;
import cn.iocoder.yudao.coreservice.modules.pay.enums.merchant.PayChannelCodeEnum;
import cn.iocoder.yudao.coreservice.modules.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

/**
 * 支付订单 DO
 *
 * @author 芋道源码
 */
@TableName("pay_order")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayOrderDO extends BaseDO {

    /**
     * 订单编号，数据库自增
     */
    private Long id;
//    /**
//     * 订单号，根据规则生成
//     *
//     * 例如说，P202110132239124200055
//     */
//    private String no;
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

    // ========== 商户相关字段 ==========

    /**
     * 商户订单编号
     * 例如说，内部系统 A 的订单号。需要保证每个 PayMerchantDO 唯一 TODO 芋艿：需要在测试下
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
     * 商户拓展参数
     */
    private String merchantExtra;

    // ========== 订单相关字段 ==========

    /**
     * 支付金额，单位：分
     */
    private Long amount;
    /**
     * 渠道手续费
     *
     * 冗余 {@link PayChannelDO#getFeeRate()}
     */
    private Double channelFeeRate;
    /**
     * 渠道手续金额
     */
    private Long channelFeeAmount;
    /**
     * 支付状态
     *
     * 枚举 {@link PayOrderStatusEnum}
     */
    private Integer status;
    /**
     * 通知商户支付结果的回调状态
     * TODO 芋艿：0 未发送 1 已发送
     */
    private Integer notifyStatus;
    /**
     * 客户端 IP
     */
    private String clientIp;
    /**
     * 订单支付成功时间
     */
    private Date successTime;
    /**
     * 订单失效时间
     */
    private Date expireTime;
    /**
     * 支付渠道的额外参数
     *
     * 参见 https://www.pingxx.com/api/支付渠道%20extra%20参数说明.html
     */
    private String channelExtra;
    /**
     * 异步通知地址
     */
    private String notifyUrl;
    /**
     * 页面跳转地址
     */
    private String returnUrl;

    // TODO 芋艿：可能要优化
    /**
     * 渠道支付错误码
     */
    private String errorCode;
    /**
     * 渠道支付错误消息
     */
    private String errorMessage;

    // ========== 退款相关字段 ==========
    /**
     * 退款状态
     *
     * TODO 芋艿：0 - 未退款；1 - 部分退款； 2 - 全额退款
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

package cn.iocoder.yudao.module.pay.dal.dataobject.wallet;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.refund.PayRefundDO;
import cn.iocoder.yudao.module.pay.enums.refund.PayRefundStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会员钱包充值
 */
@TableName(value ="pay_wallet_recharge")
@KeySequence("pay_wallet_recharge_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class PayWalletRechargeDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 钱包编号
     *
     * 关联 {@link PayWalletDO#getId()}
     */
    private Long walletId;

    /**
     * 用户实际到账余额
     *
     * 例如充 100 送 20，则该值是 120
     */
    private Integer totalPrice;
    /**
     * 实际支付金额
     */
    private Integer payPrice;
    /**
     * 钱包赠送金额
     */
    private Integer bonusPrice;

    /**
     * 充值套餐编号
     *
     * 关联 {@link PayWalletRechargeDO#getPackageId()} 字段
     */
    private Long packageId;

    /**
     * 是否已支付
     *
     * true - 已支付
     * false - 未支付
     */
    private Boolean payStatus;

    /**
     * 支付订单编号
     *
     * 关联 {@link PayOrderDO#getId()}
     */
    private Long payOrderId;

    /**
     * 支付成功的支付渠道
     *
     * 冗余 {@link PayOrderDO#getChannelCode()}
     */
    private String payChannelCode;
    /**
     * 订单支付时间
     */
    private LocalDateTime payTime;

    /**
     * 支付退款单编号
     *
     * 关联 {@link PayRefundDO#getId()}
     */
    private Long payRefundId;

    /**
     * 退款金额，包含赠送金额
     */
    private Integer refundTotalPrice;
    /**
     * 退款支付金额
     */
    private Integer refundPayPrice;

    /**
     * 退款钱包赠送金额
     */
    private Integer refundBonusPrice;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

    /**
     * 退款状态
     *
     * 枚举 {@link PayRefundStatusEnum}
     */
    private Integer refundStatus;

}

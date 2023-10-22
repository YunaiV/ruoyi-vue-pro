package cn.iocoder.yudao.module.statistics.dal.dataobject.trade;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 交易统计 DO
 * <p>
 * 以天为维度，统计全部的数据
 *
 * @author 芋道源码
 */
@TableName("trade_statistics")
@KeySequence("trade_statistics_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeStatisticsDO extends TenantBaseDO {

    /**
     * 编号，主键自增
     */
    @TableId
    private Long id;

    /**
     * 统计日期
     */
    private LocalDateTime time;

    /**
     * 创建订单数
     */
    private Integer orderCreateCount;
    /**
     * 支付订单商品数
     */
    private Integer orderPayCount;
    /**
     * 总支付金额，单位：分
     */
    private Integer orderPayPrice;

    /**
     * 退款订单数
     */
    private Integer afterSaleCount;
    /**
     * 总退款金额，单位：分
     */
    private Integer afterSaleRefundPrice;

    /**
     * 佣金金额（已结算），单位：分
     */
    private Integer brokerageSettlementPrice;

    /**
     * 总支付金额（余额），单位：分
     */
    private Integer walletPayPrice;
    /**
     * 充值订单数
     * <p>
     * 从 PayWalletRechargeDO 计算
     */
    private Integer rechargePayCount;
    /**
     * 充值金额，单位：分
     */
    private Integer rechargePayPrice;
    /**
     * 充值退款订单数
     */
    private Integer rechargeRefundCount;
    /**
     * 充值退款金额，单位：分
     */
    private Integer rechargeRefundPrice;

}

package cn.iocoder.yudao.module.trade.dal.dataobject.config;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.mybatis.core.type.IntegerListTypeHandler;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageBindModeEnum;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageEnabledConditionEnum;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageWithdrawTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.util.List;

/**
 * 交易中心配置 DO
 *
 * @author owen
 */
@TableName(value = "trade_config", autoResultMap = true)
@KeySequence("trade_config_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeConfigDO extends BaseDO {

    /**
     * 自增主键
     */
    @TableId
    private Long id;

    // ========== 售后相关 ==========

    /**
     * 售后的退款理由
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> afterSaleRefundReasons;
    /**
     * 售后的退货理由
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> afterSaleReturnReasons;

    // ========== 配送相关 ==========

    /**
     * 是否启用全场包邮
     */
    private Boolean deliveryExpressFreeEnabled;
    /**
     * 全场包邮的最小金额，单位：分
     */
    private Integer deliveryExpressFreePrice;

    /**
     * 是否开启自提
     */
    private Boolean deliveryPickUpEnabled;

    // ========== 分销相关 ==========

    /**
     * 是否启用分佣
     */
    private Boolean brokerageEnabled;
    /**
     * 分佣模式
     * <p>
     * 枚举 {@link BrokerageEnabledConditionEnum 对应的类}
     */
    private Integer brokerageEnabledCondition;
    /**
     * 分销关系绑定模式
     * <p>
     * 枚举 {@link BrokerageBindModeEnum 对应的类}
     */
    private Integer brokerageBindMode;
    /**
     * 分销海报图地址数组
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> brokeragePosterUrls;
    /**
     * 一级返佣比例
     */
    private Integer brokerageFirstPercent;
    /**
     * 二级返佣比例
     */
    private Integer brokerageSecondPercent;
    /**
     * 用户提现最低金额
     */
    private Integer brokerageWithdrawMinPrice;
    /**
     * 用户提现手续费百分比
     */
    private Integer brokerageWithdrawFeePercent;
    /**
     * 佣金冻结时间(天)
     */
    private Integer brokerageFrozenDays;
    /**
     * 提现方式
     * <p>
     * 枚举 {@link BrokerageWithdrawTypeEnum 对应的类}
     */
    @TableField(typeHandler = IntegerListTypeHandler.class)
    private List<Integer> brokerageWithdrawTypes;

}

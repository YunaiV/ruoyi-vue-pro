package cn.iocoder.yudao.module.member.dal.dataobject.point;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.mybatis.core.type.IntegerListTypeHandler;
import cn.iocoder.yudao.module.member.enums.brokerage.BrokerageBindModeEnum;
import cn.iocoder.yudao.module.member.enums.brokerage.BrokerageEnabledConditionEnum;
import cn.iocoder.yudao.module.member.enums.brokerage.BrokerageWithdrawTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.util.List;

/**
 * 会员积分配置 DO
 *
 * @author QingX
 */
@TableName(value = "member_point_config", autoResultMap = true)
@KeySequence("member_point_config_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberPointConfigDO extends BaseDO {

    /**
     * 自增主键
     */
    @TableId
    private Long id;
    /**
     * 积分抵扣开关
     */
    private Boolean tradeDeductEnable;
    /**
     * 积分抵扣，单位：分
     *
     * 1 积分抵扣多少分
     */
    private Integer tradeDeductUnitPrice;
    /**
     * 积分抵扣最大值
     */
    private Integer tradeDeductMaxPrice;
    /**
     * 1 元赠送多少分
     */
    private Integer tradeGivePoint;

    // ========== 分销相关 ==========

    /**
     * 是否启用分佣
     */
    private Boolean brokerageEnabled;
    /**
     * 分佣模式
     *
     * 枚举 {@link BrokerageEnabledConditionEnum 对应的类}
     */
    private Integer brokerageEnabledCondition;
    /**
     * 分销关系绑定模式
     *
     * 枚举 {@link BrokerageBindModeEnum 对应的类}
     */
    private Integer brokerageBindMode;
    /**
     * 分销海报图地址数组
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> brokeragePostUrls;
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
     * 提现银行
     */
    @TableField(typeHandler = IntegerListTypeHandler.class)
    private List<Integer> brokerageBankNames;
    /**
     * 佣金冻结时间(天)
     */
    private Integer brokerageFrozenDays;
    /**
     * 提现方式
     *
     * 枚举 {@link BrokerageWithdrawTypeEnum 对应的类}
     */
    @TableField(typeHandler = IntegerListTypeHandler.class)
    private List<Integer> brokerageWithdrawType;

}

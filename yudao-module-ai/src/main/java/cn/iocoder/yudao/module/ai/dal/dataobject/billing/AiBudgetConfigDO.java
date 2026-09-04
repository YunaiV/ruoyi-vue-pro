package cn.iocoder.yudao.module.ai.dal.dataobject.billing;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.ai.enums.billing.AiBudgetPeriodTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * AI 预算配置 DO
 *
 * @author 芋道源码
 */
@TableName("ai_budget_config")
@KeySequence("ai_budget_config_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiBudgetConfigDO extends TenantBaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 用户编号，0 表示租户级预算
     */
    private Long userId;
    /**
     * 周期类型
     *
     * 枚举 {@link AiBudgetPeriodTypeEnum}
     */
    private String periodType;
    /**
     * 币种，首期固定 CNY
     */
    private String currency;
    /**
     * 预算金额，单位：微元
     */
    private Long budgetAmount;
    /**
     * 告警阈值配置，例如 JSON [80,90,100]
     */
    private String alertThresholds;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}

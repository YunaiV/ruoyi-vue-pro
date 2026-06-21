package cn.iocoder.yudao.module.ai.dal.dataobject.billing;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.ai.enums.billing.AiBizTypeEnum;
import cn.iocoder.yudao.module.ai.enums.billing.AiBudgetEventTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * AI 预算事件日志 DO
 *
 * @author 芋道源码
 */
@TableName("ai_budget_log")
@KeySequence("ai_budget_log_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiBudgetLogDO extends TenantBaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 用户编号，0 表示租户级事件
     */
    private Long userId;
    /**
     * 事件类型
     *
     * 枚举 {@link AiBudgetEventTypeEnum}
     */
    private String eventType;
    /**
     * 周期开始时间
     */
    private LocalDateTime periodStartTime;
    /**
     * 币种，首期固定 CNY
     */
    private String currency;
    /**
     * 该周期预算金额，单位：微元
     */
    private Long budgetAmount;
    /**
     * 事件发生时的已用金额，单位：微元
     */
    private Long usedAmount;
    /**
     * 本次事件涉及的金额变化，单位：微元，可为空
     */
    private Long deltaAmount;
    /**
     * 触发事件的业务类型
     *
     * 枚举 {@link AiBizTypeEnum}
     */
    private String requestBizType;
    /**
     * 触发事件的业务主键，例如 ai_model_call_log.id
     */
    private Long requestBizId;
    /**
     * 描述信息，例如"达到80%阈值触发告警"
     */
    private String message;

}

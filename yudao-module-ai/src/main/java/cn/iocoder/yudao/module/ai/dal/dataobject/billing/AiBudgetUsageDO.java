package cn.iocoder.yudao.module.ai.dal.dataobject.billing;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * AI 预算用量 DO
 *
 * @author 芋道源码
 */
@TableName("ai_budget_usage")
@KeySequence("ai_budget_usage_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiBudgetUsageDO extends TenantBaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 用户编号，0 表示租户级汇总用量
     */
    private Long userId;
    /**
     * 周期开始时间，例如当月 1 号 00:00
     */
    private LocalDateTime periodStartTime;
    /**
     * 币种，首期固定 CNY
     */
    private String currency;
    /**
     * 已用金额，单位：微元
     */
    private Long usedAmount;
    /**
     * 版本号，用于乐观锁/并发控制
     */
    private Integer version;

}

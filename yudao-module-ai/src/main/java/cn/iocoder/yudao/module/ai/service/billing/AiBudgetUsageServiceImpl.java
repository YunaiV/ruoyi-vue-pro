package cn.iocoder.yudao.module.ai.service.billing;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiBudgetUsageDO;
import cn.iocoder.yudao.module.ai.dal.mysql.billing.AiBudgetUsageMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

/**
 * AI 预算用量 Service 实现类
 *
 * 并发策略：SQL 原子累加（UPDATE SET used_amount = used_amount + delta），
 * 避免先读后写的丢失更新问题。租户级（userId=0）为热点行，
 * 单行 UPDATE 受 InnoDB 行锁限制，预估吞吐 500-2000 TPS。
 *
 * 若并发成为瓶颈，可选优化方向：
 * 1. settle 改异步写入（MQ 消费），削峰填谷
 * 2. 租户级用量不实时写 DB，定期从 Redis 同步（Redis 预扣费已是准确值）
 * 3. 按用户分桶累加，定期合并到租户级汇总行
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class AiBudgetUsageServiceImpl implements AiBudgetUsageService {

    @Resource
    private AiBudgetUsageMapper budgetUsageMapper;

    @Override
    public AiBudgetUsageDO getUsage(Long userId, LocalDateTime periodStartTime) {
        return budgetUsageMapper.selectByUserAndPeriod(userId, periodStartTime);
    }

    @Override
    public void addUsage(Long userId, LocalDateTime periodStartTime, long deltaAmount) {
        Long tenantId = TenantContextHolder.getRequiredTenantId();
        // 1. 尝试 SQL 原子累加（UPDATE ... SET used_amount = used_amount + delta）
        int updated = budgetUsageMapper.incrementUsedAmount(userId, periodStartTime, deltaAmount, tenantId);
        if (updated > 0) {
            return;
        }
        // 2. 记录不存在，插入新记录；并发时可能重复插入，用 DuplicateKeyException 兜底
        try {
            AiBudgetUsageDO usage = AiBudgetUsageDO.builder()
                    .userId(userId)
                    .periodStartTime(periodStartTime)
                    .currency("CNY")
                    .usedAmount(deltaAmount)
                    .version(0)
                    .build();
            budgetUsageMapper.insert(usage);
        } catch (DuplicateKeyException e) {
            // 并发插入冲突，重试原子累加
            log.debug("[addUsage][userId({}) periodStartTime({}) 并发插入冲突，重试累加]", userId, periodStartTime);
            budgetUsageMapper.incrementUsedAmount(userId, periodStartTime, deltaAmount, tenantId);
        }
    }

}

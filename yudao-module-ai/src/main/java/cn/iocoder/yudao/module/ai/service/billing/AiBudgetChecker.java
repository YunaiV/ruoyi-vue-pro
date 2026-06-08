package cn.iocoder.yudao.module.ai.service.billing;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiBudgetConfigDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiBudgetLogDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiBudgetUsageDO;
import cn.iocoder.yudao.module.ai.enums.billing.AiBudgetEventTypeEnum;
import cn.iocoder.yudao.module.ai.enums.billing.AiBudgetPeriodTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.BUDGET_EXCEEDED;

/**
 * AI 预算校验器
 *
 * 使用 Redis Lua 脚本实现多维度原子预扣费：
 * - 用户级支持 DAILY + MONTHLY 双重限制（都要通过）
 * - 租户级同样支持 DAILY + MONTHLY 双重限制
 * - 调用前：预扣估算费用，任一维度超额则拦截
 * - 调用后：用实际费用修正差额
 * - 失败/取消：释放预扣占用
 *
 * Redis Key 格式：ai:budget:{tenantId}:{userId}:{periodStart}
 * - userId=0 表示租户级
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class AiBudgetChecker {

    private static final String KEY_PREFIX = "ai:budget:";
    private static final DateTimeFormatter PERIOD_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * Lua 脚本：多 key 原子预扣费
     *
     * KEYS[i]  = 第 i 个预算 key
     * ARGV[1]  = 预扣金额
     * ARGV[i+1]= 第 i 个预算上限（-1 表示无限制）
     *
     * 返回值：
     * 0 = 成功
     * n = 第 n 个 key 超限（1-based）
     */
    private static final String LUA_PRE_DEDUCT = """
            local amount = tonumber(ARGV[1])
            for i = 1, #KEYS do
                local limit = tonumber(ARGV[i + 1])
                if limit >= 0 then
                    local used = tonumber(redis.call('GET', KEYS[i]) or '0')
                    if (used + amount) > limit then
                        return i
                    end
                end
            end
            for i = 1, #KEYS do
                redis.call('INCRBY', KEYS[i], amount)
            end
            return 0
            """;

    private static final DefaultRedisScript<Long> PRE_DEDUCT_SCRIPT = new DefaultRedisScript<>(LUA_PRE_DEDUCT, Long.class);
    /**
     * Lua 脚本：settle 正向补扣（delta > 0）时，按多 key 最小剩余额度原子补扣。
     *
     * KEYS[i]  = 第 i 个预算 key
     * ARGV[1]  = 期望补扣金额 delta
     * ARGV[i+1]= 第 i 个预算上限（-1 表示无限制）
     *
     * 返回值：实际补扣金额（0 ~ delta）
     */
    private static final String LUA_SETTLE_POSITIVE = """
            local delta = tonumber(ARGV[1])
            local applied = delta
            for i = 1, #KEYS do
                local limit = tonumber(ARGV[i + 1])
                if limit >= 0 then
                    local used = tonumber(redis.call('GET', KEYS[i]) or '0')
                    local remain = limit - used
                    if remain < applied then
                        applied = remain
                    end
                end
            end
            if applied < 0 then
                applied = 0
            end
            if applied > 0 then
                for i = 1, #KEYS do
                    redis.call('INCRBY', KEYS[i], applied)
                end
            end
            return applied
            """;

    private static final DefaultRedisScript<Long> SETTLE_POSITIVE_SCRIPT = new DefaultRedisScript<>(LUA_SETTLE_POSITIVE, Long.class);

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private AiBudgetConfigService budgetConfigService;

    @Resource
    private AiBudgetUsageService budgetUsageService;

    @Resource
    private AiBudgetLogService budgetLogService;

    /**
     * 预扣费（调用模型前调用）
     *
     * @param tenantId       租户编号
     * @param userId         用户编号
     * @param estimatedCost  估算费用（微元）
     * @return 预扣凭证（用于后续修正/释放）
     * @throws cn.iocoder.yudao.framework.common.exception.ServiceException 超预算时抛出 BUDGET_EXCEEDED
     */
    public PreDeductResult preDeduct(Long tenantId, Long userId, long estimatedCost) {
        List<BudgetScope> scopes = new ArrayList<>();
        scopes.addAll(buildBudgetScopes(tenantId, userId));
        if (!Objects.equals(userId, 0L)) {
            scopes.addAll(buildBudgetScopes(tenantId, 0L));
        }
        LocalDateTime userPeriodStart = selectPrimaryPeriodStart(scopes, userId);
        LocalDateTime tenantPeriodStart = selectPrimaryPeriodStart(scopes, 0L);
        if (estimatedCost <= 0) {
            return new PreDeductResult(tenantId, userId, 0,
                    userPeriodStart, tenantPeriodStart, scopes);
        }

        List<BudgetKeyAggregate> aggregates = aggregateBudgetKeys(scopes);
        for (BudgetKeyAggregate aggregate : aggregates) {
            ensureRedisKey(aggregate);
        }

        List<String> keys = aggregates.stream().map(BudgetKeyAggregate::key).collect(Collectors.toList());
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(estimatedCost));
        for (BudgetKeyAggregate aggregate : aggregates) {
            args.add(String.valueOf(aggregate.limit()));
        }
        Long result = stringRedisTemplate.execute(PRE_DEDUCT_SCRIPT, keys, args.toArray());

        if (result != null && result > 0) {
            int exceededIndex = Math.toIntExact(result - 1);
            if (exceededIndex >= 0 && exceededIndex < aggregates.size()) {
                BudgetKeyAggregate exceeded = aggregates.get(exceededIndex);
                logBlockEvent(exceeded.ownerId(), exceeded.periodStart(), exceeded.limit());
            }
            throw exception(BUDGET_EXCEEDED);
        }

        return new PreDeductResult(tenantId, userId, estimatedCost,
                userPeriodStart, tenantPeriodStart, scopes);
    }

    /**
     * 修正预扣费（调用模型后调用）
     *
     * 用实际费用替换预扣费用：delta = actualCost - preDeductResult.amount
     * 同时更新 DB 用量，并检查阈值告警
     *
     * 性能说明：settle 会写 Redis + DB 两层。Redis 修正是 O(1)；
     * DB 层通过 SQL 原子累加写入 ai_budget_usage，租户级为热点行。
     * 若高并发下 DB 成为瓶颈，可将 settle 改为异步 MQ 消费，
     * 或租户级用量仅依赖 Redis，定期批量同步到 DB。
     */
    public void settle(PreDeductResult preDeductResult, long actualCost) {
        try {
            long delta = actualCost - preDeductResult.amount();
            List<BudgetScope> scopes = resolveScopes(preDeductResult);
            List<BudgetKeyAggregate> aggregates = aggregateBudgetKeys(scopes);
            long settledCost = actualCost;

            if (delta > 0) {
                long appliedDelta = applyPositiveDelta(aggregates, delta);
                settledCost = preDeductResult.amount() + appliedDelta;
            } else if (delta < 0) {
                // Redis 退回差额
                for (BudgetKeyAggregate aggregate : aggregates) {
                    stringRedisTemplate.opsForValue().increment(aggregate.key(), delta);
                }
            }

            // DB 落库（最终准绳）
            boolean usagePersisted = true;
            if (settledCost > 0) {
                Map<String, BudgetScope> usageScopes = new LinkedHashMap<>();
                for (BudgetScope scope : scopes) {
                    String usageKey = scope.ownerId() + ":" + scope.periodStart();
                    usageScopes.putIfAbsent(usageKey, scope);
                }
                for (BudgetScope scope : usageScopes.values()) {
                    try {
                        budgetUsageService.addUsage(scope.ownerId(), scope.periodStart(), settledCost);
                    } catch (Exception e) {
                        usagePersisted = false;
                        log.error("[settle][ownerId({}) periodStart({}) settledCost({}) DB 落库失败]",
                                scope.ownerId(), scope.periodStart(), settledCost, e);
                    }
                }
            }

            // 阈值告警依赖 DB 最新值，若落库失败则跳过，避免产生错误告警
            if (usagePersisted) {
                for (BudgetScope scope : scopes) {
                    if (scope.budgetAmount() > 0) {
                        checkThresholdAlerts(scope, settledCost);
                    }
                }
            } else {
                log.warn("[settle][actualCost({}) preDeductAmount({}) DB 落库存在失败，已跳过阈值告警]",
                        actualCost, preDeductResult.amount());
            }
        } catch (Exception e) {
            // settle 不向上抛出，避免调用方误触发 release 造成反向冲销
            log.error("[settle][tenantId({}) userId({}) actualCost({}) 结算失败，已内部兜底不抛出]",
                    preDeductResult.tenantId(), preDeductResult.userId(), actualCost, e);
        }
    }

    /**
     * 释放预扣费（调用失败/取消时调用）
     */
    public void release(PreDeductResult preDeductResult) {
        if (preDeductResult.amount() <= 0) {
            return;
        }
        List<BudgetScope> scopes = resolveScopes(preDeductResult);
        for (BudgetKeyAggregate aggregate : aggregateBudgetKeys(scopes)) {
            stringRedisTemplate.opsForValue().increment(aggregate.key(), -preDeductResult.amount());
        }
    }

    // ========== 阈值告警 ==========

    /**
     * 检查是否跨越阈值并记录告警事件
     *
     * 逻辑：扣费前的使用比例 < 阈值 <= 扣费后的使用比例 → 触发告警
     */
    private void checkThresholdAlerts(BudgetScope scope, long deltaAmount) {
        try {
            long budgetAmount = scope.budgetAmount();
            if (budgetAmount <= 0) {
                return;
            }

            // 解析阈值配置
            List<Integer> thresholds = parseThresholds(scope.alertThresholds());
            if (thresholds.isEmpty()) {
                return;
            }

            // 查询当前已用金额
            AiBudgetUsageDO usage = budgetUsageService.getUsage(scope.ownerId(), scope.periodStart());
            long usedAmount = usage != null ? usage.getUsedAmount() : 0L;
            long previousUsed = usedAmount - deltaAmount;

            double currentPercent = (double) usedAmount / budgetAmount * 100;
            double previousPercent = (double) previousUsed / budgetAmount * 100;

            for (int threshold : thresholds) {
                // 本次扣费跨越了该阈值
                if (previousPercent < threshold && currentPercent >= threshold) {
                    AiBudgetLogDO logDO = AiBudgetLogDO.builder()
                            .userId(scope.ownerId())
                            .eventType(AiBudgetEventTypeEnum.THRESHOLD_ALERT.getType())
                            .periodStartTime(scope.periodStart())
                            .currency("CNY")
                            .budgetAmount(budgetAmount)
                            .usedAmount(usedAmount)
                            .deltaAmount(deltaAmount)
                            .message(StrUtil.format("预算使用达到{}%阈值（已用{}/预算{}微元）",
                                    threshold, usedAmount, budgetAmount))
                            .build();
                    budgetLogService.createBudgetLog(logDO);
                    log.warn("[checkThresholdAlerts][userId({}) periodType({}) 预算使用达到{}%阈值, used={}, budget={}]",
                            scope.ownerId(), scope.periodType(), threshold, usedAmount, budgetAmount);
                }
            }
        } catch (Exception e) {
            log.error("[checkThresholdAlerts][userId({}) periodType({}) 阈值告警检查失败]",
                    scope.ownerId(), scope.periodType(), e);
        }
    }

    /**
     * 记录超限拦截事件
     */
    private void logBlockEvent(Long userId, LocalDateTime periodStart, long budgetAmount) {
        try {
            AiBudgetUsageDO usage = budgetUsageService.getUsage(userId, periodStart);
            long usedAmount = usage != null ? usage.getUsedAmount() : 0L;
            AiBudgetLogDO logDO = AiBudgetLogDO.builder()
                    .userId(userId)
                    .eventType(AiBudgetEventTypeEnum.OVER_LIMIT_BLOCK.getType())
                    .periodStartTime(periodStart)
                    .currency("CNY")
                    .budgetAmount(budgetAmount)
                    .usedAmount(usedAmount)
                    .message(StrUtil.format("预算超限拦截（已用{}/预算{}微元）", usedAmount, budgetAmount))
                    .build();
            budgetLogService.createBudgetLog(logDO);
        } catch (Exception e) {
            log.error("[logBlockEvent][userId({}) 记录拦截事件失败]", userId, e);
        }
    }

    /**
     * 解析阈值配置，例如 "[80,90,100]" → [80, 90, 100]
     */
    private List<Integer> parseThresholds(String alertThresholds) {
        if (StrUtil.isBlank(alertThresholds)) {
            return List.of(80, 90, 100); // 默认阈值
        }
        try {
            return JSONUtil.toList(alertThresholds, Integer.class);
        } catch (Exception e) {
            return List.of(80, 90, 100);
        }
    }

    // ========== 内部方法 ==========

    private String buildKey(Long tenantId, Long userId, String periodStr) {
        return KEY_PREFIX + tenantId + ":" + userId + ":" + periodStr;
    }

    private List<AiBudgetConfigDO> getEnabledBudgetConfigs(Long userId) {
        List<AiBudgetConfigDO> configs = new ArrayList<>(2);
        AiBudgetConfigDO monthly = budgetConfigService.getBudgetConfig(userId, AiBudgetPeriodTypeEnum.MONTHLY.getType());
        if (monthly != null && CommonStatusEnum.ENABLE.getStatus().equals(monthly.getStatus())) {
            configs.add(monthly);
        }
        AiBudgetConfigDO daily = budgetConfigService.getBudgetConfig(userId, AiBudgetPeriodTypeEnum.DAILY.getType());
        if (daily != null && CommonStatusEnum.ENABLE.getStatus().equals(daily.getStatus())) {
            configs.add(daily);
        }
        return configs;
    }

    private List<BudgetScope> buildBudgetScopes(Long tenantId, Long ownerId) {
        List<AiBudgetConfigDO> configs = getEnabledBudgetConfigs(ownerId);
        List<BudgetScope> scopes = new ArrayList<>();
        if (configs.isEmpty()) {
            LocalDateTime periodStart = AiBudgetPeriodHelper.getCurrentPeriodStart(null);
            scopes.add(new BudgetScope(ownerId, AiBudgetPeriodTypeEnum.MONTHLY.getType(), periodStart,
                    buildKey(tenantId, ownerId, periodStart.format(PERIOD_FORMAT)), -1L, null));
            return scopes;
        }
        for (AiBudgetConfigDO config : configs) {
            LocalDateTime periodStart = AiBudgetPeriodHelper.getCurrentPeriodStart(config);
            scopes.add(new BudgetScope(ownerId, config.getPeriodType(), periodStart,
                    buildKey(tenantId, ownerId, periodStart.format(PERIOD_FORMAT)),
                    config.getBudgetAmount(), config.getAlertThresholds()));
        }
        return scopes;
    }

    private LocalDateTime selectPrimaryPeriodStart(List<BudgetScope> scopes, Long ownerId) {
        for (BudgetScope scope : scopes) {
            if (Objects.equals(scope.ownerId(), ownerId)
                    && AiBudgetPeriodTypeEnum.MONTHLY.getType().equals(scope.periodType())) {
                return scope.periodStart();
            }
        }
        for (BudgetScope scope : scopes) {
            if (Objects.equals(scope.ownerId(), ownerId)
                    && AiBudgetPeriodTypeEnum.DAILY.getType().equals(scope.periodType())) {
                return scope.periodStart();
            }
        }
        return AiBudgetPeriodHelper.getCurrentPeriodStart(null);
    }

    private List<BudgetScope> resolveScopes(PreDeductResult preDeductResult) {
        if (preDeductResult.scopes() != null && !preDeductResult.scopes().isEmpty()) {
            return preDeductResult.scopes();
        }
        List<BudgetScope> scopes = new ArrayList<>();
        String userKey = buildKey(preDeductResult.tenantId(), preDeductResult.userId(),
                preDeductResult.periodStart().format(PERIOD_FORMAT));
        scopes.add(new BudgetScope(preDeductResult.userId(), AiBudgetPeriodTypeEnum.MONTHLY.getType(),
                preDeductResult.periodStart(), userKey, -1L, null));
        if (!Objects.equals(preDeductResult.userId(), 0L)) {
            String tenantKey = buildKey(preDeductResult.tenantId(), 0L,
                    preDeductResult.tenantPeriodStart().format(PERIOD_FORMAT));
            scopes.add(new BudgetScope(0L, AiBudgetPeriodTypeEnum.MONTHLY.getType(),
                    preDeductResult.tenantPeriodStart(), tenantKey, -1L, null));
        }
        return scopes;
    }

    private List<BudgetKeyAggregate> aggregateBudgetKeys(List<BudgetScope> scopes) {
        Map<String, BudgetKeyAggregate> keyMap = new LinkedHashMap<>();
        for (BudgetScope scope : scopes) {
            BudgetKeyAggregate current = keyMap.get(scope.redisKey());
            if (current == null) {
                keyMap.put(scope.redisKey(), new BudgetKeyAggregate(scope.redisKey(), scope.ownerId(),
                        scope.periodStart(), scope.periodType(), scope.budgetAmount()));
                continue;
            }
            if (scope.budgetAmount() >= 0
                    && (current.limit() < 0 || scope.budgetAmount() < current.limit())) {
                keyMap.put(scope.redisKey(), new BudgetKeyAggregate(scope.redisKey(), scope.ownerId(),
                        scope.periodStart(), scope.periodType(), scope.budgetAmount()));
            }
        }
        return new ArrayList<>(keyMap.values());
    }

    /**
     * settle 阶段正向补扣（actual > preDeduct）时：
     * - 有限额：使用 Lua 原子补扣，确保不突破任一维度硬限额
     * - 无限额：直接补扣
     *
     * @return 实际补扣金额（0 ~ delta）
     */
    private long applyPositiveDelta(List<BudgetKeyAggregate> aggregates, long delta) {
        boolean hasLimit = false;
        for (BudgetKeyAggregate aggregate : aggregates) {
            ensureRedisKey(aggregate);
            if (aggregate.limit() >= 0) {
                hasLimit = true;
            }
        }
        if (!hasLimit) {
            for (BudgetKeyAggregate aggregate : aggregates) {
                stringRedisTemplate.opsForValue().increment(aggregate.key(), delta);
            }
            return delta;
        }

        List<String> keys = aggregates.stream().map(BudgetKeyAggregate::key).collect(Collectors.toList());
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(delta));
        for (BudgetKeyAggregate aggregate : aggregates) {
            args.add(String.valueOf(aggregate.limit()));
        }
        Long result = stringRedisTemplate.execute(SETTLE_POSITIVE_SCRIPT, keys, args.toArray());
        long appliedDelta = result == null ? 0L : Math.max(0L, Math.min(delta, result));
        if (appliedDelta < delta) {
            log.warn("[applyPositiveDelta][delta({}) appliedDelta({}) 触发硬限额保护，超出部分不再计入预算]", delta, appliedDelta);
        }
        return appliedDelta;
    }

    /**
     * 冷启动：Redis key 不存在时从 DB 加载
     */
    private void ensureRedisKey(BudgetKeyAggregate aggregate) {
        Boolean exists = stringRedisTemplate.hasKey(aggregate.key());
        if (Boolean.TRUE.equals(exists)) {
            return;
        }
        // 从 DB 加载
        AiBudgetUsageDO usage = budgetUsageService.getUsage(aggregate.ownerId(), aggregate.periodStart());
        long usedAmount = usage != null ? usage.getUsedAmount() : 0L;
        // 过期时间：DAILY 2 天，MONTHLY 35 天
        Duration ttl = AiBudgetPeriodTypeEnum.DAILY.getType().equals(aggregate.periodType())
                ? Duration.ofDays(2) : Duration.ofDays(35);
        stringRedisTemplate.opsForValue().setIfAbsent(aggregate.key(), String.valueOf(usedAmount), ttl);
    }

    /**
     * 预扣凭证
     */
    public record PreDeductResult(Long tenantId, Long userId, long amount,
                                  LocalDateTime periodStart, LocalDateTime tenantPeriodStart,
                                  List<BudgetScope> scopes) {

        public PreDeductResult(Long tenantId, Long userId, long amount,
                               LocalDateTime periodStart, LocalDateTime tenantPeriodStart) {
            this(tenantId, userId, amount, periodStart, tenantPeriodStart, List.of());
        }
    }

    private record BudgetScope(Long ownerId, String periodType, LocalDateTime periodStart,
                               String redisKey, long budgetAmount, String alertThresholds) {
    }

    private record BudgetKeyAggregate(String key, Long ownerId, LocalDateTime periodStart,
                                      String periodType, long limit) {
    }

}

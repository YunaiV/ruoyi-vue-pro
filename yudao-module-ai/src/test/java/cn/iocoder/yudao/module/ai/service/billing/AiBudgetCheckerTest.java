package cn.iocoder.yudao.module.ai.service.billing;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiBudgetConfigDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiBudgetLogDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiBudgetUsageDO;
import cn.iocoder.yudao.module.ai.enums.billing.AiBudgetPeriodTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link AiBudgetChecker} 的单元测试
 */
public class AiBudgetCheckerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private AiBudgetChecker budgetChecker;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private AiBudgetConfigService budgetConfigService;

    @Mock
    private AiBudgetUsageService budgetUsageService;

    @Mock
    private AiBudgetLogService budgetLogService;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(budgetUsageService.getUsage(anyLong(), any())).thenReturn(null);
    }

    // ========== preDeduct ==========

    @Test
    public void testPreDeduct_zeroCost() {
        AiBudgetChecker.PreDeductResult result = budgetChecker.preDeduct(1L, 100L, 0);
        assertEquals(0, result.amount());
        assertEquals(1L, result.tenantId());
        assertEquals(100L, result.userId());
    }

    @Test
    public void testPreDeduct_noBudgetConfig_unlimited() {
        // 无预算配置 → 无限制（-1）
        lenient().when(budgetConfigService.getBudgetConfig(anyLong(), anyString())).thenReturn(null);
        lenient().when(stringRedisTemplate.hasKey(anyString())).thenReturn(false);
        lenient().when(valueOperations.setIfAbsent(anyString(), anyString(), any())).thenReturn(true);
        // Lua 脚本返回 0（成功）
        lenient().when(stringRedisTemplate.execute(Mockito.<RedisScript<Long>>any(), anyList(),
                any(String.class), any(String.class), any(String.class)))
                .thenReturn(0L);

        AiBudgetChecker.PreDeductResult result = budgetChecker.preDeduct(1L, 100L, 5000L);
        assertEquals(5000L, result.amount());
    }

    @Test
    public void testPreDeduct_userBudgetExceeded() {
        // 用户预算超限
        AiBudgetConfigDO userConfig = AiBudgetConfigDO.builder()
                .userId(100L).periodType("MONTHLY").budgetAmount(10_000_000L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        lenient().when(budgetConfigService.getBudgetConfig(anyLong(), anyString())).thenReturn(null);
        when(budgetConfigService.getBudgetConfig(eq(100L), eq(AiBudgetPeriodTypeEnum.MONTHLY.getType())))
                .thenReturn(userConfig);
        lenient().when(stringRedisTemplate.hasKey(anyString())).thenReturn(true);
        // Lua 脚本返回 1（用户预算不足）
        lenient().when(stringRedisTemplate.execute(Mockito.<RedisScript<Long>>any(), anyList(),
                any(String.class), any(String.class), any(String.class)))
                .thenReturn(1L);

        assertThrows(ServiceException.class, () -> budgetChecker.preDeduct(1L, 100L, 5000L));
        // 验证记录了拦截事件
        verify(budgetLogService).createBudgetLog(any(AiBudgetLogDO.class));
    }

    @Test
    public void testPreDeduct_tenantBudgetExceeded() {
        lenient().when(budgetConfigService.getBudgetConfig(anyLong(), anyString())).thenReturn(null);
        AiBudgetConfigDO tenantConfig = AiBudgetConfigDO.builder()
                .userId(0L).periodType("MONTHLY").budgetAmount(50_000_000L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        when(budgetConfigService.getBudgetConfig(eq(0L), eq(AiBudgetPeriodTypeEnum.MONTHLY.getType())))
                .thenReturn(tenantConfig);
        lenient().when(stringRedisTemplate.hasKey(anyString())).thenReturn(true);
        // Lua 脚本返回 2（租户预算不足）
        lenient().when(stringRedisTemplate.execute(Mockito.<RedisScript<Long>>any(), anyList(),
                any(String.class), any(String.class), any(String.class)))
                .thenReturn(2L);

        assertThrows(ServiceException.class, () -> budgetChecker.preDeduct(1L, 100L, 5000L));
        verify(budgetLogService).createBudgetLog(any(AiBudgetLogDO.class));
    }

    // ========== settle ==========

    @Test
    public void testSettle_actualCostLessThanPreDeduct() {
        lenient().when(budgetConfigService.getBudgetConfig(anyLong(), anyString())).thenReturn(null);

        LocalDateTime periodStart = LocalDateTime.of(2026, 2, 1, 0, 0, 0);
        AiBudgetChecker.PreDeductResult preDeduct = new AiBudgetChecker.PreDeductResult(1L, 100L, 10000L, periodStart, periodStart);

        budgetChecker.settle(preDeduct, 6000L);

        // delta = 6000 - 10000 = -4000（退回差额）
        verify(valueOperations, atLeastOnce()).increment(contains(":100:"), eq(-4000L));
        verify(valueOperations, atLeastOnce()).increment(contains(":0:"), eq(-4000L));
        // DB 落库
        verify(budgetUsageService).addUsage(eq(100L), eq(periodStart), eq(6000L));
        verify(budgetUsageService).addUsage(eq(0L), any(LocalDateTime.class), eq(6000L));
    }

    @Test
    public void testSettle_zeroCost() {
        lenient().when(budgetConfigService.getBudgetConfig(anyLong(), anyString())).thenReturn(null);

        LocalDateTime periodStart = LocalDateTime.of(2026, 2, 1, 0, 0, 0);
        AiBudgetChecker.PreDeductResult preDeduct = new AiBudgetChecker.PreDeductResult(1L, 100L, 5000L, periodStart, periodStart);

        budgetChecker.settle(preDeduct, 0L);

        // delta = 0 - 5000 = -5000
        verify(valueOperations, atLeastOnce()).increment(contains(":100:"), eq(-5000L));
        // 不落库（actualCost = 0）
        verify(budgetUsageService, never()).addUsage(anyLong(), any(), anyLong());
    }

    // ========== release ==========

    @Test
    public void testRelease_zeroAmount() {
        LocalDateTime periodStart = LocalDateTime.of(2026, 2, 1, 0, 0, 0);
        AiBudgetChecker.PreDeductResult preDeduct = new AiBudgetChecker.PreDeductResult(1L, 100L, 0, periodStart, periodStart);

        budgetChecker.release(preDeduct);

        // 不应调用 Redis
        verify(valueOperations, never()).increment(anyString(), anyLong());
    }

    @Test
    public void testRelease_positiveAmount() {
        LocalDateTime periodStart = LocalDateTime.of(2026, 2, 1, 0, 0, 0);
        AiBudgetChecker.PreDeductResult preDeduct = new AiBudgetChecker.PreDeductResult(1L, 100L, 8000L, periodStart, periodStart);

        budgetChecker.release(preDeduct);

        verify(valueOperations).increment(contains(":100:"), eq(-8000L));
        verify(valueOperations).increment(contains(":0:"), eq(-8000L));
    }

    // ========== settle 阈值告警 ==========

    @Test
    public void testSettle_thresholdAlert_triggered() {
        // 配置：预算 100,000 微元，阈值 [80, 90, 100]
        AiBudgetConfigDO config = AiBudgetConfigDO.builder()
                .userId(100L).periodType("MONTHLY").budgetAmount(100_000L)
                .alertThresholds("[80,90,100]")
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        lenient().when(budgetConfigService.getBudgetConfig(anyLong(), anyString())).thenReturn(null);
        when(budgetConfigService.getBudgetConfig(eq(100L), eq("MONTHLY"))).thenReturn(config);
        lenient().when(stringRedisTemplate.hasKey(anyString())).thenReturn(true);
        lenient().when(stringRedisTemplate.execute(Mockito.<RedisScript<Long>>any(), anyList(),
                any(String.class), any(String.class), any(String.class))).thenReturn(0L);

        // 已用 75,000，本次扣 10,000 → 85,000（跨越 80% 阈值）
        AiBudgetUsageDO usage = AiBudgetUsageDO.builder().usedAmount(85_000L).build();
        when(budgetUsageService.getUsage(eq(100L), any())).thenReturn(usage);

        AiBudgetChecker.PreDeductResult preDeduct = budgetChecker.preDeduct(1L, 100L, 10_000L);

        budgetChecker.settle(preDeduct, 10000L);

        // 验证写入了阈值告警日志（80% 阈值被跨越）
        verify(budgetLogService, atLeastOnce()).createBudgetLog(argThat(log ->
                "THRESHOLD_ALERT".equals(log.getEventType())
                        && log.getUserId().equals(100L)
                        && log.getMessage().contains("80%")));
    }

    @Test
    public void testSettle_thresholdAlert_notTriggered() {
        // 配置：预算 100,000 微元，阈值 [80]
        AiBudgetConfigDO config = AiBudgetConfigDO.builder()
                .userId(100L).periodType("MONTHLY").budgetAmount(100_000L)
                .alertThresholds("[80]")
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        lenient().when(budgetConfigService.getBudgetConfig(anyLong(), anyString())).thenReturn(null);
        when(budgetConfigService.getBudgetConfig(eq(100L), eq("MONTHLY"))).thenReturn(config);
        lenient().when(stringRedisTemplate.hasKey(anyString())).thenReturn(true);
        lenient().when(stringRedisTemplate.execute(Mockito.<RedisScript<Long>>any(), anyList(),
                any(String.class), any(String.class), any(String.class))).thenReturn(0L);

        // 已用 50,000，本次扣 10,000 → 60,000（未跨越 80%）
        AiBudgetUsageDO usage = AiBudgetUsageDO.builder().usedAmount(60_000L).build();
        when(budgetUsageService.getUsage(eq(100L), any())).thenReturn(usage);

        AiBudgetChecker.PreDeductResult preDeduct = budgetChecker.preDeduct(1L, 100L, 10_000L);

        budgetChecker.settle(preDeduct, 10000L);

        // 不应写入阈值告警日志（只有 addUsage 的调用，没有 THRESHOLD_ALERT）
        verify(budgetLogService, never()).createBudgetLog(argThat(log ->
                "THRESHOLD_ALERT".equals(log.getEventType())));
    }

    @Test
    public void testSettle_multipleThresholdsCrossed() {
        // 配置：预算 100,000 微元，阈值 [80, 90, 100]
        AiBudgetConfigDO config = AiBudgetConfigDO.builder()
                .userId(100L).periodType("MONTHLY").budgetAmount(100_000L)
                .alertThresholds("[80,90,100]")
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        lenient().when(budgetConfigService.getBudgetConfig(anyLong(), anyString())).thenReturn(null);
        when(budgetConfigService.getBudgetConfig(eq(100L), eq("MONTHLY"))).thenReturn(config);
        lenient().when(stringRedisTemplate.hasKey(anyString())).thenReturn(true);
        lenient().when(stringRedisTemplate.execute(Mockito.<RedisScript<Long>>any(), anyList(),
                any(String.class), any(String.class), any(String.class))).thenReturn(0L);

        // 已用 70,000，本次扣 25,000 → 95,000（跨越 80% 和 90% 两个阈值）
        AiBudgetUsageDO usage = AiBudgetUsageDO.builder().usedAmount(95_000L).build();
        when(budgetUsageService.getUsage(eq(100L), any())).thenReturn(usage);

        AiBudgetChecker.PreDeductResult preDeduct = budgetChecker.preDeduct(1L, 100L, 25_000L);

        budgetChecker.settle(preDeduct, 25000L);

        // 应写入两条告警日志（80% 和 90%）
        verify(budgetLogService, atLeast(2)).createBudgetLog(argThat(log ->
                "THRESHOLD_ALERT".equals(log.getEventType()) && log.getUserId().equals(100L)));
    }

    @Test
    public void testSettle_thresholdAlert_noBudgetConfig() {
        // 无预算配置 → 不触发告警
        lenient().when(budgetConfigService.getBudgetConfig(anyLong(), anyString())).thenReturn(null);

        LocalDateTime periodStart = LocalDateTime.of(2026, 2, 1, 0, 0, 0);
        AiBudgetChecker.PreDeductResult preDeduct = new AiBudgetChecker.PreDeductResult(1L, 100L, 5000L, periodStart, periodStart);

        budgetChecker.settle(preDeduct, 5000L);

        // 不应写入告警日志
        verify(budgetLogService, never()).createBudgetLog(argThat(log ->
                "THRESHOLD_ALERT".equals(log.getEventType())));
    }

    @Test
    public void testSettle_thresholdAlert_disabledConfig() {
        // 禁用的预算配置 → 不触发告警
        AiBudgetConfigDO config = AiBudgetConfigDO.builder()
                .userId(100L).periodType("MONTHLY").budgetAmount(100_000L)
                .alertThresholds("[80]")
                .status(CommonStatusEnum.DISABLE.getStatus()).build();
        lenient().when(budgetConfigService.getBudgetConfig(anyLong(), anyString())).thenReturn(null);
        when(budgetConfigService.getBudgetConfig(eq(100L), eq("MONTHLY"))).thenReturn(config);
        lenient().when(stringRedisTemplate.hasKey(anyString())).thenReturn(true);
        lenient().when(stringRedisTemplate.execute(Mockito.<RedisScript<Long>>any(), anyList(),
                any(String.class), any(String.class), any(String.class))).thenReturn(0L);

        AiBudgetChecker.PreDeductResult preDeduct = budgetChecker.preDeduct(1L, 100L, 5_000L);

        budgetChecker.settle(preDeduct, 5000L);

        verify(budgetLogService, never()).createBudgetLog(argThat(log ->
                "THRESHOLD_ALERT".equals(log.getEventType())));
    }

    // ========== DAILY 周期 ==========

    @Test
    public void testPreDeduct_dailyBudgetConfig() {
        // 用户配置 DAILY 预算
        LocalDateTime dailyAnchor = LocalDateTime.now().minusHours(30).withNano(0);
        AiBudgetConfigDO dailyConfig = AiBudgetConfigDO.builder()
                .userId(100L).periodType("DAILY").budgetAmount(5_000_000L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        dailyConfig.setCreateTime(dailyAnchor);
        lenient().when(budgetConfigService.getBudgetConfig(anyLong(), anyString())).thenReturn(null);
        when(budgetConfigService.getBudgetConfig(eq(100L), eq("DAILY"))).thenReturn(dailyConfig);
        lenient().when(stringRedisTemplate.hasKey(anyString())).thenReturn(true);
        lenient().when(stringRedisTemplate.execute(Mockito.<RedisScript<Long>>any(), anyList(),
                any(String.class), any(String.class), any(String.class))).thenReturn(0L);

        AiBudgetChecker.PreDeductResult result = budgetChecker.preDeduct(1L, 100L, 3000L);

        assertEquals(3000L, result.amount());
        assertEquals(100L, result.userId());
        // DAILY 应按配置创建时间做滚动 24h 周期锚点（30h 前 -> 当前周期起点为 6h 前）
        assertEquals(dailyAnchor.plusDays(1), result.periodStart());
    }

    @Test
    public void testPreDeduct_dailyBudgetExceeded() {
        LocalDateTime dailyAnchor = LocalDateTime.now().minusHours(3).withNano(0);
        AiBudgetConfigDO dailyConfig = AiBudgetConfigDO.builder()
                .userId(100L).periodType("DAILY").budgetAmount(1_000_000L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        dailyConfig.setCreateTime(dailyAnchor);
        lenient().when(budgetConfigService.getBudgetConfig(anyLong(), anyString())).thenReturn(null);
        when(budgetConfigService.getBudgetConfig(eq(100L), eq("DAILY"))).thenReturn(dailyConfig);
        lenient().when(stringRedisTemplate.hasKey(anyString())).thenReturn(true);
        // Lua 返回 1（用户预算不足）
        lenient().when(stringRedisTemplate.execute(Mockito.<RedisScript<Long>>any(), anyList(),
                any(String.class), any(String.class), any(String.class))).thenReturn(1L);

        assertThrows(ServiceException.class, () -> budgetChecker.preDeduct(1L, 100L, 5000L));
        verify(budgetLogService).createBudgetLog(any(AiBudgetLogDO.class));
    }

    @Test
    public void testPreDeduct_userDailyAndMonthlyEnabled_dailyExceededShouldBlock() {
        LocalDateTime anchorTime = LocalDateTime.of(2025, 1, 20, 10, 11, 12);
        AiBudgetConfigDO monthlyConfig = AiBudgetConfigDO.builder()
                .userId(100L).periodType("MONTHLY").budgetAmount(10_000_000L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        monthlyConfig.setCreateTime(anchorTime);
        AiBudgetConfigDO dailyConfig = AiBudgetConfigDO.builder()
                .userId(100L).periodType("DAILY").budgetAmount(1_000_000L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        dailyConfig.setCreateTime(LocalDateTime.now().minusHours(4).withNano(0));

        lenient().when(budgetConfigService.getBudgetConfig(anyLong(), anyString())).thenReturn(null);
        when(budgetConfigService.getBudgetConfig(eq(100L), eq("MONTHLY"))).thenReturn(monthlyConfig);
        when(budgetConfigService.getBudgetConfig(eq(100L), eq("DAILY"))).thenReturn(dailyConfig);
        when(budgetConfigService.getBudgetConfig(eq(0L), eq("MONTHLY"))).thenReturn(null);
        when(budgetConfigService.getBudgetConfig(eq(0L), eq("DAILY"))).thenReturn(null);
        lenient().when(stringRedisTemplate.hasKey(anyString())).thenReturn(true);
        // key 顺序是用户 MONTHLY、用户 DAILY、租户默认 MONTHLY；返回 2 表示用户 DAILY 超限
        lenient().when(stringRedisTemplate.execute(Mockito.<RedisScript<Long>>any(), anyList(),
                any(String.class), any(String.class), any(String.class), any(String.class))).thenReturn(2L);

        assertThrows(ServiceException.class, () -> budgetChecker.preDeduct(1L, 100L, 5000L));
        verify(budgetLogService).createBudgetLog(argThat(log ->
                log.getUserId().equals(100L)
                        && log.getPeriodStartTime().equals(AiBudgetPeriodHelper.getCurrentPeriodStart(dailyConfig))));
    }

    // ========== settle 实际费用大于预扣 ==========

    @Test
    public void testSettle_actualCostGreaterThanPreDeduct() {
        lenient().when(budgetConfigService.getBudgetConfig(anyLong(), anyString())).thenReturn(null);

        LocalDateTime periodStart = LocalDateTime.of(2026, 2, 1, 0, 0, 0);
        AiBudgetChecker.PreDeductResult preDeduct = new AiBudgetChecker.PreDeductResult(1L, 100L, 5000L, periodStart, periodStart);

        budgetChecker.settle(preDeduct, 8000L);

        // delta = 8000 - 5000 = 3000（补扣差额）
        verify(valueOperations, atLeastOnce()).increment(contains(":100:"), eq(3000L));
        // DB 落库
        verify(budgetUsageService).addUsage(eq(100L), eq(periodStart), eq(8000L));
    }

    @Test
    public void testSettle_actualCostGreaterThanPreDeduct_withLimit_shouldCapByBudget() {
        AiBudgetConfigDO userConfig = AiBudgetConfigDO.builder()
                .userId(100L).periodType("MONTHLY").budgetAmount(6_000L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        lenient().when(budgetConfigService.getBudgetConfig(anyLong(), anyString())).thenReturn(null);
        when(budgetConfigService.getBudgetConfig(eq(100L), eq(AiBudgetPeriodTypeEnum.MONTHLY.getType())))
                .thenReturn(userConfig);
        when(budgetConfigService.getBudgetConfig(eq(100L), eq(AiBudgetPeriodTypeEnum.DAILY.getType())))
                .thenReturn(null);
        when(budgetConfigService.getBudgetConfig(eq(0L), eq(AiBudgetPeriodTypeEnum.MONTHLY.getType())))
                .thenReturn(null);
        when(budgetConfigService.getBudgetConfig(eq(0L), eq(AiBudgetPeriodTypeEnum.DAILY.getType())))
                .thenReturn(null);
        lenient().when(stringRedisTemplate.hasKey(anyString())).thenReturn(true);
        // 第一次 execute：preDeduct 成功；第二次 execute：settle 正向补扣仅允许 1000
        lenient().when(stringRedisTemplate.execute(Mockito.<RedisScript<Long>>any(), anyList(),
                any(String.class), any(String.class), any(String.class)))
                .thenReturn(0L, 1000L);

        AiBudgetChecker.PreDeductResult preDeduct = budgetChecker.preDeduct(1L, 100L, 5000L);

        // 期望补扣 3000，但受限额仅补扣 1000，最终记账 6000
        budgetChecker.settle(preDeduct, 8000L);

        verify(budgetUsageService).addUsage(eq(100L), any(LocalDateTime.class), eq(6000L));
        verify(budgetUsageService).addUsage(eq(0L), any(LocalDateTime.class), eq(6000L));
        verify(valueOperations, never()).increment(anyString(), eq(3000L));
    }

    @Test
    public void testSettle_whenDbPersistFailed_shouldNotThrow() {
        lenient().when(budgetConfigService.getBudgetConfig(anyLong(), anyString())).thenReturn(null);
        doThrow(new RuntimeException("db error")).when(budgetUsageService)
                .addUsage(anyLong(), any(LocalDateTime.class), anyLong());

        LocalDateTime periodStart = LocalDateTime.of(2026, 2, 1, 0, 0, 0);
        AiBudgetChecker.PreDeductResult preDeduct = new AiBudgetChecker.PreDeductResult(1L, 100L, 5000L, periodStart, periodStart);

        // 结算阶段 DB 失败不应继续向上抛出，否则调用方可能触发错误 release
        assertDoesNotThrow(() -> budgetChecker.settle(preDeduct, 8000L));
        verify(valueOperations, atLeastOnce()).increment(contains(":100:"), eq(3000L));
    }

    @Test
    public void testSettle_shouldUseTenantPeriodStartFromPreDeductResult() {
        lenient().when(budgetConfigService.getBudgetConfig(anyLong(), anyString())).thenReturn(null);

        LocalDateTime userPeriodStart = LocalDateTime.of(2026, 2, 25, 0, 0, 0);
        LocalDateTime tenantPeriodStart = LocalDateTime.of(2026, 2, 1, 0, 0, 0);
        AiBudgetChecker.PreDeductResult preDeduct = new AiBudgetChecker.PreDeductResult(
                1L, 100L, 5000L, userPeriodStart, tenantPeriodStart);

        budgetChecker.settle(preDeduct, 7000L);

        // delta = 7000 - 5000 = 2000，应修正到预扣时的租户周期 key（20260201000000），不能重算当前周期
        verify(valueOperations).increment(eq("ai:budget:1:0:20260201000000"), eq(2000L));
    }

    @Test
    public void testRelease_shouldUseTenantPeriodStartFromPreDeductResult() {
        LocalDateTime userPeriodStart = LocalDateTime.of(2026, 2, 25, 0, 0, 0);
        LocalDateTime tenantPeriodStart = LocalDateTime.of(2026, 2, 1, 0, 0, 0);
        AiBudgetChecker.PreDeductResult preDeduct = new AiBudgetChecker.PreDeductResult(
                1L, 100L, 8000L, userPeriodStart, tenantPeriodStart);

        budgetChecker.release(preDeduct);

        // 释放应回写预扣时的租户周期 key（20260201000000），不能重算当前周期
        verify(valueOperations).increment(eq("ai:budget:1:0:20260201000000"), eq(-8000L));
    }

    @Test
    public void testPreDeduct_tenantBudgetExceeded_shouldLogWithTenantPeriodStart() {
        // 用户是 DAILY，租户是 MONTHLY，触发租户超限时应记录租户周期（月初）
        AiBudgetConfigDO userDailyConfig = AiBudgetConfigDO.builder()
                .userId(100L).periodType("DAILY").budgetAmount(10_000_000L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        AiBudgetConfigDO tenantMonthlyConfig = AiBudgetConfigDO.builder()
                .userId(0L).periodType("MONTHLY").budgetAmount(50_000_000L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();

        lenient().when(budgetConfigService.getBudgetConfig(anyLong(), anyString())).thenReturn(null);
        when(budgetConfigService.getBudgetConfig(eq(100L), eq(AiBudgetPeriodTypeEnum.MONTHLY.getType())))
                .thenReturn(null);
        when(budgetConfigService.getBudgetConfig(eq(100L), eq(AiBudgetPeriodTypeEnum.DAILY.getType())))
                .thenReturn(userDailyConfig);
        when(budgetConfigService.getBudgetConfig(eq(0L), eq(AiBudgetPeriodTypeEnum.MONTHLY.getType())))
                .thenReturn(tenantMonthlyConfig);

        lenient().when(stringRedisTemplate.hasKey(anyString())).thenReturn(true);
        // Lua 脚本返回 2（租户预算不足）
        lenient().when(stringRedisTemplate.execute(Mockito.<RedisScript<Long>>any(), anyList(),
                any(String.class), any(String.class), any(String.class))).thenReturn(2L);

        assertThrows(ServiceException.class, () -> budgetChecker.preDeduct(1L, 100L, 5000L));

        LocalDateTime expectedTenantPeriodStart = LocalDateTime.now()
                .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        verify(budgetLogService).createBudgetLog(argThat(log ->
                log.getUserId().equals(0L)
                        && log.getPeriodStartTime().equals(expectedTenantPeriodStart)));
    }

    @Test
    public void testPreDeduct_monthlyBudgetConfig_shouldUseCreateDayAsAnchor() {
        int anchorDay = 20;
        LocalDateTime anchorTime = LocalDateTime.of(2025, 1, anchorDay, 10, 11, 12);
        AiBudgetConfigDO monthlyConfig = AiBudgetConfigDO.builder()
                .userId(100L).periodType("MONTHLY").budgetAmount(10_000_000L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        monthlyConfig.setCreateTime(anchorTime);
        lenient().when(budgetConfigService.getBudgetConfig(anyLong(), anyString())).thenReturn(null);
        when(budgetConfigService.getBudgetConfig(eq(100L), eq("MONTHLY"))).thenReturn(monthlyConfig);
        lenient().when(stringRedisTemplate.hasKey(anyString())).thenReturn(true);
        lenient().when(stringRedisTemplate.execute(Mockito.<RedisScript<Long>>any(), anyList(),
                any(String.class), any(String.class), any(String.class))).thenReturn(0L);

        LocalDateTime before = LocalDateTime.now();
        AiBudgetChecker.PreDeductResult result = budgetChecker.preDeduct(1L, 100L, 3000L);
        LocalDateTime after = LocalDateTime.now();

        LocalDateTime expectedBefore = calcMonthlyAnchorStart(anchorTime, before);
        LocalDateTime expectedAfter = calcMonthlyAnchorStart(anchorTime, after);
        assertTrue(result.periodStart().equals(expectedBefore) || result.periodStart().equals(expectedAfter));
    }

    private static LocalDateTime calcMonthlyAnchorStart(LocalDateTime anchor, LocalDateTime now) {
        int anchorDay = anchor.getDayOfMonth();
        LocalDate currentDate = now.toLocalDate();
        LocalDate currentMonthAnchorDate = currentDate.withDayOfMonth(Math.min(anchorDay, currentDate.lengthOfMonth()));
        LocalDateTime currentMonthAnchorStart = currentMonthAnchorDate.atTime(
                anchor.getHour(), anchor.getMinute(), anchor.getSecond(), anchor.getNano());
        if (!now.isBefore(currentMonthAnchorStart)) {
            return currentMonthAnchorStart;
        }
        LocalDate previousMonthDate = currentDate.minusMonths(1);
        LocalDate previousMonthAnchorDate = previousMonthDate.withDayOfMonth(
                Math.min(anchorDay, previousMonthDate.lengthOfMonth()));
        return previousMonthAnchorDate.atTime(anchor.getHour(), anchor.getMinute(), anchor.getSecond(), anchor.getNano());
    }

}


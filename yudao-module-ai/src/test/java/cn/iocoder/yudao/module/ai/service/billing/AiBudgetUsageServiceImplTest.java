package cn.iocoder.yudao.module.ai.service.billing;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiBudgetUsageDO;
import cn.iocoder.yudao.module.ai.dal.mysql.billing.AiBudgetUsageMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link AiBudgetUsageServiceImpl} 的单元测试
 */
@Import(AiBudgetUsageServiceImpl.class)
public class AiBudgetUsageServiceImplTest extends BaseDbUnitTest {

    @Resource
    private AiBudgetUsageServiceImpl budgetUsageService;

    @Resource
    private AiBudgetUsageMapper budgetUsageMapper;

    private static final LocalDateTime PERIOD_START = LocalDateTime.of(2026, 2, 1, 0, 0, 0);

    @BeforeEach
    public void setUp() {
        // 注意：BaseDbUnitTest 不加载租户拦截器，insert 时 tenant_id 取 H2 列默认值 0
        TenantContextHolder.setTenantId(0L);
    }

    @Test
    public void testAddUsage_firstTime() {
        // 首次添加用量
        budgetUsageService.addUsage(1L, PERIOD_START, 5_000_000L);

        AiBudgetUsageDO usage = budgetUsageMapper.selectByUserAndPeriod(1L, PERIOD_START);
        assertNotNull(usage);
        assertEquals(1L, usage.getUserId());
        assertEquals(PERIOD_START, usage.getPeriodStartTime());
        assertEquals("CNY", usage.getCurrency());
        assertEquals(5_000_000L, usage.getUsedAmount());
    }

    @Test
    public void testAddUsage_accumulate() {
        // 首次
        budgetUsageService.addUsage(1L, PERIOD_START, 3_000_000L);
        // 累加
        budgetUsageService.addUsage(1L, PERIOD_START, 2_000_000L);

        AiBudgetUsageDO usage = budgetUsageMapper.selectByUserAndPeriod(1L, PERIOD_START);
        assertNotNull(usage);
        assertEquals(5_000_000L, usage.getUsedAmount());
    }

    @Test
    public void testAddUsage_multipleUsers() {
        budgetUsageService.addUsage(1L, PERIOD_START, 3_000_000L);
        budgetUsageService.addUsage(2L, PERIOD_START, 7_000_000L);
        budgetUsageService.addUsage(0L, PERIOD_START, 10_000_000L); // 租户级

        assertEquals(3_000_000L, budgetUsageMapper.selectByUserAndPeriod(1L, PERIOD_START).getUsedAmount());
        assertEquals(7_000_000L, budgetUsageMapper.selectByUserAndPeriod(2L, PERIOD_START).getUsedAmount());
        assertEquals(10_000_000L, budgetUsageMapper.selectByUserAndPeriod(0L, PERIOD_START).getUsedAmount());
    }

    @Test
    public void testGetUsage_notExists() {
        assertNull(budgetUsageService.getUsage(999L, PERIOD_START));
    }

    @Test
    public void testAddUsage_differentPeriods() {
        LocalDateTime feb = LocalDateTime.of(2026, 2, 1, 0, 0, 0);
        LocalDateTime mar = LocalDateTime.of(2026, 3, 1, 0, 0, 0);

        budgetUsageService.addUsage(1L, feb, 5_000_000L);
        budgetUsageService.addUsage(1L, mar, 8_000_000L);

        assertEquals(5_000_000L, budgetUsageMapper.selectByUserAndPeriod(1L, feb).getUsedAmount());
        assertEquals(8_000_000L, budgetUsageMapper.selectByUserAndPeriod(1L, mar).getUsedAmount());
    }

    // ========== version 字段 ==========

    @Test
    public void testAddUsage_versionInitialized() {
        budgetUsageService.addUsage(1L, PERIOD_START, 1_000_000L);

        AiBudgetUsageDO usage = budgetUsageMapper.selectByUserAndPeriod(1L, PERIOD_START);
        assertEquals(0, usage.getVersion());
    }

    // ========== getUsage ==========

    @Test
    public void testGetUsage_exists() {
        budgetUsageService.addUsage(1L, PERIOD_START, 5_000_000L);

        AiBudgetUsageDO usage = budgetUsageService.getUsage(1L, PERIOD_START);
        assertNotNull(usage);
        assertEquals(5_000_000L, usage.getUsedAmount());
    }

    // ========== 多次累加 ==========

    @Test
    public void testAddUsage_multipleAccumulations() {
        budgetUsageService.addUsage(1L, PERIOD_START, 1_000_000L);
        budgetUsageService.addUsage(1L, PERIOD_START, 2_000_000L);
        budgetUsageService.addUsage(1L, PERIOD_START, 3_000_000L);

        AiBudgetUsageDO usage = budgetUsageMapper.selectByUserAndPeriod(1L, PERIOD_START);
        assertEquals(6_000_000L, usage.getUsedAmount());
    }

    // ========== DAILY 周期 ==========

    @Test
    public void testAddUsage_dailyPeriods() {
        LocalDateTime day1 = LocalDateTime.of(2026, 2, 25, 0, 0, 0);
        LocalDateTime day2 = LocalDateTime.of(2026, 2, 26, 0, 0, 0);

        budgetUsageService.addUsage(1L, day1, 3_000_000L);
        budgetUsageService.addUsage(1L, day2, 5_000_000L);
        budgetUsageService.addUsage(1L, day1, 1_000_000L); // 累加到 day1

        assertEquals(4_000_000L, budgetUsageMapper.selectByUserAndPeriod(1L, day1).getUsedAmount());
        assertEquals(5_000_000L, budgetUsageMapper.selectByUserAndPeriod(1L, day2).getUsedAmount());
    }

}

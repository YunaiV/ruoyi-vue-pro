package cn.iocoder.yudao.module.ai.service.billing;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.budget.AiBudgetLogPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiBudgetLogDO;
import cn.iocoder.yudao.module.ai.dal.mysql.billing.AiBudgetLogMapper;
import cn.iocoder.yudao.module.ai.enums.billing.AiBudgetEventTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link AiBudgetLogServiceImpl} 的单元测试
 */
@Import(AiBudgetLogServiceImpl.class)
public class AiBudgetLogServiceImplTest extends BaseDbUnitTest {

    @Resource
    private AiBudgetLogServiceImpl budgetLogService;

    @Resource
    private AiBudgetLogMapper budgetLogMapper;

    // ========== createBudgetLog ==========

    @Test
    public void testCreateBudgetLog_thresholdAlert() {
        AiBudgetLogDO logDO = AiBudgetLogDO.builder()
                .userId(1L)
                .eventType(AiBudgetEventTypeEnum.THRESHOLD_ALERT.getType())
                .periodStartTime(LocalDateTime.of(2026, 2, 1, 0, 0, 0))
                .currency("CNY")
                .budgetAmount(100_000_000L)
                .usedAmount(80_000_000L)
                .deltaAmount(5_000_000L)
                .message("预算使用达到80%阈值（已用80000000/预算100000000微元）")
                .build();

        budgetLogService.createBudgetLog(logDO);

        assertNotNull(logDO.getId());
        AiBudgetLogDO dbLog = budgetLogMapper.selectById(logDO.getId());
        assertEquals(1L, dbLog.getUserId());
        assertEquals("THRESHOLD_ALERT", dbLog.getEventType());
        assertEquals(100_000_000L, dbLog.getBudgetAmount());
        assertEquals(80_000_000L, dbLog.getUsedAmount());
        assertEquals(5_000_000L, dbLog.getDeltaAmount());
    }

    @Test
    public void testCreateBudgetLog_overLimitBlock() {
        AiBudgetLogDO logDO = AiBudgetLogDO.builder()
                .userId(0L)
                .eventType(AiBudgetEventTypeEnum.OVER_LIMIT_BLOCK.getType())
                .periodStartTime(LocalDateTime.of(2026, 2, 1, 0, 0, 0))
                .currency("CNY")
                .budgetAmount(50_000_000L)
                .usedAmount(50_000_000L)
                .message("预算超限拦截（已用50000000/预算50000000微元）")
                .build();

        budgetLogService.createBudgetLog(logDO);

        AiBudgetLogDO dbLog = budgetLogMapper.selectById(logDO.getId());
        assertEquals(0L, dbLog.getUserId());
        assertEquals("OVER_LIMIT_BLOCK", dbLog.getEventType());
        assertNull(dbLog.getDeltaAmount());
    }

    // ========== getBudgetLogPage ==========

    @Test
    public void testGetBudgetLogPage_all() {
        // 插入 3 条日志
        budgetLogMapper.insert(AiBudgetLogDO.builder()
                .userId(1L).eventType("THRESHOLD_ALERT")
                .periodStartTime(LocalDateTime.of(2026, 2, 1, 0, 0, 0))
                .currency("CNY").budgetAmount(100_000_000L).usedAmount(80_000_000L)
                .message("80%").build());
        budgetLogMapper.insert(AiBudgetLogDO.builder()
                .userId(1L).eventType("OVER_LIMIT_BLOCK")
                .periodStartTime(LocalDateTime.of(2026, 2, 1, 0, 0, 0))
                .currency("CNY").budgetAmount(100_000_000L).usedAmount(100_000_000L)
                .message("超限").build());
        budgetLogMapper.insert(AiBudgetLogDO.builder()
                .userId(0L).eventType("THRESHOLD_ALERT")
                .periodStartTime(LocalDateTime.of(2026, 2, 1, 0, 0, 0))
                .currency("CNY").budgetAmount(500_000_000L).usedAmount(400_000_000L)
                .message("租户80%").build());

        AiBudgetLogPageReqVO reqVO = new AiBudgetLogPageReqVO();
        PageResult<AiBudgetLogDO> page = budgetLogService.getBudgetLogPage(reqVO);

        assertEquals(3, page.getTotal());
    }

    @Test
    public void testGetBudgetLogPage_filterByUserId() {
        budgetLogMapper.insert(AiBudgetLogDO.builder()
                .userId(1L).eventType("THRESHOLD_ALERT")
                .periodStartTime(LocalDateTime.of(2026, 2, 1, 0, 0, 0))
                .currency("CNY").budgetAmount(100_000_000L).usedAmount(80_000_000L)
                .message("用户1").build());
        budgetLogMapper.insert(AiBudgetLogDO.builder()
                .userId(0L).eventType("THRESHOLD_ALERT")
                .periodStartTime(LocalDateTime.of(2026, 2, 1, 0, 0, 0))
                .currency("CNY").budgetAmount(500_000_000L).usedAmount(400_000_000L)
                .message("租户").build());

        AiBudgetLogPageReqVO reqVO = new AiBudgetLogPageReqVO();
        reqVO.setUserId(1L);
        PageResult<AiBudgetLogDO> page = budgetLogService.getBudgetLogPage(reqVO);

        assertEquals(1, page.getTotal());
        assertEquals(1L, page.getList().get(0).getUserId());
    }

    @Test
    public void testGetBudgetLogPage_filterByEventType() {
        budgetLogMapper.insert(AiBudgetLogDO.builder()
                .userId(1L).eventType("THRESHOLD_ALERT")
                .periodStartTime(LocalDateTime.of(2026, 2, 1, 0, 0, 0))
                .currency("CNY").budgetAmount(100_000_000L).usedAmount(80_000_000L)
                .message("告警").build());
        budgetLogMapper.insert(AiBudgetLogDO.builder()
                .userId(1L).eventType("OVER_LIMIT_BLOCK")
                .periodStartTime(LocalDateTime.of(2026, 2, 1, 0, 0, 0))
                .currency("CNY").budgetAmount(100_000_000L).usedAmount(100_000_000L)
                .message("拦截").build());

        AiBudgetLogPageReqVO reqVO = new AiBudgetLogPageReqVO();
        reqVO.setEventType("OVER_LIMIT_BLOCK");
        PageResult<AiBudgetLogDO> page = budgetLogService.getBudgetLogPage(reqVO);

        assertEquals(1, page.getTotal());
        assertEquals("OVER_LIMIT_BLOCK", page.getList().get(0).getEventType());
    }

    @Test
    public void testGetBudgetLogPage_empty() {
        AiBudgetLogPageReqVO reqVO = new AiBudgetLogPageReqVO();
        PageResult<AiBudgetLogDO> page = budgetLogService.getBudgetLogPage(reqVO);

        assertEquals(0, page.getTotal());
        assertTrue(page.getList().isEmpty());
    }

}

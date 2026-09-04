package cn.iocoder.yudao.module.ai.service.billing;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.budget.AiBudgetConfigPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.budget.AiBudgetConfigSaveReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiBudgetConfigDO;
import cn.iocoder.yudao.module.ai.dal.mysql.billing.AiBudgetConfigMapper;
import cn.iocoder.yudao.module.ai.enums.billing.AiBudgetPeriodTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.BUDGET_CONFIG_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link AiBudgetConfigServiceImpl} 的单元测试
 */
@Import(AiBudgetConfigServiceImpl.class)
public class AiBudgetConfigServiceImplTest extends BaseDbUnitTest {

    @Resource
    private AiBudgetConfigServiceImpl budgetConfigService;

    @Resource
    private AiBudgetConfigMapper budgetConfigMapper;

    @Test
    public void testCreateBudgetConfig() {
        AiBudgetConfigSaveReqVO reqVO = new AiBudgetConfigSaveReqVO();
        reqVO.setUserId(0L);
        reqVO.setPeriodType(AiBudgetPeriodTypeEnum.MONTHLY.getType());
        reqVO.setBudgetAmountYuan(100.0); // 100 元
        reqVO.setAlertThresholds("[80,90,100]");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());

        Long id = budgetConfigService.createBudgetConfig(reqVO);

        assertNotNull(id);
        AiBudgetConfigDO config = budgetConfigMapper.selectById(id);
        assertEquals(0L, config.getUserId());
        assertEquals("MONTHLY", config.getPeriodType());
        assertEquals("CNY", config.getCurrency());
        assertEquals(100_000_000L, config.getBudgetAmount()); // 100元 = 100,000,000微元
        assertEquals("[80,90,100]", config.getAlertThresholds());
    }

    @Test
    public void testUpdateBudgetConfig() {
        // 先创建
        AiBudgetConfigDO config = AiBudgetConfigDO.builder()
                .userId(0L).periodType("MONTHLY").currency("CNY")
                .budgetAmount(50_000_000L).status(CommonStatusEnum.ENABLE.getStatus())
                .build();
        budgetConfigMapper.insert(config);

        // 更新
        AiBudgetConfigSaveReqVO updateVO = new AiBudgetConfigSaveReqVO();
        updateVO.setId(config.getId());
        updateVO.setUserId(0L);
        updateVO.setPeriodType("MONTHLY");
        updateVO.setBudgetAmountYuan(200.0);
        updateVO.setStatus(CommonStatusEnum.ENABLE.getStatus());

        budgetConfigService.updateBudgetConfig(updateVO);

        AiBudgetConfigDO updated = budgetConfigMapper.selectById(config.getId());
        assertEquals(200_000_000L, updated.getBudgetAmount());
    }

    @Test
    public void testDeleteBudgetConfig() {
        AiBudgetConfigDO config = AiBudgetConfigDO.builder()
                .userId(1L).periodType("MONTHLY").currency("CNY")
                .budgetAmount(10_000_000L).status(CommonStatusEnum.ENABLE.getStatus())
                .build();
        budgetConfigMapper.insert(config);

        budgetConfigService.deleteBudgetConfig(config.getId());

        assertNull(budgetConfigMapper.selectById(config.getId()));
    }

    @Test
    public void testDeleteBudgetConfig_notExists() {
        assertServiceException(() -> budgetConfigService.deleteBudgetConfig(999L),
                BUDGET_CONFIG_NOT_EXISTS);
    }

    @Test
    public void testGetBudgetConfig_byUserAndPeriod() {
        AiBudgetConfigDO config = AiBudgetConfigDO.builder()
                .userId(0L).periodType("MONTHLY").currency("CNY")
                .budgetAmount(100_000_000L).status(CommonStatusEnum.ENABLE.getStatus())
                .build();
        budgetConfigMapper.insert(config);

        AiBudgetConfigDO result = budgetConfigService.getBudgetConfig(0L, "MONTHLY");
        assertNotNull(result);
        assertEquals(100_000_000L, result.getBudgetAmount());

        // 不存在的查询
        assertNull(budgetConfigService.getBudgetConfig(999L, "MONTHLY"));
    }

    // ========== getBudgetConfig(id) ==========

    @Test
    public void testGetBudgetConfig_byId() {
        AiBudgetConfigDO config = AiBudgetConfigDO.builder()
                .userId(1L).periodType("DAILY").currency("CNY")
                .budgetAmount(5_000_000L).status(CommonStatusEnum.ENABLE.getStatus())
                .build();
        budgetConfigMapper.insert(config);

        AiBudgetConfigDO result = budgetConfigService.getBudgetConfig(config.getId());
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("DAILY", result.getPeriodType());
    }

    @Test
    public void testGetBudgetConfig_byId_notExists() {
        assertNull(budgetConfigService.getBudgetConfig(999L));
    }

    // ========== getBudgetConfigPage ==========

    @Test
    public void testGetBudgetConfigPage_all() {
        budgetConfigMapper.insert(AiBudgetConfigDO.builder()
                .userId(0L).periodType("MONTHLY").currency("CNY")
                .budgetAmount(100_000_000L).status(CommonStatusEnum.ENABLE.getStatus()).build());
        budgetConfigMapper.insert(AiBudgetConfigDO.builder()
                .userId(1L).periodType("DAILY").currency("CNY")
                .budgetAmount(5_000_000L).status(CommonStatusEnum.ENABLE.getStatus()).build());
        budgetConfigMapper.insert(AiBudgetConfigDO.builder()
                .userId(2L).periodType("MONTHLY").currency("CNY")
                .budgetAmount(50_000_000L).status(CommonStatusEnum.DISABLE.getStatus()).build());

        AiBudgetConfigPageReqVO reqVO = new AiBudgetConfigPageReqVO();
        PageResult<AiBudgetConfigDO> page = budgetConfigService.getBudgetConfigPage(reqVO);

        assertEquals(3, page.getTotal());
    }

    @Test
    public void testGetBudgetConfigPage_filterByUserId() {
        budgetConfigMapper.insert(AiBudgetConfigDO.builder()
                .userId(0L).periodType("MONTHLY").currency("CNY")
                .budgetAmount(100_000_000L).status(CommonStatusEnum.ENABLE.getStatus()).build());
        budgetConfigMapper.insert(AiBudgetConfigDO.builder()
                .userId(1L).periodType("DAILY").currency("CNY")
                .budgetAmount(5_000_000L).status(CommonStatusEnum.ENABLE.getStatus()).build());

        AiBudgetConfigPageReqVO reqVO = new AiBudgetConfigPageReqVO();
        reqVO.setUserId(1L);
        PageResult<AiBudgetConfigDO> page = budgetConfigService.getBudgetConfigPage(reqVO);

        assertEquals(1, page.getTotal());
        assertEquals(1L, page.getList().get(0).getUserId());
    }

    @Test
    public void testGetBudgetConfigPage_filterByPeriodType() {
        budgetConfigMapper.insert(AiBudgetConfigDO.builder()
                .userId(0L).periodType("MONTHLY").currency("CNY")
                .budgetAmount(100_000_000L).status(CommonStatusEnum.ENABLE.getStatus()).build());
        budgetConfigMapper.insert(AiBudgetConfigDO.builder()
                .userId(1L).periodType("DAILY").currency("CNY")
                .budgetAmount(5_000_000L).status(CommonStatusEnum.ENABLE.getStatus()).build());

        AiBudgetConfigPageReqVO reqVO = new AiBudgetConfigPageReqVO();
        reqVO.setPeriodType("DAILY");
        PageResult<AiBudgetConfigDO> page = budgetConfigService.getBudgetConfigPage(reqVO);

        assertEquals(1, page.getTotal());
        assertEquals("DAILY", page.getList().get(0).getPeriodType());
    }

    @Test
    public void testGetBudgetConfigPage_filterByStatus() {
        budgetConfigMapper.insert(AiBudgetConfigDO.builder()
                .userId(0L).periodType("MONTHLY").currency("CNY")
                .budgetAmount(100_000_000L).status(CommonStatusEnum.ENABLE.getStatus()).build());
        budgetConfigMapper.insert(AiBudgetConfigDO.builder()
                .userId(1L).periodType("MONTHLY").currency("CNY")
                .budgetAmount(50_000_000L).status(CommonStatusEnum.DISABLE.getStatus()).build());

        AiBudgetConfigPageReqVO reqVO = new AiBudgetConfigPageReqVO();
        reqVO.setStatus(CommonStatusEnum.DISABLE.getStatus());
        PageResult<AiBudgetConfigDO> page = budgetConfigService.getBudgetConfigPage(reqVO);

        assertEquals(1, page.getTotal());
        assertEquals(CommonStatusEnum.DISABLE.getStatus(), page.getList().get(0).getStatus());
    }

    // ========== yuanToMicro 边界 ==========

    @Test
    public void testCreateBudgetConfig_nullBudgetAmount() {
        AiBudgetConfigSaveReqVO reqVO = new AiBudgetConfigSaveReqVO();
        reqVO.setUserId(0L);
        reqVO.setPeriodType("MONTHLY");
        reqVO.setBudgetAmountYuan(null); // null → 0
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());

        Long id = budgetConfigService.createBudgetConfig(reqVO);

        AiBudgetConfigDO config = budgetConfigMapper.selectById(id);
        assertEquals(0L, config.getBudgetAmount());
    }

}

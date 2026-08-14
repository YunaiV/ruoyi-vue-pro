package cn.iocoder.yudao.module.hrm.service.salary.config;

import cn.iocoder.yudao.module.hrm.service.salary.monthrecord.HrmSalaryMonthRecordService;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.config.HrmSalaryConfigCreateReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.config.HrmSalaryConfigUpdateReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryConfigDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.salary.config.HrmSalaryConfigMapper;
import cn.iocoder.yudao.module.hrm.enums.salary.config.HrmSalarySocialSecurityMonthTypeEnum;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_CONFIG_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_CONFIG_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * {@link HrmSalaryConfigServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmSalaryConfigServiceImpl.class)
public class HrmSalaryConfigServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmSalaryConfigServiceImpl salaryConfigService;

    @Resource
    private HrmSalaryConfigMapper salaryConfigMapper;

    @MockBean
    private HrmSalaryMonthRecordService salaryMonthRecordService;

    @Test
    public void testCreateSalaryConfig_success() {
        // 准备参数
        HrmSalaryConfigCreateReqVO createReqVO = randomPojo(HrmSalaryConfigCreateReqVO.class, o -> {
            o.setCycleStartDay(26);
            o.setSocialSecurityMonthType(HrmSalarySocialSecurityMonthTypeEnum.CURRENT_MONTH.getType());
            o.setStartYear(2026);
            o.setStartMonth(7);
        });

        // 调用
        Long id = salaryConfigService.createSalaryConfig(createReqVO);

        // 断言
        assertNotNull(id);
        HrmSalaryConfigDO config = salaryConfigMapper.selectById(id);
        assertPojoEquals(createReqVO, config, "id", "cycleEndDay");
        assertEquals(25, config.getCycleEndDay());
        assertEquals(1L, salaryConfigMapper.selectCount());
        verify(salaryMonthRecordService).createMonthRecord(argThat(reqVO ->
                reqVO.getYear() == 2026 && reqVO.getMonth() == 7));
    }

    @Test
    public void testCreateSalaryConfig_exists() {
        // mock 数据
        salaryConfigMapper.insert(randomSalaryConfigDO());
        // 准备参数
        HrmSalaryConfigCreateReqVO reqVO = randomPojo(HrmSalaryConfigCreateReqVO.class);

        // 调用，并断言异常
        assertServiceException(() -> salaryConfigService.createSalaryConfig(reqVO), SALARY_CONFIG_EXISTS);
        verifyNoInteractions(salaryMonthRecordService);
    }

    @Test
    public void testUpdateSalaryConfig_success() {
        // mock 数据
        HrmSalaryConfigDO dbConfig = randomSalaryConfigDO();
        salaryConfigMapper.insert(dbConfig);
        // 准备参数
        HrmSalaryConfigUpdateReqVO updateReqVO = new HrmSalaryConfigUpdateReqVO()
                .setSocialSecurityMonthType(HrmSalarySocialSecurityMonthTypeEnum.NEXT_MONTH.getType());

        // 调用
        salaryConfigService.updateSalaryConfig(updateReqVO);

        // 断言
        HrmSalaryConfigDO updatedConfig = salaryConfigMapper.selectById(dbConfig.getId());
        assertEquals(HrmSalarySocialSecurityMonthTypeEnum.NEXT_MONTH.getType(), updatedConfig.getSocialSecurityMonthType());
        assertEquals(dbConfig.getCycleStartDay(), updatedConfig.getCycleStartDay());
        assertEquals(dbConfig.getCycleEndDay(), updatedConfig.getCycleEndDay());
        assertEquals(dbConfig.getStartYear(), updatedConfig.getStartYear());
        assertEquals(dbConfig.getStartMonth(), updatedConfig.getStartMonth());
        assertEquals(1L, salaryConfigMapper.selectCount());
        verifyNoInteractions(salaryMonthRecordService);
    }

    @Test
    public void testUpdateSalaryConfig_notExists() {
        // 准备参数
        HrmSalaryConfigUpdateReqVO reqVO = randomPojo(HrmSalaryConfigUpdateReqVO.class);

        // 调用，并断言异常
        assertServiceException(() -> salaryConfigService.updateSalaryConfig(reqVO), SALARY_CONFIG_NOT_EXISTS);
    }

    // ========== 随机对象 ==========

    private HrmSalaryConfigDO randomSalaryConfigDO() {
        return randomPojo(HrmSalaryConfigDO.class, o -> {
            o.setCycleStartDay(1).setCycleEndDay(31);
            o.setSocialSecurityMonthType(HrmSalarySocialSecurityMonthTypeEnum.CURRENT_MONTH.getType()).setStartYear(2026).setStartMonth(7);
        });
    }

}

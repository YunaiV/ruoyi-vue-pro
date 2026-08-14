package cn.iocoder.yudao.module.hrm.service.employee.employment;

import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.salarycard.HrmEmployeeSalaryCardSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.employment.HrmEmployeeSalaryCardDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.employee.employment.HrmEmployeeSalaryCardMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.Map;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_RESOURCE_BELONG_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_SALARY_CARD_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link HrmEmployeeSalaryCardServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmEmployeeSalaryCardServiceImpl.class)
public class HrmEmployeeSalaryCardServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmEmployeeSalaryCardServiceImpl salaryCardService;

    @Resource
    private HrmEmployeeSalaryCardMapper salaryCardMapper;

    @MockitoBean
    private HrmEmployeeService employeeService;

    @Test
    public void testSaveSalaryCard_create() {
        // 准备参数
        Long employeeId = randomLongId();
        HrmEmployeeDO employee = randomPojo(HrmEmployeeDO.class, o -> o.setId(employeeId));
        HrmEmployeeSalaryCardSaveReqVO reqVO = randomPojo(HrmEmployeeSalaryCardSaveReqVO.class,
                o -> o.setId(null).setEmployeeId(employeeId));
        // mock 方法
        when(employeeService.validateEmployeeExists(employeeId)).thenReturn(employee);

        // 调用
        Long id = salaryCardService.saveSalaryCard(reqVO);

        // 断言
        assertNotNull(id);
        assertPojoEquals(reqVO, salaryCardMapper.selectById(id), "id");
        verify(employeeService).validateEmployeeExists(employeeId);
    }

    @Test
    public void testSaveSalaryCard_updateByEmployeeId() {
        // mock 数据
        Long employeeId = randomLongId();
        HrmEmployeeSalaryCardDO dbSalaryCard = randomSalaryCardDO(employeeId);
        salaryCardMapper.insert(dbSalaryCard);
        // 准备参数
        HrmEmployeeSalaryCardSaveReqVO reqVO = randomPojo(HrmEmployeeSalaryCardSaveReqVO.class,
                o -> o.setId(null).setEmployeeId(employeeId));
        // mock 方法
        when(employeeService.validateEmployeeExists(employeeId))
                .thenReturn(randomPojo(HrmEmployeeDO.class, o -> o.setId(employeeId)));

        // 调用
        Long id = salaryCardService.saveSalaryCard(reqVO);

        // 断言
        assertEquals(dbSalaryCard.getId(), id);
        assertPojoEquals(reqVO, salaryCardMapper.selectById(id), "id");
        assertEquals(1L, salaryCardMapper.selectCount());
    }

    @Test
    public void testSaveSalaryCard_updateLatestWhenHistoryDuplicated() {
        // mock 数据
        Long employeeId = randomLongId();
        HrmEmployeeSalaryCardDO oldSalaryCard = randomSalaryCardDO(employeeId);
        salaryCardMapper.insert(oldSalaryCard);
        HrmEmployeeSalaryCardDO latestSalaryCard = randomSalaryCardDO(employeeId);
        salaryCardMapper.insert(latestSalaryCard);
        // 准备参数
        HrmEmployeeSalaryCardSaveReqVO reqVO = randomPojo(HrmEmployeeSalaryCardSaveReqVO.class,
                o -> o.setId(null).setEmployeeId(employeeId));
        // mock 方法
        when(employeeService.validateEmployeeExists(employeeId))
                .thenReturn(randomPojo(HrmEmployeeDO.class, o -> o.setId(employeeId)));

        // 调用
        Long id = salaryCardService.saveSalaryCard(reqVO);

        // 断言
        assertEquals(latestSalaryCard.getId(), id);
        assertPojoEquals(reqVO, salaryCardMapper.selectById(id), "id");
        assertEquals(2L, salaryCardMapper.selectCount());
    }

    @Test
    public void testSaveSalaryCard_employeeMismatch() {
        // mock 数据
        HrmEmployeeSalaryCardDO dbSalaryCard = randomSalaryCardDO(randomLongId());
        salaryCardMapper.insert(dbSalaryCard);
        // 准备参数
        Long employeeId = randomLongId();
        HrmEmployeeSalaryCardSaveReqVO reqVO = randomPojo(HrmEmployeeSalaryCardSaveReqVO.class,
                o -> o.setId(dbSalaryCard.getId()).setEmployeeId(employeeId));
        // mock 方法
        when(employeeService.validateEmployeeExists(employeeId))
                .thenReturn(randomPojo(HrmEmployeeDO.class, o -> o.setId(employeeId)));

        // 调用，并断言异常
        assertServiceException(() -> salaryCardService.saveSalaryCard(reqVO),
                EMPLOYEE_RESOURCE_BELONG_INVALID, "工资卡");
    }

    @Test
    public void testValidateSalaryCardExists_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用，并断言异常
        assertServiceException(() -> salaryCardService.validateSalaryCardExists(id),
                EMPLOYEE_SALARY_CARD_NOT_EXISTS);
    }

    @Test
    public void testDeleteSalaryCardByEmployeeId_success() {
        // mock 数据
        Long employeeId = randomLongId();
        HrmEmployeeSalaryCardDO salaryCard = randomSalaryCardDO(employeeId);
        salaryCardMapper.insert(salaryCard);
        // mock 方法
        when(employeeService.validateEmployeeExists(employeeId))
                .thenReturn(randomPojo(HrmEmployeeDO.class, o -> o.setId(employeeId)));

        // 调用
        salaryCardService.deleteSalaryCardByEmployeeId(employeeId);

        // 断言
        assertEquals(0L, salaryCardMapper.selectCount());
        verify(employeeService).validateEmployeeExists(employeeId);
    }

    @Test
    public void testDeleteSalaryCardByEmployeeId_notExists() {
        // 准备参数
        Long employeeId = randomLongId();
        // mock 方法
        when(employeeService.validateEmployeeExists(employeeId))
                .thenReturn(randomPojo(HrmEmployeeDO.class, o -> o.setId(employeeId)));

        // 调用，并断言异常
        assertServiceException(() -> salaryCardService.deleteSalaryCardByEmployeeId(employeeId),
                EMPLOYEE_SALARY_CARD_NOT_EXISTS);
    }

    @Test
    public void testGetSalaryCardMap() {
        // mock 数据
        HrmEmployeeSalaryCardDO firstSalaryCard = randomSalaryCardDO(randomLongId());
        salaryCardMapper.insert(firstSalaryCard);
        HrmEmployeeSalaryCardDO secondSalaryCard = randomSalaryCardDO(randomLongId());
        salaryCardMapper.insert(secondSalaryCard);
        salaryCardMapper.insert(randomSalaryCardDO(randomLongId()));

        // 调用
        Map<Long, HrmEmployeeSalaryCardDO> result = salaryCardService.getSalaryCardMap(
                java.util.Arrays.asList(firstSalaryCard.getEmployeeId(), secondSalaryCard.getEmployeeId()));

        // 断言
        assertEquals(2, result.size());
        assertEquals(firstSalaryCard.getId(), result.get(firstSalaryCard.getEmployeeId()).getId());
        assertEquals(secondSalaryCard.getId(), result.get(secondSalaryCard.getEmployeeId()).getId());
        assertTrue(salaryCardService.getSalaryCardMap(Collections.emptyList()).isEmpty());
    }

    @Test
    public void testGetSalaryCardMap_historyDuplicated() {
        // mock 数据
        Long employeeId = randomLongId();
        HrmEmployeeSalaryCardDO oldSalaryCard = randomSalaryCardDO(employeeId);
        salaryCardMapper.insert(oldSalaryCard);
        HrmEmployeeSalaryCardDO latestSalaryCard = randomSalaryCardDO(employeeId);
        salaryCardMapper.insert(latestSalaryCard);

        // 调用
        Map<Long, HrmEmployeeSalaryCardDO> result = salaryCardService.getSalaryCardMap(
                Collections.singletonList(employeeId));

        // 断言
        assertEquals(1, result.size());
        assertEquals(latestSalaryCard.getId(), result.get(employeeId).getId());
    }

    private static HrmEmployeeSalaryCardDO randomSalaryCardDO(Long employeeId) {
        return randomPojo(HrmEmployeeSalaryCardDO.class,
                o -> o.setId(null).setEmployeeId(employeeId).setDeleted(false));
    }

}

package cn.iocoder.yudao.module.hrm.service.employee.employment;

import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.contract.HrmEmployeeContractSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.employment.HrmEmployeeContractDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.employee.employment.HrmEmployeeContractMapper;
import cn.iocoder.yudao.module.hrm.dal.mysql.employee.info.HrmEmployeeMapper;
import cn.iocoder.yudao.module.hrm.enums.employee.employment.HrmEmployeeContractStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeEntryStatusEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_CONTRACT_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;

/**
 * {@link HrmEmployeeContractServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmEmployeeContractServiceImpl.class)
public class HrmEmployeeContractServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmEmployeeContractServiceImpl contractService;

    @Resource
    private HrmEmployeeContractMapper contractMapper;
    @Resource
    private HrmEmployeeMapper employeeMapper;

    @MockitoBean
    private HrmEmployeeService employeeService;

    @Test
    public void testCreateContract_success() {
        // 准备参数
        Long employeeId = randomLongId();
        HrmEmployeeContractSaveReqVO reqVO = randomPojo(
                HrmEmployeeContractSaveReqVO.class,
                o -> {
                    o.setId(null).setEmployeeId(employeeId).setSort(10);
                    o.setType(1).setStatus(1).setExpireRemind(true);
                    o.setStartTime(LocalDateTime.of(2026, 1, 1, 9, 0))
                            .setEndTime(LocalDateTime.of(2027, 1, 1, 18, 0))
                            .setSignTime(LocalDateTime.of(2025, 12, 20, 15, 30));
                });

        // 调用
        Long id = contractService.createContract(reqVO);

        // 断言
        assertNotNull(id);
        HrmEmployeeContractDO contract = contractMapper.selectById(id);
        assertPojoEquals(reqVO, contract, "id", "startTime", "endTime", "signTime");
        assertEquals(reqVO.getStartTime().toLocalDate().atStartOfDay(), contract.getStartTime());
        assertEquals(reqVO.getEndTime().toLocalDate().atStartOfDay(), contract.getEndTime());
        assertEquals(reqVO.getSignTime().toLocalDate().atStartOfDay(), contract.getSignTime());
        verify(employeeService).validateEmployeeExists(employeeId);
    }

    @Test
    public void testUpdateContract_success() {
        // mock 数据
        HrmEmployeeContractDO dbRecord = randomContractDO(randomLongId(), 20);
        contractMapper.insert(dbRecord);
        // 准备参数
        HrmEmployeeContractSaveReqVO reqVO = randomPojo(
                HrmEmployeeContractSaveReqVO.class,
                o -> {
                    o.setId(dbRecord.getId()).setEmployeeId(dbRecord.getEmployeeId()).setSort(10);
                    o.setType(2).setStatus(2).setExpireRemind(false);
                    o.setStartTime(LocalDateTime.of(2026, 1, 1, 9, 0))
                            .setEndTime(LocalDateTime.of(2027, 1, 1, 18, 0))
                            .setSignTime(LocalDateTime.of(2025, 12, 25, 18, 0));
                });

        // 调用
        contractService.updateContract(reqVO);

        // 断言
        HrmEmployeeContractDO contract = contractMapper.selectById(dbRecord.getId());
        assertPojoEquals(reqVO, contract, "employeeId", "term", "startTime", "endTime", "signTime");
        assertEquals(dbRecord.getEmployeeId(), contract.getEmployeeId());
        assertNull(contract.getTerm());
        assertEquals(reqVO.getStartTime().toLocalDate().atStartOfDay(), contract.getStartTime());
        assertEquals(reqVO.getEndTime().toLocalDate().atStartOfDay(), contract.getEndTime());
        assertEquals(reqVO.getSignTime().toLocalDate().atStartOfDay(), contract.getSignTime());
    }

    @Test
    public void testDeleteContract_success() {
        // mock 数据
        HrmEmployeeContractDO dbRecord = randomContractDO(randomLongId(), 20);
        contractMapper.insert(dbRecord);
        // 准备参数
        Long id = dbRecord.getId();

        // 调用
        contractService.deleteContract(id);

        // 断言
        assertNull(contractMapper.selectById(id));
    }

    @Test
    public void testDeleteContract_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用，并断言异常
        assertServiceException(() -> contractService.deleteContract(id), EMPLOYEE_CONTRACT_NOT_EXISTS);
    }

    @Test
    public void testGetContractListByEmployeeId() {
        // mock 数据
        Long employeeId = randomLongId();
        HrmEmployeeContractDO firstRecord = randomContractDO(employeeId, 20)
                .setEndTime(LocalDateTime.of(2026, 8, 5, 18, 0));
        contractMapper.insert(firstRecord);
        HrmEmployeeContractDO secondRecord = randomContractDO(employeeId, 10)
                .setEndTime(LocalDateTime.of(2026, 8, 20, 18, 0));
        contractMapper.insert(secondRecord);
        // 测试 employeeId 不匹配
        contractMapper.insert(randomContractDO(randomLongId(), 1));

        // 调用
        List<HrmEmployeeContractDO> result = contractService.getContractListByEmployeeId(employeeId);

        // 断言
        assertEquals(2, result.size());
        assertEquals(secondRecord.getId(), result.get(0).getId());
        assertEquals(firstRecord.getId(), result.get(1).getId());
    }

    @Test
    public void testGetExpireEmployeeCountInMonth() {
        // mock 数据
        LocalDate monthStartDate = LocalDate.of(2026, 8, 1);
        HrmEmployeeDO employee = new HrmEmployeeDO().setName("在职员工")
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus());
        employeeMapper.insert(employee);
        contractMapper.insert(randomContractDO(employee.getId(), 20)
                .setEndTime(monthStartDate.plusDays(5).atTime(18, 0)));
        contractMapper.insert(randomContractDO(employee.getId(), 10)
                .setEndTime(monthStartDate.plusDays(20).atTime(18, 0)));
        // 测试关闭提醒不计入
        HrmEmployeeDO remindDisabledEmployee = new HrmEmployeeDO().setName("关闭提醒员工")
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus());
        employeeMapper.insert(remindDisabledEmployee);
        contractMapper.insert(randomContractDO(remindDisabledEmployee.getId(), 1)
                .setExpireRemind(false)
                .setEndTime(monthStartDate.plusDays(8).atTime(18, 0)));
        // 测试已到期合同不计入
        HrmEmployeeDO expiredContractEmployee = new HrmEmployeeDO().setName("合同已到期员工")
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus());
        employeeMapper.insert(expiredContractEmployee);
        contractMapper.insert(randomContractDO(expiredContractEmployee.getId(), 1)
                .setStatus(HrmEmployeeContractStatusEnum.EXPIRED.getStatus())
                .setEndTime(monthStartDate.plusDays(9).atTime(18, 0)));
        // 测试离职员工不计入
        HrmEmployeeDO leftEmployee = new HrmEmployeeDO().setName("离职员工")
                .setEntryStatus(HrmEmployeeEntryStatusEnum.LEFT.getStatus());
        employeeMapper.insert(leftEmployee);
        contractMapper.insert(randomContractDO(leftEmployee.getId(), 1)
                .setEndTime(monthStartDate.plusDays(10).atTime(18, 0)));
        // 测试已删除员工不计入到期合同人数
        HrmEmployeeDO deletedEmployee = new HrmEmployeeDO().setName("已删除员工")
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus());
        employeeMapper.insert(deletedEmployee);
        contractMapper.insert(randomContractDO(deletedEmployee.getId(), 1)
                .setEndTime(monthStartDate.plusDays(10).atTime(18, 0)));
        employeeMapper.deleteById(deletedEmployee.getId());

        // 调用
        Long expireEmployeeCount = contractService.getExpireEmployeeCountInMonth(
                new LocalDateTime[]{monthStartDate.atStartOfDay(),
                        monthStartDate.plusMonths(1).atStartOfDay().minusNanos(1)});

        // 断言
        assertEquals(1L, expireEmployeeCount);
    }

    private static HrmEmployeeContractDO randomContractDO(Long employeeId, Integer sort) {
        return randomPojo(HrmEmployeeContractDO.class, o -> o.setId(null).setEmployeeId(employeeId)
                .setType(1).setStatus(1).setExpireRemind(true).setSort(sort).setDeleted(false));
    }

}

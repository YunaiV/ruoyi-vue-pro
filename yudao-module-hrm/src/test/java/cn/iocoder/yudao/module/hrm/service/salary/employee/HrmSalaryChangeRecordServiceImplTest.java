package cn.iocoder.yudao.module.hrm.service.salary.employee;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.employee.HrmSalaryChangeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.salary.employee.HrmSalaryChangeRecordMapper;
import cn.iocoder.yudao.module.hrm.enums.salary.employee.HrmSalaryChangeRecordStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.employee.HrmSalaryChangeRecordTypeEnum;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_CHANGE_RECORD_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_CHANGE_RECORD_STATUS_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link HrmSalaryChangeRecordServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmSalaryChangeRecordServiceImpl.class)
public class HrmSalaryChangeRecordServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmSalaryChangeRecordServiceImpl salaryChangeRecordService;
    @Resource
    private HrmSalaryChangeRecordMapper salaryChangeRecordMapper;

    @Test
    public void testCreateSalaryChangeRecord_success() {
        // 准备参数
        HrmSalaryChangeRecordDO changeRecord =
                createSalaryChangeRecord(1001L, HrmSalaryChangeRecordStatusEnum.PENDING.getStatus(), LocalDate.now().plusDays(1));

        // 调用
        Long id = salaryChangeRecordService.createSalaryChangeRecord(changeRecord);

        // 断言
        assertEquals(changeRecord.getId(), id);
        assertEquals(changeRecord.getRemark(), salaryChangeRecordMapper.selectById(id).getRemark());
    }

    @Test
    public void testUpdateSalaryChangeRecord_success() {
        // mock 数据
        HrmSalaryChangeRecordDO changeRecord =
                createSalaryChangeRecord(1001L, HrmSalaryChangeRecordStatusEnum.PENDING.getStatus(), LocalDate.now().plusDays(1));
        salaryChangeRecordMapper.insert(changeRecord);
        // 准备参数
        changeRecord.setRemark("调整后备注");

        // 调用
        salaryChangeRecordService.updateSalaryChangeRecord(changeRecord);

        // 断言
        assertEquals("调整后备注", salaryChangeRecordMapper.selectById(changeRecord.getId()).getRemark());
    }

    @Test
    public void testUpdateSalaryChangeRecordStatus_compareAndSet() {
        // mock 数据
        HrmSalaryChangeRecordDO changeRecord =
                createSalaryChangeRecord(1001L, HrmSalaryChangeRecordStatusEnum.PENDING.getStatus(),
                        LocalDate.now().plusDays(1));
        salaryChangeRecordMapper.insert(changeRecord);

        // 调用
        boolean firstUpdated = salaryChangeRecordService.updateSalaryChangeRecordStatus(
                changeRecord.getId(), HrmSalaryChangeRecordStatusEnum.PENDING.getStatus(),
                HrmSalaryChangeRecordStatusEnum.EFFECTIVE.getStatus());
        boolean repeatedUpdated = salaryChangeRecordService.updateSalaryChangeRecordStatus(
                changeRecord.getId(), HrmSalaryChangeRecordStatusEnum.PENDING.getStatus(),
                HrmSalaryChangeRecordStatusEnum.EFFECTIVE.getStatus());

        // 断言
        assertTrue(firstUpdated);
        assertFalse(repeatedUpdated);
        assertEquals(HrmSalaryChangeRecordStatusEnum.EFFECTIVE.getStatus(),
                salaryChangeRecordMapper.selectById(changeRecord.getId()).getStatus());
    }

    @Test
    public void testValidateSalaryChangeRecordExists_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用，并断言异常
        assertServiceException(() -> salaryChangeRecordService.validateSalaryChangeRecordExists(id),
                SALARY_CHANGE_RECORD_NOT_EXISTS);
    }

    @Test
    public void testHasPendingSalaryChangeRecord() {
        // mock 数据
        HrmSalaryChangeRecordDO pendingRecord =
                createSalaryChangeRecord(1001L, HrmSalaryChangeRecordStatusEnum.PENDING.getStatus(), LocalDate.now().plusDays(1));
        salaryChangeRecordMapper.insert(pendingRecord);

        // 调用，并断言
        assertTrue(salaryChangeRecordService.hasPendingSalaryChangeRecord(
                pendingRecord.getEmployeeId(), null));
        assertFalse(salaryChangeRecordService.hasPendingSalaryChangeRecord(
                pendingRecord.getEmployeeId(), pendingRecord.getId()));
    }

    @Test
    public void testGetSalarySetRecordByEmployeeId_duplicateReturnsLatest() {
        // mock 数据
        Long employeeId = randomLongId();
        HrmSalaryChangeRecordDO firstRecord = createSalaryChangeRecord(
                employeeId, HrmSalaryChangeRecordStatusEnum.EFFECTIVE.getStatus(), LocalDate.now().minusDays(1));
        firstRecord.setType(HrmSalaryChangeRecordTypeEnum.SALARY_SET.getType());
        salaryChangeRecordMapper.insert(firstRecord);
        HrmSalaryChangeRecordDO latestRecord = createSalaryChangeRecord(
                employeeId, HrmSalaryChangeRecordStatusEnum.EFFECTIVE.getStatus(), LocalDate.now());
        latestRecord.setType(HrmSalaryChangeRecordTypeEnum.SALARY_SET.getType());
        salaryChangeRecordMapper.insert(latestRecord);

        // 调用
        HrmSalaryChangeRecordDO result = salaryChangeRecordService.getSalarySetRecordByEmployeeId(employeeId);

        // 断言
        assertEquals(latestRecord.getId(), result.getId());
    }

    @Test
    public void testGetDueSalaryChangeCount() {
        // mock 数据
        HrmSalaryChangeRecordDO dueRecord =
                createSalaryChangeRecord(1001L, HrmSalaryChangeRecordStatusEnum.PENDING.getStatus(), LocalDate.now());
        HrmSalaryChangeRecordDO futureRecord =
                createSalaryChangeRecord(1002L, HrmSalaryChangeRecordStatusEnum.PENDING.getStatus(), LocalDate.now().plusDays(1));
        salaryChangeRecordMapper.insert(dueRecord);
        salaryChangeRecordMapper.insert(futureRecord);

        // 调用
        long count = salaryChangeRecordService.getDueSalaryChangeCount(LocalDate.now());

        // 断言
        assertEquals(1, count);
    }

    @Test
    public void testGetDueSalaryChangeRecordList() {
        // mock 数据
        HrmSalaryChangeRecordDO dueRecord =
                createSalaryChangeRecord(1001L, HrmSalaryChangeRecordStatusEnum.PENDING.getStatus(), LocalDate.now());
        salaryChangeRecordMapper.insert(dueRecord);
        salaryChangeRecordMapper.insert(
                createSalaryChangeRecord(1002L, HrmSalaryChangeRecordStatusEnum.PENDING.getStatus(), LocalDate.now().plusDays(1)));

        // 调用
        HrmSalaryChangeRecordDO result =
                salaryChangeRecordService.getDueSalaryChangeRecordList(LocalDate.now()).get(0);

        // 断言
        assertEquals(dueRecord.getId(),
                result.getId());
    }

    @Test
    public void testCancelChangeRecord_success() {
        // mock 数据
        HrmSalaryChangeRecordDO changeRecord =
                createSalaryChangeRecord(1001L, HrmSalaryChangeRecordStatusEnum.PENDING.getStatus(), LocalDate.now().plusDays(1));
        salaryChangeRecordMapper.insert(changeRecord);

        // 调用
        salaryChangeRecordService.cancelSalaryChangeRecord(changeRecord.getId());

        // 断言
        assertEquals(HrmSalaryChangeRecordStatusEnum.CANCELLED.getStatus(),
                salaryChangeRecordMapper.selectById(changeRecord.getId()).getStatus());
    }

    @Test
    public void testDeleteChangeRecord_success() {
        // mock 数据
        HrmSalaryChangeRecordDO changeRecord =
                createSalaryChangeRecord(1001L, HrmSalaryChangeRecordStatusEnum.CANCELLED.getStatus(), LocalDate.now().plusDays(1));
        salaryChangeRecordMapper.insert(changeRecord);

        // 调用
        salaryChangeRecordService.deleteSalaryChangeRecord(changeRecord.getId());

        // 断言
        assertNull(salaryChangeRecordMapper.selectById(changeRecord.getId()));
    }

    @Test
    public void testDeleteChangeRecord_effective() {
        // mock 数据
        HrmSalaryChangeRecordDO changeRecord =
                createSalaryChangeRecord(1001L, HrmSalaryChangeRecordStatusEnum.EFFECTIVE.getStatus(), LocalDate.now());
        salaryChangeRecordMapper.insert(changeRecord);

        // 调用，并断言异常
        assertServiceException(() -> salaryChangeRecordService.deleteSalaryChangeRecord(changeRecord.getId()),
                SALARY_CHANGE_RECORD_STATUS_INVALID);
    }

    @Test
    public void testCancelChangeRecord_effective() {
        // mock 数据
        HrmSalaryChangeRecordDO changeRecord =
                createSalaryChangeRecord(1001L, HrmSalaryChangeRecordStatusEnum.EFFECTIVE.getStatus(), LocalDate.now());
        salaryChangeRecordMapper.insert(changeRecord);

        // 调用，并断言异常
        assertServiceException(() -> salaryChangeRecordService.cancelSalaryChangeRecord(changeRecord.getId()),
                SALARY_CHANGE_RECORD_STATUS_INVALID);
    }

    // ========== 随机对象 ==========

    private static HrmSalaryChangeRecordDO createSalaryChangeRecord(
            Long employeeId, Integer status, LocalDate effectDate) {
        return HrmSalaryChangeRecordDO.builder()
                .employeeId(employeeId).type(HrmSalaryChangeRecordTypeEnum.SALARY_ADJUSTMENT.getType()).reason(3)
                .effectTime(effectDate.atStartOfDay())
                .beforeTotal(new BigDecimal("8000.00")).afterTotal(new BigDecimal("9000.00"))
                .probationBeforeTotal(new BigDecimal("6000.00"))
                .probationAfterTotal(new BigDecimal("7000.00"))
                .salaryOptions(Collections.emptyList()).probationSalaryOptions(Collections.emptyList())
                .status(status).remark("测试调薪").build();
    }

}

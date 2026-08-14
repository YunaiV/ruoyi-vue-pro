package cn.iocoder.yudao.module.hrm.service.insurance.monthrecord;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.monthrecord.HrmInsuranceMonthRecordCreateReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.monthrecord.HrmInsuranceMonthEmployeeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.monthrecord.HrmInsuranceMonthRecordDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.insurance.monthrecord.HrmInsuranceMonthRecordMapper;
import cn.iocoder.yudao.module.hrm.enums.insurance.employee.HrmInsuranceEmployeeStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.insurance.monthrecord.HrmInsuranceMonthStatusEnum;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.Collections;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.INSURANCE_FIRST_MONTH_RECORD_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.INSURANCE_MONTH_RECORD_CANNOT_DELETE_ONLY;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.INSURANCE_MONTH_RECORD_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link HrmInsuranceMonthRecordServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmInsuranceMonthRecordServiceImpl.class)
public class HrmInsuranceMonthRecordServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmInsuranceMonthRecordServiceImpl monthRecordService;

    @Resource
    private HrmInsuranceMonthRecordMapper monthRecordMapper;

    @MockBean
    private HrmInsuranceMonthEmployeeRecordService monthEmployeeRecordService;

    @Test
    public void testCreateFirstMonthRecord_success() {
        // 准备参数
        HrmInsuranceMonthRecordCreateReqVO reqVO =
                new HrmInsuranceMonthRecordCreateReqVO().setYear(2026).setMonth(7);
        when(monthEmployeeRecordService.getMonthEmployeeRecordListByMonthRecordId(anyLong()))
                .thenReturn(Collections.emptyList());

        // 调用
        Long id = monthRecordService.createFirstMonthRecord(reqVO);

        // 断言
        assertNotNull(id);
        HrmInsuranceMonthRecordDO monthRecord = monthRecordMapper.selectById(id);
        assertEquals("2026年7月社保表", monthRecord.getTitle());
        assertEquals(HrmInsuranceMonthStatusEnum.UNARCHIVED.getStatus(), monthRecord.getStatus());
        assertEquals(0, monthRecord.getInsuredEmployeeCount());
        assertEquals(0, BigDecimal.ZERO.compareTo(monthRecord.getPersonalInsuranceAmount()));
        verify(monthEmployeeRecordService).createMonthEmployeeRecordList(any(HrmInsuranceMonthRecordDO.class));
    }

    @Test
    public void testCreateFirstMonthRecord_recordExists() {
        // mock 数据
        monthRecordMapper.insert(randomMonthRecord(2026, 6,
                HrmInsuranceMonthStatusEnum.UNARCHIVED.getStatus()));
        // 准备参数
        HrmInsuranceMonthRecordCreateReqVO reqVO =
                new HrmInsuranceMonthRecordCreateReqVO().setYear(2026).setMonth(7);

        // 调用，并断言异常
        assertServiceException(() -> monthRecordService.createFirstMonthRecord(reqVO),
                INSURANCE_FIRST_MONTH_RECORD_EXISTS);
    }

    @Test
    public void testCreateNextMonthRecord_firstMonthNotExists() {
        // 调用，并断言异常
        assertServiceException(() -> monthRecordService.createNextMonthRecord(),
                INSURANCE_MONTH_RECORD_NOT_EXISTS);
    }

    @Test
    public void testCreateNextMonthRecord_emptyMonthSuccess() {
        // mock 数据
        HrmInsuranceMonthRecordDO currentMonthRecord = randomMonthRecord(2026, 6,
                HrmInsuranceMonthStatusEnum.UNARCHIVED.getStatus());
        monthRecordMapper.insert(currentMonthRecord);
        when(monthEmployeeRecordService.getMonthEmployeeRecordListByMonthRecordId(anyLong()))
                .thenReturn(Collections.emptyList());

        // 调用
        Long nextMonthRecordId = monthRecordService.createNextMonthRecord();

        // 断言
        assertEquals(HrmInsuranceMonthStatusEnum.ARCHIVED.getStatus(),
                monthRecordMapper.selectById(currentMonthRecord.getId()).getStatus());
        assertEquals(7, monthRecordMapper.selectById(nextMonthRecordId).getMonth());
    }

    @Test
    public void testCreateNextMonthRecord_success() {
        // mock 数据
        HrmInsuranceMonthRecordDO currentMonthRecord = randomMonthRecord(2026, 12,
                HrmInsuranceMonthStatusEnum.UNARCHIVED.getStatus());
        monthRecordMapper.insert(currentMonthRecord);
        when(monthEmployeeRecordService.getMonthEmployeeRecordListByMonthRecordId(anyLong()))
                .thenReturn(Collections.emptyList());

        // 调用
        Long nextMonthRecordId = monthRecordService.createNextMonthRecord();

        // 断言
        assertEquals(HrmInsuranceMonthStatusEnum.ARCHIVED.getStatus(),
                monthRecordMapper.selectById(currentMonthRecord.getId()).getStatus());
        HrmInsuranceMonthRecordDO nextMonthRecord = monthRecordMapper.selectById(nextMonthRecordId);
        assertEquals(2027, nextMonthRecord.getYear());
        assertEquals(1, nextMonthRecord.getMonth());
    }

    @Test
    public void testDeleteMonthRecord_success() {
        // mock 数据
        HrmInsuranceMonthRecordDO previousMonthRecord = randomMonthRecord(2026, 6,
                HrmInsuranceMonthStatusEnum.ARCHIVED.getStatus());
        monthRecordMapper.insert(previousMonthRecord);
        HrmInsuranceMonthRecordDO currentMonthRecord = randomMonthRecord(2026, 7,
                HrmInsuranceMonthStatusEnum.UNARCHIVED.getStatus());
        monthRecordMapper.insert(currentMonthRecord);

        // 调用
        monthRecordService.deleteMonthRecord(currentMonthRecord.getId());

        // 断言
        assertNull(monthRecordMapper.selectById(currentMonthRecord.getId()));
        assertEquals(HrmInsuranceMonthStatusEnum.UNARCHIVED.getStatus(),
                monthRecordMapper.selectById(previousMonthRecord.getId()).getStatus());
        verify(monthEmployeeRecordService)
                .deleteMonthEmployeeRecordListByMonthRecordId(currentMonthRecord.getId());
    }

    @Test
    public void testDeleteMonthRecord_onlyOne() {
        // mock 数据
        HrmInsuranceMonthRecordDO monthRecord = randomMonthRecord(2026, 7,
                HrmInsuranceMonthStatusEnum.UNARCHIVED.getStatus());
        monthRecordMapper.insert(monthRecord);

        // 调用，并断言异常
        assertServiceException(() -> monthRecordService.deleteMonthRecord(monthRecord.getId()),
                INSURANCE_MONTH_RECORD_CANNOT_DELETE_ONLY);
    }

    // ========== 随机对象 ==========

    private HrmInsuranceMonthRecordDO randomMonthRecord(Integer year, Integer month, Integer status) {
        return randomPojo(HrmInsuranceMonthRecordDO.class, record -> record
                .setId(null).setTitle(year + "年" + month + "月社保表")
                .setYear(year).setMonth(month)
                .setInsuredEmployeeCount(1).setStoppedEmployeeCount(0).setStatus(status)
                .setPersonalInsuranceAmount(BigDecimal.valueOf(100))
                .setPersonalProvidentFundAmount(BigDecimal.valueOf(50))
                .setCorporateInsuranceAmount(BigDecimal.valueOf(200))
                .setCorporateProvidentFundAmount(BigDecimal.valueOf(50)));
    }

}

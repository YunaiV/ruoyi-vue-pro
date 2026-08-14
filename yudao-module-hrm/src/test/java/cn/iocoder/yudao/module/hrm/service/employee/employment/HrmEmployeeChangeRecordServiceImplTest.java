package cn.iocoder.yudao.module.hrm.service.employee.employment;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.changerecord.HrmEmployeeChangeRecordCreateReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.employment.HrmEmployeeChangeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.employee.employment.HrmEmployeeChangeRecordMapper;
import cn.iocoder.yudao.module.hrm.enums.employee.employment.HrmEmployeeChangeTypeEnum;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link HrmEmployeeChangeRecordServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmEmployeeChangeRecordServiceImpl.class)
public class HrmEmployeeChangeRecordServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmEmployeeChangeRecordServiceImpl changeRecordService;

    @Resource
    private HrmEmployeeChangeRecordMapper changeRecordMapper;

    @Test
    public void testCreateChangeRecord_success() {
        // 准备参数
        Long employeeId = randomLongId();
        HrmEmployeeChangeRecordCreateReqVO changeRecord = randomPojo(HrmEmployeeChangeRecordCreateReqVO.class,
                o -> o.setEmployeeId(employeeId).setType(HrmEmployeeChangeTypeEnum.TRANSFER.getType())
                        .setReason(1).setEffectTime(LocalDate.now().atTime(15, 30)));

        // 调用
        changeRecordService.createEmployeeChangeRecord(changeRecord);
        // 断言
        HrmEmployeeChangeRecordDO dbChangeRecord = changeRecordMapper.selectListByEmployeeId(employeeId).get(0);
        assertPojoEquals(changeRecord, dbChangeRecord);
        assertEquals(LocalDate.now().atStartOfDay(), dbChangeRecord.getEffectTime());
    }

    @Test
    public void testCreateChangeRecord_rehireKeepsEntryTime() {
        // 准备参数
        Long employeeId = randomLongId();
        LocalDateTime entryTime = LocalDate.now().atTime(15, 30);
        HrmEmployeeChangeRecordCreateReqVO changeRecord = randomPojo(HrmEmployeeChangeRecordCreateReqVO.class,
                o -> o.setEmployeeId(employeeId).setType(HrmEmployeeChangeTypeEnum.REHIRE.getType())
                        .setReason(1).setEffectTime(entryTime));

        // 调用
        changeRecordService.createEmployeeChangeRecord(changeRecord);

        // 断言
        HrmEmployeeChangeRecordDO dbChangeRecord = changeRecordMapper.selectListByEmployeeId(employeeId).get(0);
        assertEquals(entryTime, dbChangeRecord.getEffectTime());
    }

    @Test
    public void testGetChangeRecordList() {
        // mock 数据
        Long employeeId = randomLongId();
        LocalDate beginEffectTime = LocalDate.of(2026, 7, 1);
        HrmEmployeeChangeRecordDO firstRecord = randomChangeRecordDO(employeeId,
                HrmEmployeeChangeTypeEnum.TRANSFER, beginEffectTime.plusDays(2));
        firstRecord.setAppliedTime(beginEffectTime.plusDays(2).atStartOfDay());
        changeRecordMapper.insert(firstRecord);
        HrmEmployeeChangeRecordDO secondRecord = randomChangeRecordDO(employeeId,
                HrmEmployeeChangeTypeEnum.PROMOTION, beginEffectTime.plusDays(10));
        changeRecordMapper.insert(secondRecord);
        changeRecordMapper.insert(randomChangeRecordDO(randomLongId(),
                HrmEmployeeChangeTypeEnum.DEMOTION, beginEffectTime.plusDays(5)));
        changeRecordMapper.insert(randomChangeRecordDO(employeeId,
                HrmEmployeeChangeTypeEnum.REHIRE, beginEffectTime.minusDays(1)));
        LocalDate sameEffectTime = beginEffectTime.plusDays(20);
        HrmEmployeeChangeRecordDO earlierRecord = randomChangeRecordDO(employeeId,
                HrmEmployeeChangeTypeEnum.TRANSFER, sameEffectTime);
        earlierRecord.setCreateTime(LocalDateTime.of(2026, 7, 1, 10, 0));
        changeRecordMapper.insert(earlierRecord);
        HrmEmployeeChangeRecordDO laterRecord = randomChangeRecordDO(employeeId,
                HrmEmployeeChangeTypeEnum.PROMOTION, sameEffectTime);
        laterRecord.setCreateTime(LocalDateTime.of(2026, 7, 1, 11, 0));
        changeRecordMapper.insert(laterRecord);

        // 调用
        List<HrmEmployeeChangeRecordDO> employeeRecords =
                changeRecordService.getEmployeeChangeRecordListByEmployeeId(employeeId);
        List<HrmEmployeeChangeRecordDO> rangeRecords =
                changeRecordService.getEmployeeChangeRecordListByEmployeeIdsAndEffectTimeBetween(
                        Arrays.asList(employeeId, randomLongId()), new LocalDateTime[]{
                                beginEffectTime.atStartOfDay(), beginEffectTime.plusMonths(1).atTime(23, 59, 59)});
        List<HrmEmployeeChangeRecordDO> emptyRecords =
                changeRecordService.getEmployeeChangeRecordListByEmployeeIdsAndEffectTimeBetween(
                        Collections.emptyList(), new LocalDateTime[]{
                                beginEffectTime.atStartOfDay(), beginEffectTime.plusMonths(1).atTime(23, 59, 59)});
        List<HrmEmployeeChangeRecordDO> effectTimeRecords =
                changeRecordService.getPendingEmployeeChangeRecordList(sameEffectTime.atTime(23, 59, 59));
        // 断言
        assertEquals(5, employeeRecords.size());
        assertEquals(laterRecord.getId(), employeeRecords.get(0).getId());
        assertEquals(4, rangeRecords.size());
        assertEquals(0, emptyRecords.size());
        assertEquals(4, effectTimeRecords.size());
        assertEquals(earlierRecord.getId(), effectTimeRecords.get(2).getId());
        assertEquals(laterRecord.getId(), effectTimeRecords.get(3).getId());
    }

    private static HrmEmployeeChangeRecordDO randomChangeRecordDO(
            Long employeeId, HrmEmployeeChangeTypeEnum changeType, LocalDate effectTime) {
        return randomPojo(HrmEmployeeChangeRecordDO.class, o -> o.setId(null).setEmployeeId(employeeId)
                .setType(changeType.getType()).setReason(1).setEffectTime(effectTime.atStartOfDay())
                .setAppliedTime(null).setDeleted(false));
    }

}

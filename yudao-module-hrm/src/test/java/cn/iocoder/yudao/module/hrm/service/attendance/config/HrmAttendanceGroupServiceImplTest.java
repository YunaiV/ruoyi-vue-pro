package cn.iocoder.yudao.module.hrm.service.attendance.config;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.group.HrmAttendanceGroupSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.config.HrmAttendanceGroupDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.config.HrmAttendanceHolidayDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.attendance.config.HrmAttendanceGroupMapper;
import cn.iocoder.yudao.module.hrm.enums.attendance.config.HrmAttendanceAbsenteeismDeductMethodEnum;
import cn.iocoder.yudao.module.hrm.enums.attendance.config.HrmAttendanceHolidayTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.attendance.config.HrmAttendanceLateEarlyDeductMethodEnum;
import cn.iocoder.yudao.module.hrm.enums.attendance.config.HrmAttendanceMisscardDeductMethodEnum;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link HrmAttendanceGroupServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
public class HrmAttendanceGroupServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private HrmAttendanceGroupServiceImpl attendanceGroupService;

    @Mock
    private HrmAttendanceGroupMapper attendanceGroupMapper;

    @Mock
    private HrmEmployeeService employeeService;

    @Mock
    private DeptApi deptApi;

    @Test
    public void testGetAttendanceGroupMap_employeeAndDeptAndDefaultGroup() {
        // mock 数据
        HrmEmployeeDO directEmployee = new HrmEmployeeDO().setId(1L).setDeptId(10L);
        HrmEmployeeDO deptEmployee = new HrmEmployeeDO().setId(2L).setDeptId(20L);
        HrmEmployeeDO defaultEmployee = new HrmEmployeeDO().setId(3L).setDeptId(30L);
        HrmAttendanceGroupDO defaultGroup = new HrmAttendanceGroupDO().setId(100L).setDefaultStatus(true);
        HrmAttendanceGroupDO directGroup = new HrmAttendanceGroupDO().setId(102L)
                .setEmployeeIds(Collections.singletonList(directEmployee.getId()));
        HrmAttendanceGroupDO deptGroup = new HrmAttendanceGroupDO().setId(101L)
                .setDeptIds(Collections.singletonList(10L));
        Map<Long, HrmEmployeeDO> employeeMap = new HashMap<>();
        employeeMap.put(directEmployee.getId(), directEmployee);
        employeeMap.put(deptEmployee.getId(), deptEmployee);
        employeeMap.put(defaultEmployee.getId(), defaultEmployee);
        when(employeeService.getEmployeeMap(Arrays.asList(1L, 2L, 3L))).thenReturn(employeeMap);
        when(attendanceGroupMapper.selectListOrderByDefaultStatusAndId())
                .thenReturn(Arrays.asList(defaultGroup, directGroup, deptGroup));
        when(deptApi.getParentDeptList(10L)).thenReturn(Collections.emptyList());
        when(deptApi.getParentDeptList(20L)).thenReturn(Collections.singletonList(
                new DeptRespDTO().setId(10L)));
        when(deptApi.getParentDeptList(30L)).thenReturn(Collections.emptyList());

        // 调用
        Map<Long, HrmAttendanceGroupDO> result =
                attendanceGroupService.getAttendanceGroupMap(Arrays.asList(1L, 2L, 3L));

        // 断言
        assertEquals(directGroup, result.get(directEmployee.getId()));
        assertEquals(deptGroup, result.get(deptEmployee.getId()));
        assertEquals(defaultGroup, result.get(defaultEmployee.getId()));
        verify(deptApi, times(3)).getParentDeptList(any());
        verify(deptApi, never()).getDeptList(anyCollection());
    }

    @Test
    public void testGetAttendanceGroupShiftMap_specialWorkDateBeforeRestHoliday() {
        // mock 数据
        LocalDate attendanceDate = LocalDate.of(2026, 10, 1);
        HrmAttendanceGroupDO.Shift workShift = HrmAttendanceGroupDO.Shift.builder()
                .weeks(Collections.singletonList(1))
                .startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(18, 0)).build();
        HrmAttendanceGroupDO attendanceGroup = HrmAttendanceGroupDO.builder().rest(true)
                .shifts(Collections.singletonList(workShift))
                .specialDates(Collections.singletonList(HrmAttendanceGroupDO.SpecialDate.builder()
                        .date(attendanceDate.atStartOfDay())
                        .type(HrmAttendanceHolidayTypeEnum.WORK.getType()).build()))
                .build();
        HrmAttendanceHolidayDO holiday = HrmAttendanceHolidayDO.builder()
                .date(attendanceDate.atStartOfDay())
                .type(HrmAttendanceHolidayTypeEnum.REST.getType()).build();

        // 调用
        Map<LocalDate, HrmAttendanceGroupDO.Shift> result =
                attendanceGroupService.getAttendanceGroupShiftMap(attendanceGroup,
                        Collections.singletonList(attendanceDate),
                        Collections.singletonMap(attendanceDate, holiday));

        // 断言
        assertEquals(workShift, result.get(attendanceDate));
    }

    @Test
    public void testGetAttendanceGroupShiftMap_specialRestDateBeforeWorkHoliday() {
        // mock 数据
        LocalDate attendanceDate = LocalDate.of(2026, 10, 1);
        HrmAttendanceGroupDO attendanceGroup = HrmAttendanceGroupDO.builder().rest(true)
                .shifts(Collections.singletonList(HrmAttendanceGroupDO.Shift.builder()
                        .weeks(Collections.singletonList(attendanceDate.getDayOfWeek().getValue()))
                        .startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(18, 0)).build()))
                .specialDates(Collections.singletonList(HrmAttendanceGroupDO.SpecialDate.builder()
                        .date(attendanceDate.atStartOfDay())
                        .type(HrmAttendanceHolidayTypeEnum.REST.getType()).build()))
                .build();
        HrmAttendanceHolidayDO holiday = HrmAttendanceHolidayDO.builder()
                .date(attendanceDate.atStartOfDay())
                .type(HrmAttendanceHolidayTypeEnum.WORK.getType()).build();

        // 调用
        Map<LocalDate, HrmAttendanceGroupDO.Shift> result =
                attendanceGroupService.getAttendanceGroupShiftMap(attendanceGroup,
                        Collections.singletonList(attendanceDate),
                        Collections.singletonMap(attendanceDate, holiday));

        // 断言
        assertNull(result.get(attendanceDate));
    }

    @Test
    public void testCreateAttendanceGroup_convertEmbeddedSettings() {
        // mock 数据
        when(attendanceGroupMapper.insert(any(HrmAttendanceGroupDO.class))).thenAnswer(invocation -> {
            HrmAttendanceGroupDO group = invocation.getArgument(0);
            group.setId(1L);
            return 1;
        });
        when(deptApi.getChildDeptList(anyCollection())).thenReturn(Collections.emptyList());
        when(attendanceGroupMapper.selectListOrderByDefaultStatusAndId()).thenReturn(Collections.emptyList());

        // 准备参数
        LocalDateTime specialDate = LocalDate.of(2026, 10, 1).atTime(15, 30);
        HrmAttendanceGroupSaveReqVO reqVO = new HrmAttendanceGroupSaveReqVO()
                .setName("考勤组 A")
                .setOpenWifiCard(true).setOpenPointCard(true).setRest(true)
                .setDeptIds(Collections.emptyList()).setEmployeeIds(Collections.singletonList(10L))
                .setSpecialDates(Collections.singletonList(new HrmAttendanceGroupSaveReqVO.SpecialDate()
                        .setDate(specialDate).setType(HrmAttendanceHolidayTypeEnum.WORK.getType())))
                .setShifts(Collections.singletonList(new HrmAttendanceGroupSaveReqVO.Shift()
                        .setWeeks(Arrays.asList(1, 2, 3, 4, 5))
                        .setStartTime(LocalTime.of(9, 0)).setEndTime(LocalTime.of(18, 0))
                        .setClockInStartTime(LocalTime.of(8, 0)).setClockInEndTime(LocalTime.of(10, 0))
                        .setClockOutStartTime(LocalTime.of(17, 0)).setClockOutEndTime(LocalTime.of(20, 0))
                        .setRestStartTime(LocalTime.of(12, 0)).setRestEndTime(LocalTime.of(13, 0))
                        .setExcludeRestTime(true)))
                .setPoints(Collections.singletonList(new HrmAttendanceGroupSaveReqVO.Point()
                        .setName("总部").setAddress("总部办公区")
                        .setLatitude(new BigDecimal("39.983424"))
                        .setLongitude(new BigDecimal("116.322987")).setRadius(300)))
                .setWifis(Collections.singletonList(new HrmAttendanceGroupSaveReqVO.Wifi()
                        .setSsid("Office-WiFi").setMac("00:11:22:33:44:55")))
                .setDeductRule(new HrmAttendanceGroupSaveReqVO.DeductRule()
                        .setLateMethod(HrmAttendanceLateEarlyDeductMethodEnum.BY_MINUTE.getMethod())
                        .setLateDeductMoney(BigDecimal.TEN)
                        .setEarlyMethod(HrmAttendanceLateEarlyDeductMethodEnum.BY_MINUTE.getMethod())
                        .setEarlyDeductMoney(BigDecimal.TEN)
                        .setAbsenteeismMethod(HrmAttendanceAbsenteeismDeductMethodEnum.BY_DAY.getMethod())
                        .setAbsenteeismDeductMoney(BigDecimal.valueOf(100))
                        .setMisscardMethod(HrmAttendanceMisscardDeductMethodEnum.BY_COUNT.getMethod())
                        .setMisscardDeductMoney(BigDecimal.valueOf(20)));

        // 调用
        Long groupId = attendanceGroupService.createAttendanceGroup(reqVO);

        // 断言
        ArgumentCaptor<HrmAttendanceGroupDO> insertCaptor = ArgumentCaptor.forClass(HrmAttendanceGroupDO.class);
        verify(attendanceGroupMapper).insert(insertCaptor.capture());
        HrmAttendanceGroupDO group = insertCaptor.getValue();
        assertEquals(1L, groupId);
        assertFalse(group.getDefaultStatus());
        assertEquals(HrmAttendanceGroupDO.SpecialDate.class, group.getSpecialDates().get(0).getClass());
        assertEquals(specialDate.toLocalDate().atStartOfDay(), group.getSpecialDates().get(0).getDate());
        assertEquals(HrmAttendanceGroupDO.Shift.class, group.getShifts().get(0).getClass());
        assertEquals(reqVO.getShifts().get(0).getStartTime(), group.getShifts().get(0).getStartTime());
        assertEquals(HrmAttendanceGroupDO.Point.class, group.getPoints().get(0).getClass());
        assertEquals(reqVO.getPoints().get(0).getLatitude(), group.getPoints().get(0).getLatitude());
        assertEquals(HrmAttendanceGroupDO.Wifi.class, group.getWifis().get(0).getClass());
        assertEquals(reqVO.getWifis().get(0).getMac(), group.getWifis().get(0).getMac());
        assertEquals(HrmAttendanceGroupDO.DeductRule.class, group.getDeductRule().getClass());
        assertEquals(reqVO.getDeductRule().getLateDeductMoney(), group.getDeductRule().getLateDeductMoney());
        assertEquals(specialDate, reqVO.getSpecialDates().get(0).getDate());
    }

    @Test
    public void testUpdateAttendanceGroup_transferRepeatedScope() {
        // mock 数据
        HrmAttendanceGroupDO currentGroup = new HrmAttendanceGroupDO().setId(1L)
                .setName("考勤组 A").setDeptIds(Collections.emptyList())
                .setEmployeeIds(Collections.singletonList(10L)).setDefaultStatus(false);
        HrmAttendanceGroupDO otherGroup = new HrmAttendanceGroupDO().setId(2L)
                .setName("考勤组 B").setDeptIds(Collections.emptyList())
                .setEmployeeIds(Arrays.asList(10L, 11L)).setDefaultStatus(false);
        when(attendanceGroupMapper.selectById(currentGroup.getId())).thenReturn(currentGroup);
        when(deptApi.getChildDeptList(anyCollection())).thenReturn(Collections.emptyList());
        when(attendanceGroupMapper.selectListOrderByDefaultStatusAndId())
                .thenReturn(Arrays.asList(currentGroup, otherGroup));
        HrmAttendanceGroupSaveReqVO reqVO = new HrmAttendanceGroupSaveReqVO();
        reqVO.setId(currentGroup.getId());
        reqVO.setName("修改后的考勤组 A");
        reqVO.setDeptIds(Collections.emptyList());
        reqVO.setEmployeeIds(Collections.singletonList(10L));
        LocalDateTime specialDate = LocalDate.of(2026, 10, 1).atTime(15, 30);
        reqVO.setSpecialDates(Collections.singletonList(new HrmAttendanceGroupSaveReqVO.SpecialDate()
                .setDate(specialDate).setType(HrmAttendanceHolidayTypeEnum.WORK.getType())));

        // 调用
        attendanceGroupService.updateAttendanceGroup(reqVO);

        // 断言
        ArgumentCaptor<HrmAttendanceGroupDO> updateCaptor =
                ArgumentCaptor.forClass(HrmAttendanceGroupDO.class);
        verify(attendanceGroupMapper, times(2)).updateById(updateCaptor.capture());
        List<HrmAttendanceGroupDO> updateList = updateCaptor.getAllValues();
        assertEquals(currentGroup.getId(), updateList.get(0).getId());
        assertEquals(reqVO.getName(), updateList.get(0).getName());
        assertEquals(specialDate.toLocalDate().atStartOfDay(),
                updateList.get(0).getSpecialDates().get(0).getDate());
        assertEquals(specialDate, reqVO.getSpecialDates().get(0).getDate());
        assertEquals(otherGroup.getId(), updateList.get(1).getId());
        assertEquals(Collections.singletonList(11L), updateList.get(1).getEmployeeIds());
        verify(attendanceGroupMapper, never()).insert(any(HrmAttendanceGroupDO.class));
    }

    @Test
    public void testDeleteAttendanceGroup_success() {
        // mock 数据
        HrmAttendanceGroupDO group =
                new HrmAttendanceGroupDO().setId(1L).setDefaultStatus(false);
        when(attendanceGroupMapper.selectById(group.getId())).thenReturn(group);

        // 调用
        attendanceGroupService.deleteAttendanceGroup(group.getId());

        // 断言
        verify(attendanceGroupMapper).deleteById(group.getId());
    }

}

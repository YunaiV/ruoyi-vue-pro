package cn.iocoder.yudao.module.hrm.service.attendance.record;

import cn.iocoder.yudao.module.hrm.service.attendance.config.HrmAttendanceGroupService;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.clock.HrmAttendanceClockPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.clock.HrmAttendanceClockSaveReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.attendance.vo.clock.HrmPortalAttendanceClockCreateReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.attendance.vo.clock.HrmPortalAttendanceClockDetailRespVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.config.HrmAttendanceGroupDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.record.HrmAttendanceClockDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.attendance.record.HrmAttendanceClockMapper;
import cn.iocoder.yudao.module.hrm.enums.attendance.record.HrmAttendanceClockButtonStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.attendance.record.HrmAttendanceClockSourceEnum;
import cn.iocoder.yudao.module.hrm.enums.attendance.record.HrmAttendanceClockStatusEnum;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.ATTENDANCE_CLOCK_POINT_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.ATTENDANCE_CLOCK_WIFI_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * {@link HrmAttendanceClockServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmAttendanceClockServiceImpl.class)
public class HrmAttendanceClockServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmAttendanceClockServiceImpl attendanceClockService;

    @Resource
    private HrmAttendanceClockMapper attendanceClockMapper;

    @MockBean
    private HrmEmployeeService employeeService;
    @MockBean
    private HrmAttendanceGroupService attendanceGroupService;

    @Test
    public void testCreate_sourceAndModifyBoundary() {
        // 准备参数
        HrmEmployeeDO employee = createEmployee(10L, "手工打卡员工", 210L);
        when(employeeService.validateEmployeeExists(employee.getId())).thenReturn(employee);
        when(attendanceGroupService.getEmployeeAttendanceShift(any(), any())).thenReturn(createShift());
        HrmAttendanceClockSaveReqVO createReqVO = createClockReqVO(employee.getId(),
                LocalDateTime.of(2026, 7, 18, 9, 10), LocalDateTime.of(2026, 7, 18, 9, 0), 1);

        // 调用
        Long manualId = attendanceClockService.createAttendanceClock(createReqVO);

        // 断言：管理端新增不能伪造来源和状态
        HrmAttendanceClockDO manualClock = attendanceClockMapper.selectById(manualId);
        assertEquals(HrmAttendanceClockSourceEnum.MANUAL.getSource(), manualClock.getSourceType());
        assertEquals(HrmAttendanceClockStatusEnum.LATE.getStatus(), manualClock.getStatus());

        // ========== 非手工打卡记录 ==========
        HrmAttendanceClockDO mobileClock = HrmAttendanceClockDO.builder()
                .employeeId(employee.getId()).type(1)
                .attendanceTime(LocalDateTime.of(2026, 7, 18, 9, 0))
                .clockTime(LocalDateTime.of(2026, 7, 18, 9, 0))
                .sourceType(HrmAttendanceClockSourceEnum.MOBILE.getSource())
                .status(HrmAttendanceClockStatusEnum.NORMAL.getStatus()).stage(1).build();
        attendanceClockMapper.insert(mobileClock);
        HrmAttendanceClockSaveReqVO updateReqVO = createClockReqVO(employee.getId(),
                LocalDateTime.of(2026, 7, 18, 9, 1), LocalDateTime.of(2026, 7, 18, 9, 0), 1);
        updateReqVO.setId(mobileClock.getId());

        // 调用，并断言异常：非手工记录不能修改、删除，批量删除不会部分成功
        assertThrows(ServiceException.class, () -> attendanceClockService.updateAttendanceClock(updateReqVO));
        assertThrows(ServiceException.class, () -> attendanceClockService.deleteAttendanceClock(mobileClock.getId()));
        assertThrows(ServiceException.class, () -> attendanceClockService.deleteAttendanceClockList(
                Arrays.asList(manualId, mobileClock.getId())));
        assertNotNull(attendanceClockMapper.selectById(manualId));
        assertNotNull(attendanceClockMapper.selectById(mobileClock.getId()));
    }

    @Test
    public void testGetPageList() {
        // mock 数据
        HrmEmployeeDO employee = createEmployee(20L, "分页员工", 300L);
        HrmAttendanceClockDO lateClock = createClock(employee.getId(),
                LocalDateTime.of(2026, 7, 8, 9, 15), LocalDateTime.of(2026, 7, 8, 9, 0),
                1, HrmAttendanceClockStatusEnum.LATE.getStatus());
        HrmAttendanceClockDO earlyClock = createClock(employee.getId(),
                LocalDateTime.of(2026, 7, 8, 17, 45), LocalDateTime.of(2026, 7, 8, 18, 0),
                2, HrmAttendanceClockStatusEnum.EARLY.getStatus());
        attendanceClockMapper.insert(lateClock);
        attendanceClockMapper.insert(earlyClock);
        when(employeeService.getEmployeeList(any())).thenReturn(Collections.singletonList(employee));

        // 准备参数
        HrmAttendanceClockPageReqVO pageReqVO = new HrmAttendanceClockPageReqVO();
        pageReqVO.setSearch(employee.getJobNumber());
        pageReqVO.setDeptIds(Collections.singletonList(employee.getDeptId()));
        pageReqVO.setAddress("总部");

        // 调用，并断言异常
        assertEquals(2, attendanceClockService.getAttendanceClockPage(pageReqVO).getTotal());
        List<HrmAttendanceClockDO> clockList = attendanceClockService.getAttendanceClockList(pageReqVO);
        assertEquals(2, clockList.size());
        assertEquals(employee.getId(), clockList.get(0).getEmployeeId());
    }

    @Test
    public void testGetAttendanceClockListByEmployeeIdAndClockTime() {
        // mock 数据
        Long employeeId = 30L;
        LocalDateTime beginTime = LocalDateTime.of(2026, 8, 1, 0, 0);
        LocalDateTime endTime = beginTime.plusMonths(1);
        HrmAttendanceClockDO beginBoundaryClock = createClock(employeeId, beginTime, beginTime,
                1, HrmAttendanceClockStatusEnum.NORMAL.getStatus());
        attendanceClockMapper.insert(beginBoundaryClock);
        HrmAttendanceClockDO innerClock = createClock(employeeId, endTime.minusSeconds(1), endTime.minusSeconds(1),
                2, HrmAttendanceClockStatusEnum.NORMAL.getStatus());
        attendanceClockMapper.insert(innerClock);
        HrmAttendanceClockDO endBoundaryClock = createClock(employeeId, endTime, endTime,
                1, HrmAttendanceClockStatusEnum.NORMAL.getStatus());
        attendanceClockMapper.insert(endBoundaryClock);

        // 调用
        List<HrmAttendanceClockDO> result = attendanceClockService
                .getAttendanceClockListByEmployeeIdAndClockTime(
                        employeeId, new LocalDateTime[]{beginTime, endTime});

        // 断言：开始、结束边界都包含
        assertEquals(3, result.size());
        assertEquals(beginBoundaryClock.getId(), result.get(0).getId());
        assertEquals(innerClock.getId(), result.get(1).getId());
        assertEquals(endBoundaryClock.getId(), result.get(2).getId());
    }

    @Test
    public void testDeleteList_deduplicatesAndPrevalidates() {
        // mock 数据
        HrmEmployeeDO employee = createEmployee(40L, "批量删除员工", 410L);
        when(employeeService.validateEmployeeExists(employee.getId())).thenReturn(employee);
        when(attendanceGroupService.getEmployeeAttendanceShift(any(), any())).thenReturn(createShift());
        LocalDateTime attendanceTime = LocalDateTime.of(2026, 7, 11, 9, 0);
        Long firstId = attendanceClockService.createAttendanceClock(createClockReqVO(employee.getId(),
                attendanceTime, attendanceTime, 1));
        Long secondId = attendanceClockService.createAttendanceClock(createClockReqVO(employee.getId(),
                attendanceTime.plusHours(9), attendanceTime.plusHours(9), 2));
        Long retainedId = attendanceClockService.createAttendanceClock(createClockReqVO(employee.getId(),
                attendanceTime.plusDays(1), attendanceTime.plusDays(1), 1));

        // 调用
        attendanceClockService.deleteAttendanceClockList(Arrays.asList(firstId, firstId, secondId));

        // 断言
        assertNull(attendanceClockMapper.selectById(firstId));
        assertNull(attendanceClockMapper.selectById(secondId));
        assertNotNull(attendanceClockMapper.selectById(retainedId));
        assertThrows(ServiceException.class, () -> attendanceClockService.deleteAttendanceClockList(
                Arrays.asList(retainedId, Long.MAX_VALUE)));
        assertNotNull(attendanceClockMapper.selectById(retainedId));
    }

    @Test
    public void testCreate_validatesShiftClockWindow() {
        // mock 数据
        HrmEmployeeDO employee = createEmployee(50L, "班次窗口员工", 510L);
        when(employeeService.validateEmployeeExists(employee.getId())).thenReturn(employee);
        when(attendanceGroupService.getEmployeeAttendanceShift(any(), any())).thenReturn(createShift());

        // 准备参数：伪造应打卡时间
        HrmAttendanceClockSaveReqVO invalidScheduledTimeReqVO = createClockReqVO(employee.getId(),
                LocalDateTime.of(2026, 7, 18, 9, 30), LocalDateTime.of(2026, 7, 18, 9, 30), 1);
        // 准备参数：实际打卡时间早于允许窗口
        HrmAttendanceClockSaveReqVO invalidClockTimeReqVO = createClockReqVO(employee.getId(),
                LocalDateTime.of(2026, 7, 18, 7, 59), LocalDateTime.of(2026, 7, 18, 9, 0), 1);

        // 调用、断言
        assertThrows(ServiceException.class,
                () -> attendanceClockService.createAttendanceClock(invalidScheduledTimeReqVO));
        assertThrows(ServiceException.class,
                () -> attendanceClockService.createAttendanceClock(invalidClockTimeReqVO));
        Long boundaryClockId = attendanceClockService.createAttendanceClock(createClockReqVO(employee.getId(),
                LocalDateTime.of(2026, 7, 18, 8, 0), LocalDateTime.of(2026, 7, 18, 9, 0), 1));
        assertNotNull(attendanceClockMapper.selectById(boundaryClockId));
    }

    @Test
    public void testCreate_supportsCrossDayShiftClockWindow() {
        // mock 数据
        HrmEmployeeDO employee = createEmployee(60L, "跨日班次员工", 610L);
        when(employeeService.validateEmployeeExists(employee.getId())).thenReturn(employee);
        HrmAttendanceGroupDO.Shift shift = createShift()
                .setStartTime(LocalTime.of(22, 0)).setEndTime(LocalTime.of(6, 0))
                .setClockInStartTime(LocalTime.of(20, 0)).setClockInEndTime(LocalTime.of(23, 0))
                .setClockOutStartTime(LocalTime.of(5, 0)).setClockOutEndTime(LocalTime.of(8, 0));
        when(attendanceGroupService.getEmployeeAttendanceShift(any(), any())).thenReturn(shift);

        // 调用：7 月 18 日夜班的下班卡发生在次日
        Long clockId = attendanceClockService.createAttendanceClock(createClockReqVO(employee.getId(),
                LocalDateTime.of(2026, 7, 19, 6, 30), LocalDateTime.of(2026, 7, 19, 6, 0), 2));

        // 断言
        HrmAttendanceClockDO clock = attendanceClockMapper.selectById(clockId);
        assertNotNull(clock);
        assertEquals(HrmAttendanceClockStatusEnum.NORMAL.getStatus(), clock.getStatus());
    }

    @Test
    public void testCreateMyAttendanceClock_mobileSourceAndOwner() {
        // mock 数据：本人 + 全天可打上班卡的班次
        Long userId = 9001L;
        HrmEmployeeDO employee = createEmployee(30L, "手机打卡员工", 400L);
        when(employeeService.validateEmployeeBySelf(userId)).thenReturn(employee);
        when(attendanceGroupService.getMyAttendanceGroup(employee.getId()))
                .thenReturn(createGroup(false, false, null, null));
        when(attendanceGroupService.getEmployeeAttendanceShift(any(), any())).thenReturn(createAllDayShift());

        // 调用
        Long clockId = attendanceClockService.createMyAttendanceClock(userId,
                new HrmPortalAttendanceClockCreateReqVO());

        // 断言：归属本人，来源固定手机端
        HrmAttendanceClockDO clock = attendanceClockMapper.selectById(clockId);
        assertNotNull(clock);
        assertEquals(employee.getId(), clock.getEmployeeId());
        assertEquals(HrmAttendanceClockSourceEnum.MOBILE.getSource(), clock.getSourceType());
        assertEquals(1, clock.getType());
    }

    @Test
    public void testCreateMyAttendanceClock_pointValidate() {
        // mock 数据：仅开启定位打卡
        Long userId = 9002L;
        HrmEmployeeDO employee = createEmployee(31L, "定位打卡员工", 401L);
        HrmAttendanceGroupDO.Point point = HrmAttendanceGroupDO.Point.builder()
                .name("总部").address("文三路")
                .latitude(new BigDecimal("30.270000")).longitude(new BigDecimal("120.150000"))
                .radius(200).build();
        when(employeeService.validateEmployeeBySelf(userId)).thenReturn(employee);
        when(attendanceGroupService.getMyAttendanceGroup(employee.getId()))
                .thenReturn(createGroup(true, false, Collections.singletonList(point), null));
        when(attendanceGroupService.getEmployeeAttendanceShift(any(), any())).thenReturn(createAllDayShift());

        // 准备参数：超出半径
        HrmPortalAttendanceClockCreateReqVO invalidReqVO = new HrmPortalAttendanceClockCreateReqVO();
        invalidReqVO.setLatitude(new BigDecimal("30.280000"));
        invalidReqVO.setLongitude(new BigDecimal("120.160000"));
        assertServiceException(() -> attendanceClockService.createMyAttendanceClock(userId, invalidReqVO),
                ATTENDANCE_CLOCK_POINT_INVALID);

        // 准备参数：命中地点
        HrmPortalAttendanceClockCreateReqVO validReqVO = new HrmPortalAttendanceClockCreateReqVO();
        validReqVO.setLatitude(new BigDecimal("30.270050"));
        validReqVO.setLongitude(new BigDecimal("120.150050"));
        validReqVO.setAddress("总部门口");
        Long clockId = attendanceClockService.createMyAttendanceClock(userId, validReqVO);

        // 断言
        HrmAttendanceClockDO clock = attendanceClockMapper.selectById(clockId);
        assertNotNull(clock);
        assertEquals(HrmAttendanceClockSourceEnum.MOBILE.getSource(), clock.getSourceType());
        assertEquals("总部门口", clock.getAddress());
    }

    @Test
    public void testCreateMyAttendanceClock_wifiValidate() {
        // mock 数据：仅开启 WiFi 打卡
        Long userId = 9003L;
        HrmEmployeeDO employee = createEmployee(32L, "WiFi打卡员工", 402L);
        HrmAttendanceGroupDO.Wifi wifi = HrmAttendanceGroupDO.Wifi.builder()
                .ssid("office_wifi").mac("00:11:22:33:44:55").build();
        when(employeeService.validateEmployeeBySelf(userId)).thenReturn(employee);
        when(attendanceGroupService.getMyAttendanceGroup(employee.getId()))
                .thenReturn(createGroup(false, true, null, Collections.singletonList(wifi)));
        when(attendanceGroupService.getEmployeeAttendanceShift(any(), any())).thenReturn(createAllDayShift());

        // 准备参数：未命中 WiFi
        HrmPortalAttendanceClockCreateReqVO invalidReqVO = new HrmPortalAttendanceClockCreateReqVO();
        invalidReqVO.setSsid("guest_wifi");
        invalidReqVO.setMac("AA:BB:CC:DD:EE:FF");
        assertServiceException(() -> attendanceClockService.createMyAttendanceClock(userId, invalidReqVO),
                ATTENDANCE_CLOCK_WIFI_INVALID);

        // 准备参数：MAC 精确命中
        HrmPortalAttendanceClockCreateReqVO validReqVO = new HrmPortalAttendanceClockCreateReqVO();
        validReqVO.setMac("00:11:22:33:44:55");
        Long clockId = attendanceClockService.createMyAttendanceClock(userId, validReqVO);

        // 断言
        HrmAttendanceClockDO clock = attendanceClockMapper.selectById(clockId);
        assertNotNull(clock);
        assertEquals(HrmAttendanceClockSourceEnum.MOBILE.getSource(), clock.getSourceType());
        assertEquals("00:11:22:33:44:55", clock.getMac());
    }

    @Test
    public void testGetMyAttendanceClockDetail_crossDayShift() {
        // mock 数据：跨日班次的上班卡在昨日，下班卡在今日
        Long userId = 9004L;
        HrmEmployeeDO employee = createEmployee(33L, "跨日打卡员工", 403L);
        HrmAttendanceGroupDO group = createGroup(false, false, null, null);
        HrmAttendanceGroupDO.Shift shift = createCrossDayShift();
        when(employeeService.validateEmployeeBySelf(userId)).thenReturn(employee);
        when(attendanceGroupService.getMyAttendanceGroup(employee.getId())).thenReturn(group);
        when(attendanceGroupService.getEmployeeAttendanceShift(any(), any())).thenReturn(shift);
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        LocalDateTime onDutyAttendanceTime = now.toLocalDate().minusDays(1).atTime(shift.getStartTime());
        LocalDateTime offDutyAttendanceTime = now.toLocalDate().atTime(shift.getEndTime());
        attendanceClockMapper.insert(createClock(employee.getId(), onDutyAttendanceTime,
                onDutyAttendanceTime, 1, HrmAttendanceClockStatusEnum.NORMAL.getStatus()));
        HrmAttendanceClockDO offDutyClock = createClock(employee.getId(), now,
                offDutyAttendanceTime, 2, HrmAttendanceClockStatusEnum.NORMAL.getStatus());
        attendanceClockMapper.insert(offDutyClock);

        // 调用
        HrmPortalAttendanceClockDetailRespVO detail =
                attendanceClockService.getMyAttendanceClockDetail(userId);

        // 断言：次日下班卡归属昨日班次，并进入更新打卡状态
        assertEquals(now.toLocalDate().minusDays(1), detail.getAttendanceDate());
        assertEquals(HrmAttendanceClockButtonStatusEnum.UPDATE.getStatus(),
                detail.getNextClock().getButtonStatus());
        assertEquals(offDutyClock.getClockTime(), detail.getTimeline().get(1).getClockTime());
    }

    // ========== 随机对象 ==========

    private HrmEmployeeDO createEmployee(Long id, String name, Long deptId) {
        return new HrmEmployeeDO().setId(id).setName(name).setJobNumber("NO-" + name)
                .setDeptId(deptId).setPostName("工程师");
    }

    private HrmAttendanceClockDO createClock(Long employeeId, LocalDateTime clockTime,
                                             LocalDateTime attendanceTime, Integer clockType,
                                             Integer status) {
        return HrmAttendanceClockDO.builder().employeeId(employeeId).clockTime(clockTime)
                .attendanceTime(attendanceTime).type(clockType).sourceType(
                        HrmAttendanceClockSourceEnum.MANUAL.getSource())
                .status(status).stage(1).address("总部").build();
    }

    private HrmAttendanceClockSaveReqVO createClockReqVO(Long employeeId, LocalDateTime clockTime,
                                                          LocalDateTime attendanceTime, Integer clockType) {
        HrmAttendanceClockSaveReqVO reqVO = new HrmAttendanceClockSaveReqVO();
        reqVO.setEmployeeId(employeeId);
        reqVO.setClockTime(clockTime);
        reqVO.setAttendanceTime(attendanceTime);
        reqVO.setType(clockType);
        reqVO.setAddress("总部");
        return reqVO;
    }

    private HrmAttendanceGroupDO.Shift createShift() {
        return HrmAttendanceGroupDO.Shift.builder()
                .weeks(Arrays.asList(1, 2, 3, 4, 5))
                .startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(18, 0))
                .clockInStartTime(LocalTime.of(8, 0)).clockInEndTime(LocalTime.of(10, 0))
                .clockOutStartTime(LocalTime.of(17, 0)).clockOutEndTime(LocalTime.of(19, 0))
                .restStartTime(LocalTime.of(12, 0)).restEndTime(LocalTime.of(13, 0))
                .excludeRestTime(true).build();
    }

    /** 全天可打卡班次，避免单测依赖当前时刻落在固定窗口内 */
    private HrmAttendanceGroupDO.Shift createAllDayShift() {
        return HrmAttendanceGroupDO.Shift.builder()
                .weeks(Arrays.asList(1, 2, 3, 4, 5, 6, 7))
                .startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(18, 0))
                .clockInStartTime(LocalTime.of(0, 0)).clockInEndTime(LocalTime.of(23, 59))
                .clockOutStartTime(LocalTime.of(0, 0)).clockOutEndTime(LocalTime.of(23, 59))
                .restStartTime(LocalTime.of(12, 0)).restEndTime(LocalTime.of(13, 0))
                .excludeRestTime(true).build();
    }

    private HrmAttendanceGroupDO.Shift createCrossDayShift() {
        return HrmAttendanceGroupDO.Shift.builder()
                .weeks(Arrays.asList(1, 2, 3, 4, 5, 6, 7))
                .startTime(LocalTime.of(23, 0)).endTime(LocalTime.of(22, 0))
                .clockInStartTime(LocalTime.MIN).clockInEndTime(LocalTime.of(23, 59))
                .clockOutStartTime(LocalTime.MIN).clockOutEndTime(LocalTime.of(23, 59))
                .restStartTime(LocalTime.of(12, 0)).restEndTime(LocalTime.of(13, 0))
                .excludeRestTime(true).build();
    }

    private HrmAttendanceGroupDO createGroup(boolean openPointCard, boolean openWifiCard,
                                             List<HrmAttendanceGroupDO.Point> points,
                                             List<HrmAttendanceGroupDO.Wifi> wifis) {
        return HrmAttendanceGroupDO.builder()
                .id(1L).name("默认考勤组").openPointCard(openPointCard).openWifiCard(openWifiCard)
                .points(points).wifis(wifis).defaultStatus(true).build();
    }

}

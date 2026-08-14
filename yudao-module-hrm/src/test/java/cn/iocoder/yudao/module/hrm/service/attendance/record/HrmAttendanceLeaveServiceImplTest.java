package cn.iocoder.yudao.module.hrm.service.attendance.record;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.leave.HrmAttendanceLeaveCancelReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.leave.HrmAttendanceLeaveCreateReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.leave.HrmAttendanceLeavePageReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.record.HrmAttendanceLeaveDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.attendance.record.HrmAttendanceLeaveMapper;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.ATTENDANCE_LEAVE_TIME_CONFLICT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link HrmAttendanceLeaveServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmAttendanceLeaveServiceImpl.class)
public class HrmAttendanceLeaveServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmAttendanceLeaveServiceImpl attendanceLeaveService;
    @Resource
    private HrmAttendanceLeaveMapper attendanceLeaveMapper;
    @MockitoBean
    private HrmEmployeeService employeeService;
    @MockitoBean
    private BpmProcessInstanceApi processInstanceApi;

    @Test
    public void testCreateLeave_success() {
        // mock 数据
        Long userId = randomLongId();
        HrmEmployeeDO employee = new HrmEmployeeDO().setId(randomLongId())
                .setDeptId(randomLongId()).setName("张三");
        when(employeeService.validateEmployeeBySelf(userId)).thenReturn(employee);
        when(processInstanceApi.createProcessInstance(eq(userId), any())).thenReturn("leave-process");

        // 准备参数
        HrmAttendanceLeaveCreateReqVO reqVO = new HrmAttendanceLeaveCreateReqVO();
        reqVO.setType("年假");
        reqVO.setStartTime(LocalDateTime.of(2026, 8, 1, 9, 0));
        reqVO.setEndTime(LocalDateTime.of(2026, 8, 1, 18, 0));
        reqVO.setDay(new BigDecimal("1.00"));
        reqVO.setReason("个人事务");

        // 调用
        Long id = attendanceLeaveService.createLeave(userId, reqVO);

        // 断言
        HrmAttendanceLeaveDO attendanceLeave = attendanceLeaveMapper.selectById(id);
        assertNotNull(attendanceLeave);
        assertEquals(employee.getId(), attendanceLeave.getEmployeeId());
        assertEquals(0, reqVO.getDay().compareTo(attendanceLeave.getDay()));
        assertEquals(BpmProcessInstanceStatusEnum.RUNNING.getStatus(), attendanceLeave.getApprovalStatus());
        assertEquals("leave-process", attendanceLeave.getProcessInstanceId());
        verify(employeeService).validateEmployeeExistsForUpdate(employee.getId());
    }

    @Test
    public void testCreateLeave_timeConflict() {
        // mock 数据
        Long userId = randomLongId();
        HrmEmployeeDO employee = new HrmEmployeeDO().setId(randomLongId())
                .setDeptId(randomLongId()).setName("张三");
        when(employeeService.validateEmployeeBySelf(userId)).thenReturn(employee);
        when(processInstanceApi.createProcessInstance(eq(userId), any())).thenReturn("leave-process");

        // 准备参数
        HrmAttendanceLeaveCreateReqVO reqVO = new HrmAttendanceLeaveCreateReqVO();
        reqVO.setType("年假");
        reqVO.setStartTime(LocalDateTime.of(2026, 8, 1, 9, 0));
        reqVO.setEndTime(LocalDateTime.of(2026, 8, 1, 18, 0));
        reqVO.setDay(new BigDecimal("1.00"));
        reqVO.setReason("个人事务");
        attendanceLeaveService.createLeave(userId, reqVO);

        // 调用，并断言异常
        assertServiceException(() -> attendanceLeaveService.createLeave(userId, reqVO),
                ATTENDANCE_LEAVE_TIME_CONFLICT);
    }

    @Test
    public void testCancelLeave_success() {
        // mock 数据
        Long userId = randomLongId();
        Long employeeId = randomLongId();
        when(employeeService.validateEmployeeBySelf(userId)).thenReturn(
                new HrmEmployeeDO().setId(employeeId));
        HrmAttendanceLeaveDO attendanceLeave = randomAttendanceLeaveDO(employeeId, "年假",
                LocalDateTime.of(2026, 8, 1, 9, 0), BpmProcessInstanceStatusEnum.RUNNING.getStatus());
        attendanceLeaveMapper.insert(attendanceLeave);

        // 准备参数
        HrmAttendanceLeaveCancelReqVO reqVO = new HrmAttendanceLeaveCancelReqVO();
        reqVO.setId(attendanceLeave.getId());
        reqVO.setReason("行程取消");

        // 调用
        attendanceLeaveService.cancelLeave(userId, reqVO);

        // 断言
        verify(processInstanceApi).cancelProcessInstanceByStartUser(
                userId, attendanceLeave.getProcessInstanceId(), "行程取消");
        HrmAttendanceLeaveDO result = attendanceLeaveMapper.selectById(attendanceLeave.getId());
        assertEquals(BpmProcessInstanceStatusEnum.CANCEL.getStatus(), result.getApprovalStatus());
        assertEquals("行程取消", result.getApprovalReason());
        assertNotNull(result.getApprovalTime());
    }

    @Test
    public void testUpdateLeaveApprovalStatus_success() {
        // mock 数据
        Long employeeId = randomLongId();
        HrmAttendanceLeaveDO attendanceLeave = randomAttendanceLeaveDO(employeeId, "年假",
                LocalDateTime.of(2026, 8, 1, 9, 0), BpmProcessInstanceStatusEnum.RUNNING.getStatus());
        attendanceLeave.setProcessInstanceId("leave-process");
        attendanceLeaveMapper.insert(attendanceLeave);

        // 调用
        attendanceLeaveService.updateLeaveApprovalStatus(
                attendanceLeave.getId(), "leave-process", BpmProcessInstanceStatusEnum.APPROVE.getStatus(), "同意");

        // 断言
        HrmAttendanceLeaveDO result = attendanceLeaveMapper.selectById(attendanceLeave.getId());
        assertEquals(BpmProcessInstanceStatusEnum.APPROVE.getStatus(), result.getApprovalStatus());
        assertEquals("同意", result.getApprovalReason());
        assertNotNull(result.getApprovalTime());

        // 重复终态回调不能覆盖首次审批结果
        attendanceLeaveService.updateLeaveApprovalStatus(
                attendanceLeave.getId(), "leave-process",
                BpmProcessInstanceStatusEnum.REJECT.getStatus(), "驳回");
        result = attendanceLeaveMapper.selectById(attendanceLeave.getId());
        assertEquals(BpmProcessInstanceStatusEnum.APPROVE.getStatus(), result.getApprovalStatus());
        assertEquals("同意", result.getApprovalReason());
    }

    @Test
    public void testUpdateLeaveApprovalStatus_processInstanceNotMatch() {
        // mock 数据
        Long employeeId = randomLongId();
        HrmAttendanceLeaveDO attendanceLeave = randomAttendanceLeaveDO(employeeId, "年假",
                LocalDateTime.of(2026, 8, 1, 9, 0), BpmProcessInstanceStatusEnum.RUNNING.getStatus());
        attendanceLeave.setProcessInstanceId("current-process");
        attendanceLeaveMapper.insert(attendanceLeave);

        // 调用
        attendanceLeaveService.updateLeaveApprovalStatus(
                attendanceLeave.getId(), "expired-process", BpmProcessInstanceStatusEnum.APPROVE.getStatus(), "同意");

        // 断言
        HrmAttendanceLeaveDO result = attendanceLeaveMapper.selectById(attendanceLeave.getId());
        assertEquals(BpmProcessInstanceStatusEnum.RUNNING.getStatus(), result.getApprovalStatus());
        assertEquals("current-process", result.getProcessInstanceId());
    }

    @Test
    public void testGetAttendanceLeavePage() {
        // mock 数据
        HrmEmployeeDO employee = new HrmEmployeeDO().setId(randomLongId()).setName("张三");
        HrmAttendanceLeaveDO leave = randomAttendanceLeaveDO(employee.getId(), "年假",
                LocalDateTime.of(2026, 8, 1, 9, 0), BpmProcessInstanceStatusEnum.RUNNING.getStatus());
        attendanceLeaveMapper.insert(leave);
        when(employeeService.getEmployeeList(any())).thenReturn(Collections.singletonList(employee));

        // 准备参数
        HrmAttendanceLeavePageReqVO reqVO = new HrmAttendanceLeavePageReqVO();
        reqVO.setEmployeeId(employee.getId());

        // 调用
        HrmAttendanceLeaveDO result = attendanceLeaveService.getAttendanceLeavePage(reqVO).getList().get(0);

        // 断言
        assertEquals(leave.getId(), result.getId());
    }

    @Test
    public void testGetAttendanceLeaveListByEmployeeId() {
        // mock 数据
        Long employeeId = randomLongId();
        LocalDateTime beginTime = LocalDateTime.of(2026, 8, 1, 0, 0);
        HrmAttendanceLeaveDO approvedLeave = randomAttendanceLeaveDO(employeeId, "年假",
                beginTime.plusHours(9), BpmProcessInstanceStatusEnum.APPROVE.getStatus());
        attendanceLeaveMapper.insert(approvedLeave);
        HrmAttendanceLeaveDO secondApprovedLeave = randomAttendanceLeaveDO(employeeId, "病假",
                beginTime.plusDays(1).plusHours(9), BpmProcessInstanceStatusEnum.APPROVE.getStatus());
        attendanceLeaveMapper.insert(secondApprovedLeave);
        attendanceLeaveMapper.insert(randomAttendanceLeaveDO(employeeId, "事假",
                beginTime.plusDays(2).plusHours(9), BpmProcessInstanceStatusEnum.RUNNING.getStatus()));
        attendanceLeaveMapper.insert(randomAttendanceLeaveDO(randomLongId(), "年假",
                beginTime.plusHours(9), BpmProcessInstanceStatusEnum.APPROVE.getStatus()));

        // 调用
        List<HrmAttendanceLeaveDO> leaves =
                attendanceLeaveService.getAttendanceLeaveListByEmployeeId(employeeId);
        List<HrmAttendanceLeaveDO> effectiveLeaves =
                attendanceLeaveService.getEffectiveAttendanceLeaveListByEmployeeId(employeeId);

        // 断言
        assertEquals(3, leaves.size());
        assertEquals(2, effectiveLeaves.size());
        assertEquals(secondApprovedLeave.getId(), effectiveLeaves.get(0).getId());
        assertEquals(approvedLeave.getId(), effectiveLeaves.get(1).getId());
    }

    @Test
    public void testGetEffectiveAttendanceLeaveListByEmployeeIdsAndTimeRange() {
        // mock 数据
        Long firstEmployeeId = randomLongId();
        Long secondEmployeeId = randomLongId();
        LocalDateTime beginTime = LocalDateTime.of(2026, 8, 1, 0, 0);
        LocalDateTime endTime = beginTime.plusMonths(1);
        HrmAttendanceLeaveDO firstLeave = randomAttendanceLeaveDO(firstEmployeeId, "年假",
                beginTime.plusHours(9), BpmProcessInstanceStatusEnum.APPROVE.getStatus());
        attendanceLeaveMapper.insert(firstLeave);
        HrmAttendanceLeaveDO secondLeave = randomAttendanceLeaveDO(secondEmployeeId, "病假",
                endTime.minusDays(1).plusHours(9), BpmProcessInstanceStatusEnum.APPROVE.getStatus());
        attendanceLeaveMapper.insert(secondLeave);
        // 请假开始时间早于查询左边界，但结束时间落在范围内，仍属于双闭区间重叠
        HrmAttendanceLeaveDO boundaryOverlapLeave = randomAttendanceLeaveDO(firstEmployeeId, "调休",
                beginTime.minusHours(4), BpmProcessInstanceStatusEnum.APPROVE.getStatus());
        attendanceLeaveMapper.insert(boundaryOverlapLeave);
        // 双闭区间首尾相接也属于重叠
        HrmAttendanceLeaveDO leftBoundaryLeave = randomAttendanceLeaveDO(firstEmployeeId, "事假",
                beginTime.minusHours(8), BpmProcessInstanceStatusEnum.APPROVE.getStatus());
        attendanceLeaveMapper.insert(leftBoundaryLeave);
        HrmAttendanceLeaveDO rightBoundaryLeave = randomAttendanceLeaveDO(firstEmployeeId, "事假",
                endTime, BpmProcessInstanceStatusEnum.APPROVE.getStatus());
        attendanceLeaveMapper.insert(rightBoundaryLeave);
        attendanceLeaveMapper.insert(randomAttendanceLeaveDO(firstEmployeeId, "事假",
                beginTime.minusDays(2), BpmProcessInstanceStatusEnum.APPROVE.getStatus()));
        attendanceLeaveMapper.insert(randomAttendanceLeaveDO(firstEmployeeId, "事假",
                beginTime.plusDays(1), BpmProcessInstanceStatusEnum.RUNNING.getStatus()));

        // 调用
        List<HrmAttendanceLeaveDO> result =
                attendanceLeaveService.getEffectiveAttendanceLeaveListByEmployeeIdsAndTimeRange(
                        Arrays.asList(firstEmployeeId, secondEmployeeId),
                        new LocalDateTime[]{beginTime, endTime});

        // 断言
        assertEquals(5, result.size());
        assertEquals(rightBoundaryLeave.getId(), result.get(0).getId());
        assertEquals(secondLeave.getId(), result.get(1).getId());
        assertEquals(firstLeave.getId(), result.get(2).getId());
        assertEquals(boundaryOverlapLeave.getId(), result.get(3).getId());
        assertEquals(leftBoundaryLeave.getId(), result.get(4).getId());
    }

    @Test
    public void testGetAttendanceLeaveList() {
        // mock 数据
        Long employeeId = randomLongId();
        LocalDateTime startTime = LocalDateTime.of(2026, 8, 1, 9, 0);
        attendanceLeaveMapper.insert(randomAttendanceLeaveDO(
                employeeId, "年假", startTime, BpmProcessInstanceStatusEnum.APPROVE.getStatus()));
        attendanceLeaveMapper.insert(randomAttendanceLeaveDO(
                employeeId, "年假", startTime.plusDays(1), BpmProcessInstanceStatusEnum.RUNNING.getStatus()));
        attendanceLeaveMapper.insert(randomAttendanceLeaveDO(
                employeeId, "病假", startTime.plusDays(2), BpmProcessInstanceStatusEnum.APPROVE.getStatus()));
        when(employeeService.getEmployeeList(any())).thenReturn(
                Collections.singletonList(new HrmEmployeeDO().setId(employeeId)));

        // 准备参数
        HrmAttendanceLeavePageReqVO reqVO = new HrmAttendanceLeavePageReqVO();
        reqVO.setEmployeeId(employeeId);
        reqVO.setTypes(Collections.singletonList("年假"));
        reqVO.setApprovalStatus(BpmProcessInstanceStatusEnum.APPROVE.getStatus());

        // 调用
        List<HrmAttendanceLeaveDO> leaves = attendanceLeaveService.getAttendanceLeaveList(reqVO);

        // 断言
        assertEquals(1, leaves.size());
    }

    // ========== 随机对象 ==========

    private static HrmAttendanceLeaveDO randomAttendanceLeaveDO(
            Long employeeId, String type, LocalDateTime startTime, Integer approvalStatus) {
        return randomPojo(HrmAttendanceLeaveDO.class, leave -> leave.setId(null).setEmployeeId(employeeId)
                .setType(type).setStartTime(startTime).setEndTime(startTime.plusHours(8))
                .setDay(new BigDecimal("1.00")).setApprovalStatus(approvalStatus)
                .setProcessInstanceId(employeeId + "-" + startTime).setDeleted(false));
    }

}

package cn.iocoder.yudao.module.bpm.service.oa;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.util.validation.ValidationUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.controller.admin.oa.vo.BpmOALeaveCreateReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.oa.vo.BpmOALeaveUpdateReqVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.oa.BpmOALeaveDO;
import cn.iocoder.yudao.module.bpm.dal.mysql.oa.BpmOALeaveMapper;
import cn.iocoder.yudao.module.bpm.enums.task.BpmTaskStatusEnum;
import jakarta.annotation.Resource;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link BpmOALeaveServiceImpl} 的单元测试类
 *
 * @author HUIHUI
 */
@Import(BpmOALeaveServiceImpl.class)
public class BpmOALeaveServiceImplTest extends BaseDbUnitTest {

    @Resource
    private BpmOALeaveServiceImpl leaveService;
    @Resource
    private BpmOALeaveMapper leaveMapper;

    @MockitoBean
    private BpmProcessInstanceApi processInstanceApi;

    @Test
    public void testCreateLeave_success() {
        // 准备参数
        BpmOALeaveCreateReqVO reqVO = new BpmOALeaveCreateReqVO().setType(1).setReason("休假")
                .setStartTime(buildTime(2026, 10, 1)).setEndTime(buildTime(2026, 10, 3))
                .setStartUserSelectAssignees(Map.of("approve", List.of(2L)));
        when(processInstanceApi.createProcessInstance(eq(1L), any())).thenReturn("process-instance-1");

        // 调用
        Long leaveId = leaveService.createLeave(1L, reqVO);
        // 断言
        BpmOALeaveDO leave = leaveMapper.selectById(leaveId);
        assertEquals(1L, leave.getUserId());
        assertEquals(2L, leave.getDay());
        assertEquals(BpmTaskStatusEnum.RUNNING.getStatus(), leave.getStatus());
        assertEquals("process-instance-1", leave.getProcessInstanceId());
        ArgumentCaptor<BpmProcessInstanceCreateReqDTO> captor = ArgumentCaptor.forClass(BpmProcessInstanceCreateReqDTO.class);
        verify(processInstanceApi).createProcessInstance(eq(1L), captor.capture());
        assertEquals("oa_leave", captor.getValue().getProcessDefinitionKey());
        assertEquals(String.valueOf(leaveId), captor.getValue().getBusinessKey());
        assertEquals(2L, captor.getValue().getVariables().get("day"));
        assertEquals(reqVO.getStartUserSelectAssignees(), captor.getValue().getStartUserSelectAssignees());
    }

    @Test
    public void testCreateLeave_processCreateFails_rollback() {
        // 准备参数
        BpmOALeaveCreateReqVO reqVO = new BpmOALeaveCreateReqVO().setType(1).setReason("休假")
                .setStartTime(buildTime(2026, 10, 1)).setEndTime(buildTime(2026, 10, 3));
        when(processInstanceApi.createProcessInstance(eq(1L), any())).thenThrow(new IllegalStateException("流程启动失败"));

        // 调用，并断言异常
        assertThrows(IllegalStateException.class, () -> leaveService.createLeave(1L, reqVO));

        // 断言
        assertEquals(0, leaveMapper.selectCount());
    }

    @Test
    public void testUpdateLeaveStatus_success() {
        // 准备参数
        BpmOALeaveDO leave = randomPojo(BpmOALeaveDO.class);
        leaveMapper.insert(leave);

        // 调用
        leaveService.updateLeaveStatus(leave.getId(), BpmTaskStatusEnum.APPROVE.getStatus());
        // 断言
        assertEquals(BpmTaskStatusEnum.APPROVE.getStatus(), leaveMapper.selectById(leave.getId()).getStatus());
    }

    @Test
    public void testUpdateLeaveStatus_notExists() {
        // 调用，并断言异常
        assertServiceException(() -> leaveService.updateLeaveStatus(1L, BpmTaskStatusEnum.APPROVE.getStatus()),
                OA_LEAVE_NOT_EXISTS);
    }

    @Test
    public void testUpdateLeave_success() {
        // mock 数据：审批中的请假申请，请假 2 天
        BpmOALeaveDO leave = insertLeave(BpmTaskStatusEnum.RUNNING.getStatus());
        // 准备参数：只修改结束时间，延长到 4 天
        BpmOALeaveUpdateReqVO reqVO = new BpmOALeaveUpdateReqVO().setId(leave.getId())
                .setEndTime(leave.getStartTime().plusDays(4));

        // 调用
        leaveService.updateLeave(reqVO);
        // 断言：只更新修改的字段，并重新计算请假天数
        BpmOALeaveDO dbLeave = leaveMapper.selectById(leave.getId());
        assertEquals(reqVO.getEndTime(), dbLeave.getEndTime());
        assertEquals(leave.getStartTime(), dbLeave.getStartTime());
        assertEquals(leave.getType(), dbLeave.getType());
        assertEquals(leave.getReason(), dbLeave.getReason());
        assertEquals(4L, dbLeave.getDay());
        assertEquals(BpmTaskStatusEnum.RUNNING.getStatus(), dbLeave.getStatus());
        assertEquals(leave.getProcessInstanceId(), dbLeave.getProcessInstanceId());
        // 断言：流程实例从请假申请读取，同步后端计算的请假天数
        verify(processInstanceApi).updateProcessInstanceVariables("pi-1", Map.of("day", 4L));
    }

    @Test
    public void testUpdateLeave_onlyReason() {
        // mock 数据：审批中的请假申请，请假 2 天
        BpmOALeaveDO leave = insertLeave(BpmTaskStatusEnum.RUNNING.getStatus());

        // 调用：只修改原因
        leaveService.updateLeave(new BpmOALeaveUpdateReqVO().setId(leave.getId()).setReason("新的原因"));
        // 断言：只更新原因，不更新请假时间、天数，也不同步流程变量
        BpmOALeaveDO dbLeave = leaveMapper.selectById(leave.getId());
        assertEquals("新的原因", dbLeave.getReason());
        assertEquals(leave.getStartTime(), dbLeave.getStartTime());
        assertEquals(leave.getEndTime(), dbLeave.getEndTime());
        assertEquals(2L, dbLeave.getDay());
        verify(processInstanceApi, never()).updateProcessInstanceVariables(any(), any());
    }

    @Test
    public void testUpdateLeave_notExists() {
        assertServiceException(() -> leaveService.updateLeave(new BpmOALeaveUpdateReqVO()
                .setId(-1L).setReason("原因")), OA_LEAVE_NOT_EXISTS);
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testUpdateLeave_notRunning() {
        // mock 数据：已经审批通过
        BpmOALeaveDO leave = insertLeave(BpmTaskStatusEnum.APPROVE.getStatus());

        // 调用，并断言异常
        assertServiceException(() -> leaveService.updateLeave(new BpmOALeaveUpdateReqVO()
                .setId(leave.getId()).setReason("新的原因")), OA_LEAVE_UPDATE_NOT_ALLOWED);
        assertEquals("原因", leaveMapper.selectById(leave.getId()).getReason());
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testUpdateLeave_noFields() {
        // mock 数据
        BpmOALeaveDO leave = insertLeave(BpmTaskStatusEnum.RUNNING.getStatus());

        // 调用：没有修改的字段时，直接成功
        leaveService.updateLeave(new BpmOALeaveUpdateReqVO().setId(leave.getId()));
        // 断言：请假申请不变，也不同步流程变量
        BpmOALeaveDO dbLeave = leaveMapper.selectById(leave.getId());
        assertEquals(leave.getReason(), dbLeave.getReason());
        assertEquals(leave.getEndTime(), dbLeave.getEndTime());
        assertEquals(2L, dbLeave.getDay());
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testUpdateLeave_endTimeBeforeStartTime() {
        // mock 数据
        BpmOALeaveDO leave = insertLeave(BpmTaskStatusEnum.RUNNING.getStatus());

        // 调用，并断言异常：只修改开始时间时，与原来的结束时间比较
        ServiceException ex = assertThrows(ServiceException.class, () -> leaveService.updateLeave(
                new BpmOALeaveUpdateReqVO().setId(leave.getId())
                        .setStartTime(leave.getEndTime().plusDays(1))));
        assertEquals(GlobalErrorCodeConstants.BAD_REQUEST.getCode(), ex.getCode());
        BpmOALeaveDO dbLeave = leaveMapper.selectById(leave.getId());
        assertEquals(leave.getStartTime(), dbLeave.getStartTime());
        assertEquals(2L, dbLeave.getDay());
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testUpdateReqVO_validate() {
        // 断言：开始、结束时间都修改时，结束时间需要在开始时间之后
        LocalDateTime startTime = LocalDateTime.of(2026, 11, 1, 9, 0);
        assertThrows(ConstraintViolationException.class, () -> ValidationUtils.validate(new BpmOALeaveUpdateReqVO()
                .setId(1L).setStartTime(startTime).setEndTime(startTime.minusDays(1))));
        // 断言：修改原因时，不能为空白
        assertThrows(ConstraintViolationException.class, () -> ValidationUtils.validate(new BpmOALeaveUpdateReqVO()
                .setId(1L).setReason("  ")));
        // 断言：只修改部分字段时，校验通过
        assertDoesNotThrow(() -> ValidationUtils.validate(new BpmOALeaveUpdateReqVO()
                .setId(1L).setStartTime(startTime)));
    }

    private BpmOALeaveDO insertLeave(Integer status) {
        LocalDateTime startTime = LocalDateTime.of(2026, 11, 1, 9, 0);
        BpmOALeaveDO leave = BpmOALeaveDO.builder().userId(1L).type(1).reason("原因")
                .startTime(startTime).endTime(startTime.plusDays(2)).day(2L)
                .status(status).processInstanceId("pi-1").build();
        leaveMapper.insert(leave);
        return leave;
    }

}

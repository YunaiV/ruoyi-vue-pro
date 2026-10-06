package cn.iocoder.yudao.module.bpm.service.oa;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.controller.admin.oa.vo.BpmOALeaveCreateReqVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.oa.BpmOALeaveDO;
import cn.iocoder.yudao.module.bpm.dal.mysql.oa.BpmOALeaveMapper;
import cn.iocoder.yudao.module.bpm.enums.task.BpmTaskStatusEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.OA_LEAVE_NOT_EXISTS;
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

}

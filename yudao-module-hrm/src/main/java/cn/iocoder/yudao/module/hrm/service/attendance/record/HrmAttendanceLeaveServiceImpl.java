package cn.iocoder.yudao.module.hrm.service.attendance.record;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.leave.HrmAttendanceLeaveCancelReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.leave.HrmAttendanceLeaveCreateReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.leave.HrmAttendanceLeavePageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeListReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.record.HrmAttendanceLeaveDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.attendance.record.HrmAttendanceLeaveMapper;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.ATTENDANCE_LEAVE_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.ATTENDANCE_LEAVE_STATUS_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.ATTENDANCE_LEAVE_TIME_CONFLICT;

/**
 * HRM 考勤请假 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmAttendanceLeaveServiceImpl implements HrmAttendanceLeaveService {

    public static final String LEAVE_PROCESS_KEY = "hrm_attendance_leave";

    @Resource
    private HrmAttendanceLeaveMapper attendanceLeaveMapper;
    @Resource
    private HrmEmployeeService employeeService;
    @Resource
    private BpmProcessInstanceApi processInstanceApi;

    @Override
    @SuppressWarnings("ExtractMethodRecommender")
    @Transactional(rollbackFor = Exception.class)
    public Long createLeave(Long userId, HrmAttendanceLeaveCreateReqVO reqVO) {
        // 1.1 校验当前用户已关联员工
        HrmEmployeeDO employee = employeeService.validateEmployeeBySelf(userId);
        // 1.2 锁定员工，避免同一员工并发创建重叠请假
        employeeService.validateEmployeeExistsForUpdate(employee.getId());
        // 1.3 校验请假时间未与审批中、已通过的申请重叠
        Long conflictCount = attendanceLeaveMapper.selectCountByEmployeeIdAndTimeRangeAndApprovalStatusIn(
                employee.getId(), new LocalDateTime[]{reqVO.getStartTime(), reqVO.getEndTime()},
                Arrays.asList(BpmProcessInstanceStatusEnum.RUNNING.getStatus(), BpmProcessInstanceStatusEnum.APPROVE.getStatus()));
        if (conflictCount > 0) {
            throw exception(ATTENDANCE_LEAVE_TIME_CONFLICT);
        }

        // 2. 创建请假记录
        HrmAttendanceLeaveDO attendanceLeave = BeanUtils.toBean(reqVO, HrmAttendanceLeaveDO.class)
                .setEmployeeId(employee.getId()).setApprovalStatus(BpmProcessInstanceStatusEnum.RUNNING.getStatus());
        attendanceLeaveMapper.insert(attendanceLeave);

        // 3. 构建流程变量并发起审批流程
        Map<String, Object> variables = new HashMap<>();
        variables.put("employeeDeptId", employee.getDeptId());
        variables.put("employeeId", employee.getId());
        variables.put("employeeName", employee.getName());
        variables.put("leaveType", reqVO.getType());
        variables.put("startTime", reqVO.getStartTime().toString());
        variables.put("endTime", reqVO.getEndTime().toString());
        variables.put("day", reqVO.getDay());
        variables.put("reason", reqVO.getReason());
        BpmProcessInstanceCreateReqDTO processInstanceCreateReqDTO = new BpmProcessInstanceCreateReqDTO();
        processInstanceCreateReqDTO.setProcessDefinitionKey(LEAVE_PROCESS_KEY);
        processInstanceCreateReqDTO.setBusinessKey(String.valueOf(attendanceLeave.getId()));
        processInstanceCreateReqDTO.setVariables(variables);
        String processInstanceId = processInstanceApi.createProcessInstance(userId, processInstanceCreateReqDTO);
        attendanceLeaveMapper.updateById(new HrmAttendanceLeaveDO().setId(attendanceLeave.getId())
                .setProcessInstanceId(processInstanceId));
        return attendanceLeave.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelLeave(Long userId, HrmAttendanceLeaveCancelReqVO reqVO) {
        // 1. 校验请假申请属于当前员工，且处于审批中
        HrmEmployeeDO employee = employeeService.validateEmployeeBySelf(userId);
        HrmAttendanceLeaveDO attendanceLeave = validateAttendanceLeaveExists(reqVO.getId());
        if (ObjUtil.notEqual(attendanceLeave.getEmployeeId(), employee.getId())) {
            throw exception(ATTENDANCE_LEAVE_NOT_EXISTS);
        }
        if (ObjUtil.notEqual(attendanceLeave.getApprovalStatus(), BpmProcessInstanceStatusEnum.RUNNING.getStatus())
                || StrUtil.isBlank(attendanceLeave.getProcessInstanceId())) {
            throw exception(ATTENDANCE_LEAVE_STATUS_INVALID);
        }

        // 2. 取消审批流程并更新请假审批状态
        processInstanceApi.cancelProcessInstanceByStartUser(
                userId, attendanceLeave.getProcessInstanceId(), reqVO.getReason());
        updateLeaveApprovalStatus(attendanceLeave.getId(), attendanceLeave.getProcessInstanceId(),
                BpmProcessInstanceStatusEnum.CANCEL.getStatus(), reqVO.getReason());
    }

    @Override
    public List<HrmAttendanceLeaveDO> getMyAttendanceLeaveList(Long userId) {
        return attendanceLeaveMapper.selectListByEmployeeId(employeeService.validateEmployeeBySelf(userId).getId());
    }

    @Override
    public HrmAttendanceLeaveDO getAttendanceLeave(Long id) {
        return attendanceLeaveMapper.selectById(id);
    }

    @Override
    public PageResult<HrmAttendanceLeaveDO> getAttendanceLeavePage(HrmAttendanceLeavePageReqVO reqVO) {
        // 1. 解析员工筛选条件
        Collection<Long> employeeIds = resolveEmployeeIds(reqVO.getEmployeeId(), reqVO.getDeptIds(),
                reqVO.getEmployeeKeyword());
        if (employeeIds != null && CollUtil.isEmpty(employeeIds)) {
            return PageResult.empty();
        }
        reqVO.setEmployeeIds(employeeIds);

        // 2. 查询请假分页
        return attendanceLeaveMapper.selectPage(reqVO);
    }

    @Override
    public List<HrmAttendanceLeaveDO> getAttendanceLeaveList(HrmAttendanceLeavePageReqVO reqVO) {
        // 1. 解析员工筛选条件
        Collection<Long> employeeIds = resolveEmployeeIds(reqVO.getEmployeeId(), reqVO.getDeptIds(),
                reqVO.getEmployeeKeyword());
        if (employeeIds != null && CollUtil.isEmpty(employeeIds)) {
            return Collections.emptyList();
        }
        reqVO.setEmployeeIds(employeeIds);

        // 2. 查询请假列表
        return attendanceLeaveMapper.selectList(reqVO);
    }

    @Override
    public List<HrmAttendanceLeaveDO> getAttendanceLeaveListByEmployeeId(Long employeeId) {
        return attendanceLeaveMapper.selectListByEmployeeId(employeeId);
    }

    @Override
    public List<HrmAttendanceLeaveDO> getEffectiveAttendanceLeaveListByEmployeeId(Long employeeId) {
        return attendanceLeaveMapper.selectListByEmployeeIdAndApprovalStatus(employeeId, BpmProcessInstanceStatusEnum.APPROVE.getStatus());
    }

    @Override
    public List<HrmAttendanceLeaveDO> getEffectiveAttendanceLeaveListByEmployeeIdsAndTimeRange(
            Collection<Long> employeeIds, LocalDateTime[] leaveTimes) {
        if (CollUtil.isEmpty(employeeIds)) {
            return Collections.emptyList();
        }
        return attendanceLeaveMapper.selectListByEmployeeIdsAndTimeRangeAndApprovalStatus(
                employeeIds, leaveTimes, BpmProcessInstanceStatusEnum.APPROVE.getStatus());
    }

    @Override
    public void updateLeaveApprovalStatus(
            Long id, String processInstanceId, Integer status, String reason) {
        // 1. 仅处理当前审批流程的结束状态
        if (!BpmProcessInstanceStatusEnum.isProcessEndStatus(status)) {
            return;
        }
        HrmAttendanceLeaveDO attendanceLeave = validateAttendanceLeaveExists(id);
        if ((attendanceLeave.getProcessInstanceId() != null
                && ObjUtil.notEqual(attendanceLeave.getProcessInstanceId(), processInstanceId))
                || ObjUtil.notEqual(attendanceLeave.getApprovalStatus(), BpmProcessInstanceStatusEnum.RUNNING.getStatus())) {
            return;
        }

        // 2. 仅允许当前审批中的流程写入一次终态
        attendanceLeaveMapper.updateApprovalResultByIdAndProcessInstanceIdAndApprovalStatus(
                id, processInstanceId, BpmProcessInstanceStatusEnum.RUNNING.getStatus(),
                new HrmAttendanceLeaveDO().setProcessInstanceId(processInstanceId)
                        .setApprovalStatus(status).setApprovalTime(LocalDateTime.now()).setApprovalReason(reason));
    }

    private HrmAttendanceLeaveDO validateAttendanceLeaveExists(Long id) {
        HrmAttendanceLeaveDO attendanceLeave = attendanceLeaveMapper.selectById(id);
        if (attendanceLeave == null) {
            throw exception(ATTENDANCE_LEAVE_NOT_EXISTS);
        }
        return attendanceLeave;
    }

    private Collection<Long> resolveEmployeeIds(
            Long employeeId, List<Long> deptIds, String employeeKeyword) {
        if (employeeId == null && CollUtil.isEmpty(deptIds) && StrUtil.isBlank(employeeKeyword)) {
            return null;
        }
        HrmEmployeeListReqVO employeeReqVO = new HrmEmployeeListReqVO()
                .setIds(employeeId == null ? null : Collections.singletonList(employeeId))
                .setSearch(employeeKeyword).setDeptIds(deptIds);
        return convertList(employeeService.getEmployeeList(employeeReqVO), HrmEmployeeDO::getId);
    }

}

package cn.iocoder.yudao.module.bpm.service.oa;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.controller.admin.oa.vo.BpmOALeaveCreateReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.oa.vo.BpmOALeavePageReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.oa.vo.BpmOALeaveUpdateReqVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.oa.BpmOALeaveDO;
import cn.iocoder.yudao.module.bpm.dal.mysql.oa.BpmOALeaveMapper;
import cn.iocoder.yudao.module.bpm.enums.task.BpmTaskStatusEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.*;

/**
 * OA 请假申请 Service 实现类
 *
 * @author jason
 * @author 芋道源码
 */
@Service
@Validated
public class BpmOALeaveServiceImpl implements BpmOALeaveService {

    /**
     * OA 请假对应的流程定义 KEY
     */
    public static final String PROCESS_KEY = "oa_leave";

    @Resource
    private BpmOALeaveMapper leaveMapper;

    @Resource
    private BpmProcessInstanceApi processInstanceApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createLeave(Long userId, BpmOALeaveCreateReqVO createReqVO) {
        // 插入 OA 请假单
        long day = LocalDateTimeUtil.between(createReqVO.getStartTime(), createReqVO.getEndTime()).toDays();
        BpmOALeaveDO leave = BeanUtils.toBean(createReqVO, BpmOALeaveDO.class)
                .setUserId(userId).setDay(day).setStatus(BpmTaskStatusEnum.RUNNING.getStatus());
        leaveMapper.insert(leave);

        // 发起 BPM 流程
        Map<String, Object> processInstanceVariables = new HashMap<>();
        processInstanceVariables.put("day", day);
        String processInstanceId = processInstanceApi.createProcessInstance(userId,
                new BpmProcessInstanceCreateReqDTO().setProcessDefinitionKey(PROCESS_KEY)
                        .setVariables(processInstanceVariables).setBusinessKey(String.valueOf(leave.getId()))
                        .setStartUserSelectAssignees(createReqVO.getStartUserSelectAssignees()));

        // 将工作流的编号，更新到 OA 请假单中
        leaveMapper.updateById(new BpmOALeaveDO().setId(leave.getId()).setProcessInstanceId(processInstanceId));
        return leave.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLeave(BpmOALeaveUpdateReqVO updateReqVO) {
        // 1.1 校验请假申请存在，并且审批中。流程实例从请假申请中读取，不使用前端传递的参数
        BpmOALeaveDO leave = leaveMapper.selectById(updateReqVO.getId());
        if (leave == null) {
            throw exception(OA_LEAVE_NOT_EXISTS);
        }
        if (ObjUtil.notEqual(leave.getStatus(), BpmTaskStatusEnum.RUNNING.getStatus())
                || StrUtil.isEmpty(leave.getProcessInstanceId())) {
            throw exception(OA_LEAVE_UPDATE_NOT_ALLOWED);
        }
        // 1.2 没有修改的字段（为 null 时表示不修改），直接返回
        if (ObjUtil.isAllEmpty(updateReqVO.getType(), updateReqVO.getReason(),
                updateReqVO.getStartTime(), updateReqVO.getEndTime())) {
            return;
        }
        // 1.3 修改了请假时间时，校验请假时间，并重新计算请假天数：只修改开始、结束时间中的一个时，使用另一个原来的值
        // 补充说明：只修改类型、原因时，不更新请假天数，避免覆盖并发修改的请假时间
        BpmOALeaveDO updateObj = BeanUtils.toBean(updateReqVO, BpmOALeaveDO.class);
        Map<String, Object> variables = Map.of();
        if (updateReqVO.getStartTime() != null || updateReqVO.getEndTime() != null) {
            LocalDateTime startTime = ObjUtil.defaultIfNull(updateReqVO.getStartTime(), leave.getStartTime());
            LocalDateTime endTime = ObjUtil.defaultIfNull(updateReqVO.getEndTime(), leave.getEndTime());
            if (endTime.isBefore(startTime)) {
                throw invalidParamException("结束时间，需要在开始时间之后");
            }
            long day = LocalDateTimeUtil.between(startTime, endTime).toDays();
            updateObj.setDay(day);
            variables = Map.of("day", day); // 请假天数用于条件分支，所以同步到流程变量
        }

        // 2. 更新请假申请：按状态条件更新，避免流程结束后仍被修改
        int updateCount = leaveMapper.updateByIdAndStatus(leave.getId(), BpmTaskStatusEnum.RUNNING.getStatus(), updateObj);
        if (updateCount == 0) {
            throw exception(OA_LEAVE_UPDATE_NOT_ALLOWED);
        }

        // 3. 同步流程变量，审批结果由之后的【通过】处理
        if (CollUtil.isNotEmpty(variables)) {
            processInstanceApi.updateProcessInstanceVariables(leave.getProcessInstanceId(), variables);
        }
    }

    @Override
    public void updateLeaveStatus(Long id, Integer status) {
        validateLeaveExists(id);
        leaveMapper.updateById(new BpmOALeaveDO().setId(id).setStatus(status));
    }

    private void validateLeaveExists(Long id) {
        if (leaveMapper.selectById(id) == null) {
            throw exception(OA_LEAVE_NOT_EXISTS);
        }
    }

    @Override
    public BpmOALeaveDO getLeave(Long id) {
        return leaveMapper.selectById(id);
    }

    @Override
    public PageResult<BpmOALeaveDO> getLeavePage(Long userId, BpmOALeavePageReqVO pageReqVO) {
        return leaveMapper.selectPage(userId, pageReqVO);
    }

}

package cn.iocoder.yudao.adminserver.modules.activiti.service.oa.impl;

import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.adminserver.modules.activiti.controller.oa.vo.*;
import cn.iocoder.yudao.adminserver.modules.activiti.dal.dataobject.oa.OaLeaveDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.adminserver.modules.activiti.convert.oa.OaLeaveConvert;
import cn.iocoder.yudao.adminserver.modules.activiti.dal.mysql.oa.OaLeaveMapper;
import cn.iocoder.yudao.adminserver.modules.activiti.service.oa.OaLeaveService;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.adminserver.modules.activiti.enums.OaErrorCodeConstants.*;

/**
 * 请假申请 Service 实现类
 *
 * @author 芋艿
 */
@Service
@Validated
public class OaLeaveServiceImpl implements OaLeaveService {

    @Resource
    private OaLeaveMapper leaveMapper;

    @Resource
    private RuntimeService runtimeService;

    @Resource
    private org.activiti.engine.TaskService activitiTaskService;

    @Resource
    private TaskRuntime taskRuntime;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createLeave(OaLeaveCreateReqVO createReqVO) {
        // 插入
        OaLeaveDO leave = OaLeaveConvert.INSTANCE.convert(createReqVO);
        leave.setStatus(1);
        leave.setUserId(SecurityFrameworkUtils.getLoginUser().getUsername());
        leaveMapper.insert(leave);

        Map<String, Object> variables = new HashMap<>();
        //如何得到部门领导人， 暂时写死
        variables.put("deptLeader", "admin");
        final Long id = leave.getId();
        String businessKey = String.valueOf(id);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(createReqVO.getProcessKey(), businessKey, variables);

        final String processInstanceId = processInstance.getProcessInstanceId();


        UpdateWrapper<OaLeaveDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id);
        OaLeaveDO updateDo = new OaLeaveDO();
        updateDo.setProcessInstanceId(processInstanceId);
        leaveMapper.update(updateDo, updateWrapper);
        // 返回
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLeave(OaLeaveUpdateReqVO updateReqVO) {

        // 校验存在
        this.validateLeaveExists(updateReqVO.getId());

        final Task task = taskRuntime.task(updateReqVO.getTaskId());
        activitiTaskService.addComment(task.getId(), task.getProcessInstanceId(), updateReqVO.getComment());
        Map<String, Object> variables = updateReqVO.getVariables();

        //如何得到部门领导人， 暂时写死
        variables.put("deptLeader", "admin");
        taskRuntime.complete(TaskPayloadBuilder.complete().withTaskId(task.getId())
                .withVariables(variables)
                .build());
        final Object reApply = variables.get("reApply");
        if((reApply instanceof Boolean) && (Boolean)reApply){
            // 更新 表单
            OaLeaveDO updateObj = OaLeaveConvert.INSTANCE.convert(updateReqVO);
            leaveMapper.updateById(updateObj);
        }


    }

    @Override
    public void deleteLeave(Long id) {
        // 校验存在
        this.validateLeaveExists(id);
        // 删除
        leaveMapper.deleteById(id);
    }

    private void validateLeaveExists(Long id) {
        if (leaveMapper.selectById(id) == null) {
            throw exception(LEAVE_NOT_EXISTS);
        }
    }

    @Override
    public OaLeaveDO getLeave(Long id) {
        return leaveMapper.selectById(id);
    }

    @Override
    public List<OaLeaveDO> getLeaveList(Collection<Long> ids) {
        return leaveMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<OaLeaveDO> getLeavePage(OaLeavePageReqVO pageReqVO) {
        return leaveMapper.selectPage(pageReqVO);
    }

    @Override
    public List<OaLeaveDO> getLeaveList(OaLeaveExportReqVO exportReqVO) {
        return leaveMapper.selectList(exportReqVO);
    }

}

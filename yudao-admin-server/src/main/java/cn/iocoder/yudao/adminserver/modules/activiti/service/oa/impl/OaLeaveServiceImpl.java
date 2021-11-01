package cn.iocoder.yudao.adminserver.modules.activiti.service.oa.impl;

import cn.iocoder.yudao.adminserver.modules.activiti.controller.oa.vo.OALeaveCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.activiti.controller.oa.vo.OALeaveUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.activiti.controller.oa.vo.OALeaveExportReqVO;
import cn.iocoder.yudao.adminserver.modules.activiti.controller.oa.vo.OALeavePageReqVO;
import cn.iocoder.yudao.adminserver.modules.activiti.convert.oa.OALeaveConvert;
import cn.iocoder.yudao.adminserver.modules.activiti.dal.dataobject.oa.OALeaveDO;
import cn.iocoder.yudao.adminserver.modules.activiti.dal.mysql.oa.OaLeaveMapper;
import cn.iocoder.yudao.adminserver.modules.activiti.service.oa.OALeaveService;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.*;

import static cn.iocoder.yudao.adminserver.modules.activiti.enums.OAErrorCodeConstants.LEAVE_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 请假申请 Service 实现类
 *
 * @author 芋艿
 */
@Service
@Validated
public class OALeaveServiceImpl implements OALeaveService {

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
    public Long createLeave(OALeaveCreateReqVO createReqVO) {
        // 插入 OA 请假单
        OALeaveDO leave = OALeaveConvert.INSTANCE.convert(createReqVO);
        leave.setStatus(1);
        // TODO @jason：应该是存储 userId？？
        leave.setUserId(SecurityFrameworkUtils.getLoginUser().getUsername());
        leaveMapper.insert(leave);

        // 创建工作流
        Map<String, Object> variables = new HashMap<>();
        // 如何得到部门领导人，暂时写死
        variables.put("deptLeader", "admin"); // TODO @芋艿：需要部门的负责人
        Long id = leave.getId();
        String businessKey = String.valueOf(id);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(createReqVO.getProcessKey(), businessKey, variables);
        String processInstanceId = processInstance.getProcessInstanceId();

        // 将工作流的编号，更新到 OA 请假单中
        OALeaveDO updateDo = new OALeaveDO();
        updateDo.setProcessInstanceId(processInstanceId);
        updateDo.setId(id);
        leaveMapper.updateById(updateDo);
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLeave(OALeaveUpdateReqVO updateReqVO) {
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

        // TOTO @芋道源码 貌似 IDEA 会自动加上final（不需要加 final 哈。虽然是不变，但是代码比较少这么去写)
        Object reApply = variables.get("reApply");
        if(Objects.equals(reApply, true)){
            // 更新 表单
            OALeaveDO updateObj = OALeaveConvert.INSTANCE.convert(updateReqVO);
            leaveMapper.updateById(updateObj);
        }
    }

    @Override
    public void deleteLeave(Long id) {
        // 校验存在
        this.validateLeaveExists(id);
        // 删除
        leaveMapper.deleteById(id);
        // TODO @jason：需要调用 runtimeService 的 delete 方法，删除？？？
        // TOTO @芋道源码 目前页面暂时没有实现基于业务表单的删除, 该代码自动生成的。
        // TODO @芋道源码  我理解提交流程后，是不允许删除的？ ， 只能在流程处理中作废流程
    }

    private void validateLeaveExists(Long id) {
        if (leaveMapper.selectById(id) == null) {
            throw exception(LEAVE_NOT_EXISTS);
        }
    }

    @Override
    public OALeaveDO getLeave(Long id) {
        return leaveMapper.selectById(id);
    }

    @Override
    public List<OALeaveDO> getLeaveList(Collection<Long> ids) {
        return leaveMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<OALeaveDO> getLeavePage(OALeavePageReqVO pageReqVO) {
        return leaveMapper.selectPage(pageReqVO);
    }

    @Override
    public List<OALeaveDO> getLeaveList(OALeaveExportReqVO exportReqVO) {
        return leaveMapper.selectList(exportReqVO);
    }

}

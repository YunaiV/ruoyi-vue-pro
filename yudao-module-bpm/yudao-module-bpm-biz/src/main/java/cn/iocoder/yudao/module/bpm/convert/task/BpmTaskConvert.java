package cn.iocoder.yudao.module.bpm.convert.task;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskRespVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmFormDO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmTaskStatusEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.FlowableUtils;
import cn.iocoder.yudao.module.bpm.service.message.dto.BpmMessageSendWhenTaskCreatedReqDTO;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.service.impl.persistence.entity.TaskEntityImpl;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;

/**
 * Bpm 任务 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface BpmTaskConvert {

    BpmTaskConvert INSTANCE = Mappers.getMapper(BpmTaskConvert.class);

    default PageResult<BpmTaskRespVO> buildTodoTaskPage(PageResult<Task> pageResult,
                                                        Map<String, ProcessInstance> processInstanceMap,
                                                        Map<Long, AdminUserRespDTO> userMap) {
        return BeanUtils.toBean(pageResult, BpmTaskRespVO.class, taskVO -> {
            ProcessInstance processInstance = processInstanceMap.get(taskVO.getProcessInstanceId());
            if (processInstance == null) {
                return;
            }
            taskVO.setProcessInstance(BeanUtils.toBean(processInstance, BpmTaskRespVO.ProcessInstance.class));
            AdminUserRespDTO startUser = userMap.get(NumberUtils.parseLong(processInstance.getStartUserId()));
            taskVO.getProcessInstance().setStartUser(BeanUtils.toBean(startUser, BpmProcessInstanceRespVO.User.class));
        });
    }

    default PageResult<BpmTaskRespVO> buildTaskPage(PageResult<HistoricTaskInstance> pageResult,
                                                    Map<String, HistoricProcessInstance> processInstanceMap,
                                                    Map<Long, AdminUserRespDTO> userMap,
                                                    Map<Long, DeptRespDTO> deptMap) {
        List<BpmTaskRespVO> taskVOList = CollectionUtils.convertList(pageResult.getList(), task -> {
            BpmTaskRespVO taskVO = BeanUtils.toBean(task, BpmTaskRespVO.class);
            taskVO.setStatus(FlowableUtils.getTaskStatus(task)).setReason(FlowableUtils.getTaskReason(task));
            // 用户信息
            AdminUserRespDTO assignUser = userMap.get(NumberUtils.parseLong(task.getAssignee()));
            if (assignUser != null) {
                taskVO.setAssigneeUser(BeanUtils.toBean(assignUser, BpmProcessInstanceRespVO.User.class));
                findAndThen(deptMap, assignUser.getDeptId(), dept -> taskVO.getAssigneeUser().setDeptName(dept.getName()));
            }
            // 流程实例
            HistoricProcessInstance processInstance = processInstanceMap.get(taskVO.getProcessInstanceId());
            if (processInstance != null) {
                AdminUserRespDTO startUser = userMap.get(NumberUtils.parseLong(processInstance.getStartUserId()));
                taskVO.setProcessInstance(BeanUtils.toBean(processInstance, BpmTaskRespVO.ProcessInstance.class));
                taskVO.getProcessInstance().setStartUser(BeanUtils.toBean(startUser, BpmProcessInstanceRespVO.User.class));
            }
            return taskVO;
        });
        return new PageResult<>(taskVOList, pageResult.getTotal());
    }

    default List<BpmTaskRespVO> buildTaskListByProcessInstanceId(List<HistoricTaskInstance> taskList,
                                                                 HistoricProcessInstance processInstance,
                                                                 Map<Long, BpmFormDO> formMap,
                                                                 Map<Long, AdminUserRespDTO> userMap,
                                                                 Map<Long, DeptRespDTO> deptMap,
                                                                 BpmnModel bpmnModel) {
        List<BpmTaskRespVO> taskVOList = CollectionUtils.convertList(taskList, task -> {
            BpmTaskRespVO taskVO = BeanUtils.toBean(task, BpmTaskRespVO.class);
            Integer taskStatus = FlowableUtils.getTaskStatus(task);
            taskVO.setStatus(taskStatus).setReason(FlowableUtils.getTaskReason(task));
            // 流程实例
            AdminUserRespDTO startUser = userMap.get(NumberUtils.parseLong(processInstance.getStartUserId()));
            taskVO.setProcessInstance(BeanUtils.toBean(processInstance, BpmTaskRespVO.ProcessInstance.class));
            taskVO.getProcessInstance().setStartUser(BeanUtils.toBean(startUser, BpmProcessInstanceRespVO.User.class));
            // 表单信息
            BpmFormDO form = MapUtil.get(formMap, NumberUtils.parseLong(task.getFormKey()), BpmFormDO.class);
            if (form != null) {
                taskVO.setFormId(form.getId()).setFormName(form.getName()).setFormConf(form.getConf())
                        .setFormFields(form.getFields()).setFormVariables(FlowableUtils.getTaskFormVariable(task));
            }
            // 用户信息
            AdminUserRespDTO assignUser = userMap.get(NumberUtils.parseLong(task.getAssignee()));
            if (assignUser != null) {
                taskVO.setAssigneeUser(BeanUtils.toBean(assignUser, BpmProcessInstanceRespVO.User.class));
                findAndThen(deptMap, assignUser.getDeptId(), dept -> taskVO.getAssigneeUser().setDeptName(dept.getName()));
            }
            AdminUserRespDTO ownerUser = userMap.get(NumberUtils.parseLong(task.getOwner()));
            if (ownerUser != null) {
                taskVO.setOwnerUser(BeanUtils.toBean(ownerUser, BpmProcessInstanceRespVO.User.class));
                findAndThen(deptMap, ownerUser.getDeptId(), dept -> taskVO.getOwnerUser().setDeptName(dept.getName()));
            }
            if (BpmTaskStatusEnum.RUNNING.getStatus().equals(taskStatus)){
                // 设置表单权限 TODO @芋艿 是不是还要加一个全局的权限 基于 processInstance 的权限；回复：可能不需要，但是发起人，需要有个权限配置
                // TODO @jason：貌似这么返回，主要解决当前审批 task 的表单权限，但是不同抄送人的表单权限，可能不太对。例如说，对 A 抄送人是隐藏某个字段。
                // @芋艿 表单权限需要分离开。单独的接口来获取了 BpmProcessInstanceService.getProcessInstanceFormFieldsPermission
                taskVO.setFieldsPermission(BpmnModelUtils.parseFormFieldsPermission(bpmnModel, task.getTaskDefinitionKey()));
                // 操作按钮设置
                taskVO.setButtonsSetting(BpmnModelUtils.parseButtonsSetting(bpmnModel, task.getTaskDefinitionKey()));
            }
           return taskVO;
        });

        // 拼接父子关系
        Map<String, List<BpmTaskRespVO>> childrenTaskMap = convertMultiMap(
                filterList(taskVOList, r -> StrUtil.isNotEmpty(r.getParentTaskId())),
                BpmTaskRespVO::getParentTaskId);
        for (BpmTaskRespVO taskVO : taskVOList) {
            taskVO.setChildren(childrenTaskMap.get(taskVO.getId()));
        }
        return filterList(taskVOList, r -> StrUtil.isEmpty(r.getParentTaskId()));
    }

    default List<BpmTaskRespVO> buildTaskListByParentTaskId(List<Task> taskList,
                                                            Map<Long, AdminUserRespDTO> userMap,
                                                            Map<Long, DeptRespDTO> deptMap) {
        return convertList(taskList, task -> BeanUtils.toBean(task, BpmTaskRespVO.class, taskVO -> {
            AdminUserRespDTO assignUser = userMap.get(NumberUtils.parseLong(task.getAssignee()));
            if (assignUser != null) {
                taskVO.setAssigneeUser(BeanUtils.toBean(assignUser, BpmProcessInstanceRespVO.User.class));
                DeptRespDTO dept = deptMap.get(assignUser.getDeptId());
                if (dept != null) {
                    taskVO.getAssigneeUser().setDeptName(dept.getName());
                }
            }
            AdminUserRespDTO ownerUser = userMap.get(NumberUtils.parseLong(task.getOwner()));
            if (ownerUser != null) {
                taskVO.setOwnerUser(BeanUtils.toBean(ownerUser, BpmProcessInstanceRespVO.User.class));
                findAndThen(deptMap, ownerUser.getDeptId(), dept -> taskVO.getOwnerUser().setDeptName(dept.getName()));
            }
        }));
    }

    default BpmMessageSendWhenTaskCreatedReqDTO convert(ProcessInstance processInstance, AdminUserRespDTO startUser,
                                                        Task task) {
        BpmMessageSendWhenTaskCreatedReqDTO reqDTO = new BpmMessageSendWhenTaskCreatedReqDTO();
        reqDTO.setProcessInstanceId(processInstance.getProcessInstanceId())
                .setProcessInstanceName(processInstance.getName()).setStartUserId(startUser.getId())
                .setStartUserNickname(startUser.getNickname()).setTaskId(task.getId()).setTaskName(task.getName())
                .setAssigneeUserId(NumberUtils.parseLong(task.getAssignee()));
        return reqDTO;
    }

    /**
     * 将父任务的属性，拷贝到子任务（加签任务）
     * <p>
     * 为什么不使用 mapstruct 映射？因为 TaskEntityImpl 还有很多其他属性，这里我们只设置我们需要的。
     * 使用 mapstruct 会将里面嵌套的各个属性值都设置进去，会出现意想不到的问题。
     *
     * @param parentTask 父任务
     * @param childTask  加签任务
     */
    default void copyTo(TaskEntityImpl parentTask, TaskEntityImpl childTask) {
        childTask.setName(parentTask.getName());
        childTask.setDescription(parentTask.getDescription());
        childTask.setCategory(parentTask.getCategory());
        childTask.setParentTaskId(parentTask.getId());
        childTask.setProcessDefinitionId(parentTask.getProcessDefinitionId());
        childTask.setProcessInstanceId(parentTask.getProcessInstanceId());
        childTask.setTaskDefinitionKey(parentTask.getTaskDefinitionKey());
        childTask.setTaskDefinitionId(parentTask.getTaskDefinitionId());
        childTask.setPriority(parentTask.getPriority());
        childTask.setCreateTime(new Date());
        childTask.setTenantId(parentTask.getTenantId());
    }

}

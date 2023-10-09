package cn.iocoder.yudao.module.bpm.convert.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.*;
import cn.iocoder.yudao.module.bpm.dal.dataobject.task.BpmTaskExtDO;
import cn.iocoder.yudao.module.bpm.service.message.dto.BpmMessageSendWhenTaskCreatedReqDTO;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.common.engine.impl.db.SuspensionState;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.service.impl.persistence.entity.TaskEntityImpl;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Bpm 任务 Convert
 *
 * @author 芋道源码
 */
@Mapper(uses = DateUtils.class)
public interface BpmTaskConvert {

    BpmTaskConvert INSTANCE = Mappers.getMapper(BpmTaskConvert.class);

    default List<BpmTaskTodoPageItemRespVO> convertList1(List<Task> tasks,
                                                         Map<String, ProcessInstance> processInstanceMap,
                                                         Map<Long, AdminUserRespDTO> userMap) {
        return CollectionUtils.convertList(tasks, task -> {
            BpmTaskTodoPageItemRespVO respVO = convert1(task);
            ProcessInstance processInstance = processInstanceMap.get(task.getProcessInstanceId());
            if (processInstance != null) {
                AdminUserRespDTO startUser = userMap.get(NumberUtils.parseLong(processInstance.getStartUserId()));
                respVO.setProcessInstance(convert(processInstance, startUser));
            }
            return respVO;
        });
    }

    @Mapping(source = "suspended", target = "suspensionState", qualifiedByName = "convertSuspendedToSuspensionState")
    BpmTaskTodoPageItemRespVO convert1(Task bean);

    @Named("convertSuspendedToSuspensionState")
    default Integer convertSuspendedToSuspensionState(boolean suspended) {
        return suspended ? SuspensionState.SUSPENDED.getStateCode() : SuspensionState.ACTIVE.getStateCode();
    }

    default List<BpmTaskDonePageItemRespVO> convertList2(List<HistoricTaskInstance> tasks,
                                                         Map<String, BpmTaskExtDO> bpmTaskExtDOMap, Map<String, HistoricProcessInstance> historicProcessInstanceMap,
                                                         Map<Long, AdminUserRespDTO> userMap) {
        return CollectionUtils.convertList(tasks, task -> {
            BpmTaskDonePageItemRespVO respVO = convert2(task);
            BpmTaskExtDO taskExtDO = bpmTaskExtDOMap.get(task.getId());
            copyTo(taskExtDO, respVO);
            HistoricProcessInstance processInstance = historicProcessInstanceMap.get(task.getProcessInstanceId());
            if (processInstance != null) {
                AdminUserRespDTO startUser = userMap.get(NumberUtils.parseLong(processInstance.getStartUserId()));
                respVO.setProcessInstance(convert(processInstance, startUser));
            }
            return respVO;
        });
    }

    BpmTaskDonePageItemRespVO convert2(HistoricTaskInstance bean);

    @Mappings({
            @Mapping(source = "processInstance.id", target = "id"),
            @Mapping(source = "processInstance.name", target = "name"),
            @Mapping(source = "processInstance.startUserId", target = "startUserId"),
            @Mapping(source = "processInstance.processDefinitionId", target = "processDefinitionId"),
            @Mapping(source = "startUser.nickname", target = "startUserNickname")
    })
    BpmTaskTodoPageItemRespVO.ProcessInstance convert(ProcessInstance processInstance, AdminUserRespDTO startUser);

    default List<BpmTaskRespVO> convertList3(List<HistoricTaskInstance> tasks,
                                             Map<String, BpmTaskExtDO> bpmTaskExtDOMap, HistoricProcessInstance processInstance,
                                             Map<Long, AdminUserRespDTO> userMap, Map<Long, DeptRespDTO> deptMap) {
        return CollectionUtils.convertList(tasks, task -> {
            BpmTaskRespVO respVO = convert3(task);
            BpmTaskExtDO taskExtDO = bpmTaskExtDOMap.get(task.getId());
            copyTo(taskExtDO, respVO);
            if (processInstance != null) {
                AdminUserRespDTO startUser = userMap.get(NumberUtils.parseLong(processInstance.getStartUserId()));
                respVO.setProcessInstance(convert(processInstance, startUser));
            }
            AdminUserRespDTO assignUser = userMap.get(NumberUtils.parseLong(task.getAssignee()));
            if (assignUser != null) {
                respVO.setAssigneeUser(convert3(assignUser));
                DeptRespDTO dept = deptMap.get(assignUser.getDeptId());
                if (dept != null) {
                    respVO.getAssigneeUser().setDeptName(dept.getName());
                }
            }
            return respVO;
        });
    }

    @Mapping(source = "taskDefinitionKey", target = "definitionKey")
    BpmTaskRespVO convert3(HistoricTaskInstance bean);

    BpmTaskRespVO.User convert3(AdminUserRespDTO bean);

    @Mapping(target = "id", ignore = true)
    void copyTo(BpmTaskExtDO from, @MappingTarget BpmTaskDonePageItemRespVO to);

    @Mappings({@Mapping(source = "processInstance.id", target = "id"),
            @Mapping(source = "processInstance.name", target = "name"),
            @Mapping(source = "processInstance.startUserId", target = "startUserId"),
            @Mapping(source = "processInstance.processDefinitionId", target = "processDefinitionId"),
            @Mapping(source = "startUser.nickname", target = "startUserNickname")})
    BpmTaskTodoPageItemRespVO.ProcessInstance convert(HistoricProcessInstance processInstance,
                                                      AdminUserRespDTO startUser);

    default BpmTaskExtDO convert2TaskExt(Task task) {
        BpmTaskExtDO taskExtDO = new BpmTaskExtDO().setTaskId(task.getId())
                .setAssigneeUserId(NumberUtils.parseLong(task.getAssignee())).setName(task.getName())
                .setProcessDefinitionId(task.getProcessDefinitionId()).setProcessInstanceId(task.getProcessInstanceId());
        taskExtDO.setCreateTime(LocalDateTimeUtil.of(task.getCreateTime()));
        return taskExtDO;
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

    default List<BpmTaskSimpleRespVO> convertList(List<? extends FlowElement> elementList) {
        return CollectionUtils.convertList(elementList, element -> new BpmTaskSimpleRespVO()
                .setName(element.getName())
                .setDefinitionKey(element.getId()));
    }

    // TODO @海：可以使用 mapstruct 映射么？
    default TaskEntityImpl convert(TaskEntityImpl task,TaskEntityImpl parentTask){
        task.setCategory(parentTask.getCategory());
        task.setDescription(parentTask.getDescription());
        task.setTenantId(parentTask.getTenantId());
        task.setName(parentTask.getName());
        task.setParentTaskId(parentTask.getId());
        task.setProcessDefinitionId(parentTask.getProcessDefinitionId());
        task.setProcessInstanceId(parentTask.getProcessInstanceId());
        task.setTaskDefinitionKey(parentTask.getTaskDefinitionKey());
        task.setTaskDefinitionId(parentTask.getTaskDefinitionId());
        task.setPriority(parentTask.getPriority());
        task.setCreateTime(new Date());
        return task;
    }

    default List<BpmTaskSubSignRespVO> convertList(List<BpmTaskExtDO> bpmTaskExtDOList,
                                                   Map<Long, AdminUserRespDTO> userMap,
                                                   Map<String, Task> idTaskMap){
        return CollectionUtils.convertList(bpmTaskExtDOList, task->{
            BpmTaskSubSignRespVO bpmTaskSubSignRespVO = new BpmTaskSubSignRespVO();
            bpmTaskSubSignRespVO.setName(task.getName());
            bpmTaskSubSignRespVO.setId(task.getTaskId());
            Task sourceTask = idTaskMap.get(task.getTaskId());
            // 后加签任务不会直接设置 assignee ,所以不存在 assignee 的情况，则去取 owner
            String assignee = StrUtil.isNotEmpty(sourceTask.getAssignee()) ? sourceTask.getAssignee() : sourceTask.getOwner();
            AdminUserRespDTO assignUser = userMap.get(NumberUtils.parseLong(assignee));
            if (assignUser != null) {
                bpmTaskSubSignRespVO.setAssigneeUser(convert3(assignUser));
            }
            return bpmTaskSubSignRespVO;
        });
    }

    /**
     * 转换任务为父子级
     * @param result
     * @return
     */
    default List<BpmTaskRespVO> convertChildrenList(List<BpmTaskRespVO> result){
        List<BpmTaskRespVO> childrenTaskList = CollectionUtils.filterList(result, r -> StrUtil.isNotEmpty(r.getParentTaskId()));
        Map<String, List<BpmTaskRespVO>> parentChildrenTaskListMap = CollectionUtils.convertMultiMap(childrenTaskList, BpmTaskRespVO::getParentTaskId);
        for (BpmTaskRespVO bpmTaskRespVO : result) {
            bpmTaskRespVO.setChildren(parentChildrenTaskListMap.get(bpmTaskRespVO.getId()));
        }
        return CollectionUtils.filterList(result, r -> StrUtil.isEmpty(r.getParentTaskId()));
    }
}

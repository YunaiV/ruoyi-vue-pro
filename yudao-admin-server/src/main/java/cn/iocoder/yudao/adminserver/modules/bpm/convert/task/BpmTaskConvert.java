package cn.iocoder.yudao.adminserver.modules.bpm.convert.task;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.task.vo.task.BpmTaskDonePageItemRespVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.task.vo.task.BpmTaskRespVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.task.vo.task.BpmTaskTodoPageItemRespVO;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.task.BpmTaskExtDO;
import cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.dept.SysDeptDO;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.persistence.entity.SuspensionState;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

/**
 * Bpm 任务 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface BpmTaskConvert {

    BpmTaskConvert INSTANCE = Mappers.getMapper(BpmTaskConvert.class);

    default List<BpmTaskTodoPageItemRespVO> convertList(List<Task> tasks, Map<String, ProcessInstance> processInstanceMap,
                                                        Map<Long, SysUserDO> userMap) {
        return CollectionUtils.convertList(tasks, task -> {
            ProcessInstance processInstance = processInstanceMap.get(task.getProcessInstanceId());
            return convert(task, processInstance, userMap.get(Long.valueOf(processInstance.getStartUserId())));
        });
    }

    @Mappings({
            @Mapping(source = "task.id", target = "id"),
            @Mapping(source = "task.name", target = "name"),
            @Mapping(source = "task.claimTime", target = "claimTime"),
            @Mapping(source = "task.createTime", target = "createTime"),
            @Mapping(source = "task.suspended", target = "suspensionState", qualifiedByName = "convertSuspendedToSuspensionState"),
            @Mapping(source = "processInstance.id", target = "processInstance.id"),
            @Mapping(source = "processInstance.name", target = "processInstance.name"),
            @Mapping(source = "processInstance.startUserId", target = "processInstance.startUserId"),
            @Mapping(source = "processInstance.processDefinitionId", target = "processInstance.processDefinitionId"),
            @Mapping(source = "user.nickname", target = "processInstance.startUserNickname")
    })
    BpmTaskTodoPageItemRespVO convert(Task task, ProcessInstance processInstance, SysUserDO user);

    @Named("convertSuspendedToSuspensionState")
    default Integer convertSuspendedToSuspensionState(boolean suspended) {
        return suspended ? SuspensionState.SUSPENDED.getStateCode() :
                SuspensionState.ACTIVE.getStateCode();
    }

    default List<BpmTaskDonePageItemRespVO> convertList2(List<HistoricTaskInstance> tasks, Map<String, BpmTaskExtDO> bpmTaskExtDOMap,
                                                         Map<String, HistoricProcessInstance> historicProcessInstanceMap,
                                                         Map<Long, SysUserDO> userMap) {
        return CollectionUtils.convertList(tasks, task -> {
            BpmTaskExtDO taskExtDO = bpmTaskExtDOMap.get(task.getId());
            HistoricProcessInstance processInstance = historicProcessInstanceMap.get(task.getProcessInstanceId());
            SysUserDO userDO = userMap.get(Long.valueOf(processInstance.getStartUserId()));
            return convert(task, taskExtDO, processInstance, userDO);
        });
    }

    @Mappings({
            @Mapping(source = "task.id", target = "id"),
            @Mapping(source = "task.name", target = "name"),
            @Mapping(source = "task.claimTime", target = "claimTime"),
            @Mapping(source = "task.createTime", target = "createTime"),
            @Mapping(source = "task.endTime", target = "endTime"),
            @Mapping(source = "task.durationInMillis", target = "durationInMillis"),
            @Mapping(source = "taskExtDO.result", target = "result"),
            @Mapping(source = "taskExtDO.comment", target = "comment"),
            @Mapping(source = "processInstance.id", target = "processInstance.id"),
            @Mapping(source = "processInstance.name", target = "processInstance.name"),
            @Mapping(source = "processInstance.startUserId", target = "processInstance.startUserId"),
            @Mapping(source = "processInstance.processDefinitionId", target = "processInstance.processDefinitionId"),
            @Mapping(source = "startUser.nickname", target = "processInstance.startUserNickname")
    })
    BpmTaskDonePageItemRespVO convert(HistoricTaskInstance task, BpmTaskExtDO taskExtDO, HistoricProcessInstance processInstance, SysUserDO startUser);

    @Mappings({
            @Mapping(source = "id", target = "taskId"),
            @Mapping(source = "assignee", target = "assigneeUserId"),
            @Mapping(source = "createdDate", target = "createTime")
    })
    BpmTaskExtDO convert(org.activiti.api.task.model.Task bean);

    default List<BpmTaskRespVO> convertList3(List<HistoricTaskInstance> tasks, Map<String, BpmTaskExtDO> bpmTaskExtDOMap,
                                             HistoricProcessInstance processInstance, Map<Long, SysUserDO> userMap,
                                             Map<Long, SysDeptDO> deptMap) {
        return CollectionUtils.convertList(tasks, task -> {
            BpmTaskRespVO respVO = convert3(task);
            BpmTaskExtDO taskExtDO = bpmTaskExtDOMap.get(task.getId());
            copyTo3(taskExtDO, respVO);
            if (processInstance != null) {
                SysUserDO startUser = userMap.get(NumberUtils.parseLong(processInstance.getStartUserId()));
                respVO.setProcessInstance(convert(processInstance, startUser));
            }
            SysUserDO assignUser = userMap.get(NumberUtils.parseLong(task.getAssignee()));
            if (assignUser != null) {
                respVO.setAssigneeUser(convert3(assignUser));
                SysDeptDO dept = deptMap.get(assignUser.getDeptId());
                if (dept != null) {
                    respVO.getAssigneeUser().setDeptName(dept.getName());
                }
            }
            return respVO;
        });
    }

    BpmTaskRespVO convert3(HistoricTaskInstance bean);
    BpmTaskRespVO.User convert3(SysUserDO bean);
    void copyTo3(BpmTaskExtDO from, @MappingTarget BpmTaskRespVO to);

    @Mappings({
            @Mapping(source = "processInstance.id", target = "id"),
            @Mapping(source = "processInstance.name", target = "name"),
            @Mapping(source = "processInstance.startUserId", target = "startUserId"),
            @Mapping(source = "processInstance.processDefinitionId", target = "processDefinitionId"),
            @Mapping(source = "startUser.nickname", target = "startUserNickname")
    })
    BpmTaskTodoPageItemRespVO.ProcessInstance convert(HistoricProcessInstance processInstance, SysUserDO startUser);

}

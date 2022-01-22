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

    default List<BpmTaskTodoPageItemRespVO> convertList1(List<Task> tasks, Map<String, ProcessInstance> processInstanceMap,
                                                         Map<Long, SysUserDO> userMap) {
        return CollectionUtils.convertList(tasks, task -> {
            BpmTaskTodoPageItemRespVO respVO = convert1(task);
            ProcessInstance processInstance = processInstanceMap.get(task.getProcessInstanceId());
            if (processInstance != null) {
                SysUserDO startUser = userMap.get(NumberUtils.parseLong(processInstance.getStartUserId()));
                respVO.setProcessInstance(convert(processInstance, startUser));
            }
            return respVO;
        });
    }

    @Mapping(source = "suspended", target = "suspensionState", qualifiedByName = "convertSuspendedToSuspensionState")
    BpmTaskTodoPageItemRespVO convert1(Task bean);

    @Named("convertSuspendedToSuspensionState")
    default Integer convertSuspendedToSuspensionState(boolean suspended) {
        return suspended ? SuspensionState.SUSPENDED.getStateCode() :
                SuspensionState.ACTIVE.getStateCode();
    }

    default List<BpmTaskDonePageItemRespVO> convertList2(List<HistoricTaskInstance> tasks, Map<String, BpmTaskExtDO> bpmTaskExtDOMap,
                                                         Map<String, HistoricProcessInstance> historicProcessInstanceMap,
                                                         Map<Long, SysUserDO> userMap) {
        return CollectionUtils.convertList(tasks, task -> {
            BpmTaskDonePageItemRespVO respVO = convert2(task);
            BpmTaskExtDO taskExtDO = bpmTaskExtDOMap.get(task.getId());
            copyTo(taskExtDO, respVO);
            HistoricProcessInstance processInstance = historicProcessInstanceMap.get(task.getProcessInstanceId());
            if (processInstance != null) {
                SysUserDO startUser = userMap.get(NumberUtils.parseLong(processInstance.getStartUserId()));
                respVO.setProcessInstance(convert(processInstance, startUser));
            }
            return respVO;
        });
    }

    BpmTaskDonePageItemRespVO convert2(HistoricTaskInstance bean);

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
            copyTo(taskExtDO, respVO);
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

    @Mapping(source = "taskDefinitionKey", target = "definitionKey")
    BpmTaskRespVO convert3(HistoricTaskInstance bean);
    BpmTaskRespVO.User convert3(SysUserDO bean);

    void copyTo(BpmTaskExtDO from, @MappingTarget BpmTaskDonePageItemRespVO to);

    @Mappings({
            @Mapping(source = "processInstance.id", target = "id"),
            @Mapping(source = "processInstance.name", target = "name"),
            @Mapping(source = "processInstance.startUserId", target = "startUserId"),
            @Mapping(source = "processInstance.processDefinitionId", target = "processDefinitionId"),
            @Mapping(source = "startUser.nickname", target = "startUserNickname")
    })
    BpmTaskTodoPageItemRespVO.ProcessInstance convert(ProcessInstance processInstance, SysUserDO startUser);

    @Mappings({
            @Mapping(source = "processInstance.id", target = "id"),
            @Mapping(source = "processInstance.name", target = "name"),
            @Mapping(source = "processInstance.startUserId", target = "startUserId"),
            @Mapping(source = "processInstance.processDefinitionId", target = "processDefinitionId"),
            @Mapping(source = "startUser.nickname", target = "startUserNickname")
    })
    BpmTaskTodoPageItemRespVO.ProcessInstance convert(HistoricProcessInstance processInstance, SysUserDO startUser);

}

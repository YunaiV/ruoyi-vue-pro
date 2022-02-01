package cn.iocoder.yudao.module.bpm.convert.task;

import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskDonePageItemRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskTodoPageItemRespVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.task.BpmTaskExtDO;
import cn.iocoder.yudao.module.bpm.service.message.dto.BpmMessageSendWhenTaskCreatedReqDTO;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
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
        return suspended ? SuspensionState.SUSPENDED.getStateCode() :
                SuspensionState.ACTIVE.getStateCode();
    }

    default List<BpmTaskDonePageItemRespVO> convertList2(List<HistoricTaskInstance> tasks, Map<String, BpmTaskExtDO> bpmTaskExtDOMap,
                                                         Map<String, HistoricProcessInstance> historicProcessInstanceMap,
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
            @Mapping(source = "id", target = "taskId"),
            @Mapping(source = "assignee", target = "assigneeUserId"),
            @Mapping(source = "createdDate", target = "createTime")
    })
    BpmTaskExtDO convert(org.activiti.api.task.model.Task bean);

    default List<BpmTaskRespVO> convertList3(List<HistoricTaskInstance> tasks, Map<String, BpmTaskExtDO> bpmTaskExtDOMap,
                                             HistoricProcessInstance processInstance, Map<Long, AdminUserRespDTO> userMap,
                                             Map<Long, DeptRespDTO> deptMap) {
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

    void copyTo(BpmTaskExtDO from, @MappingTarget BpmTaskDonePageItemRespVO to);

    @Mappings({
            @Mapping(source = "processInstance.id", target = "id"),
            @Mapping(source = "processInstance.name", target = "name"),
            @Mapping(source = "processInstance.startUserId", target = "startUserId"),
            @Mapping(source = "processInstance.processDefinitionId", target = "processDefinitionId"),
            @Mapping(source = "startUser.nickname", target = "startUserNickname")
    })
    BpmTaskTodoPageItemRespVO.ProcessInstance convert(ProcessInstance processInstance, AdminUserRespDTO startUser);

    @Mappings({
            @Mapping(source = "processInstance.id", target = "id"),
            @Mapping(source = "processInstance.name", target = "name"),
            @Mapping(source = "processInstance.startUserId", target = "startUserId"),
            @Mapping(source = "processInstance.processDefinitionId", target = "processDefinitionId"),
            @Mapping(source = "startUser.nickname", target = "startUserNickname")
    })
    BpmTaskTodoPageItemRespVO.ProcessInstance convert(HistoricProcessInstance processInstance, AdminUserRespDTO startUser);

    default BpmMessageSendWhenTaskCreatedReqDTO convert(ProcessInstance processInstance, AdminUserRespDTO startUser, org.activiti.api.task.model.Task task) {
        BpmMessageSendWhenTaskCreatedReqDTO reqDTO = new BpmMessageSendWhenTaskCreatedReqDTO();
        copyTo(processInstance, reqDTO);
        copyTo(startUser, reqDTO);
        copyTo(task, reqDTO);
        return reqDTO;
    }
    @Mapping(source = "name", target = "processInstanceName")
    void copyTo(ProcessInstance from, @MappingTarget BpmMessageSendWhenTaskCreatedReqDTO to);
    @Mappings({
            @Mapping(source = "id", target = "startUserId"),
            @Mapping(source = "nickname", target = "startUserNickname")
    })
    void copyTo(AdminUserRespDTO from, @MappingTarget BpmMessageSendWhenTaskCreatedReqDTO to);
    @Mappings({
            @Mapping(source = "id", target = "taskId"),
            @Mapping(source = "name", target = "taskName"),
            @Mapping(source = "assignee", target = "assigneeUserId")
    })
    void copyTo(org.activiti.api.task.model.Task task, @MappingTarget BpmMessageSendWhenTaskCreatedReqDTO to);
}

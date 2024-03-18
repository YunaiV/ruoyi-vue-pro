package cn.iocoder.yudao.module.bpm.convert.task;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskRespVO;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmConstants;
import cn.iocoder.yudao.module.bpm.service.message.dto.BpmMessageSendWhenTaskCreatedReqDTO;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
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

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;

/**
 * Bpm 任务 Convert
 *
 * @author 芋道源码
 */
@Mapper(uses = DateUtils.class)
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
            AdminUserRespDTO startUser = userMap.get(NumberUtils.parseLong(processInstance.getStartUserId()));
            taskVO.setProcessInstance(BeanUtils.toBean(processInstance, BpmTaskRespVO.ProcessInstance.class,
                    processInstanceVO -> processInstanceVO.setStartUser(BeanUtils.toBean(startUser, BpmProcessInstanceRespVO.User.class))));
        });
    }

    default PageResult<BpmTaskRespVO> buildDoneTaskPage(PageResult<HistoricTaskInstance> pageResult,
                                                        Map<String, HistoricProcessInstance> processInstanceMap,
                                                        Map<Long, AdminUserRespDTO> userMap) {
        List<BpmTaskRespVO> taskVOList = CollectionUtils.convertList(pageResult.getList(), task -> {
            BpmTaskRespVO taskVO = BeanUtils.toBean(task, BpmTaskRespVO.class);
            taskVO.setStatus((Integer) task.getTaskLocalVariables().get(BpmConstants.TASK_VARIABLE_STATUS));
            taskVO.setReason((String) task.getTaskLocalVariables().get(BpmConstants.TASK_VARIABLE_REASON));
            // 流程实例
            HistoricProcessInstance processInstance = processInstanceMap.get(taskVO.getProcessInstanceId());
            if (processInstance != null) {
                AdminUserRespDTO startUser = userMap.get(NumberUtils.parseLong(processInstance.getStartUserId()));
                taskVO.setProcessInstance(BeanUtils.toBean(processInstance, BpmTaskRespVO.ProcessInstance.class,
                        processInstanceVO -> processInstanceVO.setStartUser(BeanUtils.toBean(startUser, BpmProcessInstanceRespVO.User.class))));
            }
            return taskVO;
        });
        return new PageResult<>(taskVOList, pageResult.getTotal());
    }

    default List<BpmTaskRespVO> buildTaskListByProcessInstanceId(List<HistoricTaskInstance> taskList,
                                                                 HistoricProcessInstance processInstance,
                                                                 Map<Long, AdminUserRespDTO> userMap,
                                                                 Map<Long, DeptRespDTO> deptMap) {
        List<BpmTaskRespVO> taskVOList = CollectionUtils.convertList(taskList, task -> {
            BpmTaskRespVO taskVO = BeanUtils.toBean(task, BpmTaskRespVO.class);
            taskVO.setStatus((Integer) task.getTaskLocalVariables().get(BpmConstants.TASK_VARIABLE_STATUS));
            taskVO.setReason((String) task.getTaskLocalVariables().get(BpmConstants.TASK_VARIABLE_REASON));
            // 流程实例
            AdminUserRespDTO startUser = userMap.get(NumberUtils.parseLong(processInstance.getStartUserId()));
            taskVO.setProcessInstance(BeanUtils.toBean(processInstance, BpmTaskRespVO.ProcessInstance.class,
                    processInstanceVO -> processInstanceVO.setStartUser(BeanUtils.toBean(startUser, BpmProcessInstanceRespVO.User.class))));
            // 用户信息
            AdminUserRespDTO assignUser = userMap.get(NumberUtils.parseLong(task.getAssignee()));
            if (assignUser != null) {
                taskVO.setAssigneeUser(BeanUtils.toBean(assignUser, BpmProcessInstanceRespVO.User.class));
                DeptRespDTO dept = deptMap.get(assignUser.getDeptId());
                if (dept != null) {
                    taskVO.getAssigneeUser().setDeptName(dept.getName());
                }
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

    default BpmMessageSendWhenTaskCreatedReqDTO convert(ProcessInstance processInstance, AdminUserRespDTO startUser,
                                                        Task task) {
        BpmMessageSendWhenTaskCreatedReqDTO reqDTO = new BpmMessageSendWhenTaskCreatedReqDTO();
        reqDTO.setProcessInstanceId(processInstance.getProcessInstanceId())
                .setProcessInstanceName(processInstance.getName()).setStartUserId(startUser.getId())
                .setStartUserNickname(startUser.getNickname()).setTaskId(task.getId()).setTaskName(task.getName())
                .setAssigneeUserId(NumberUtils.parseLong(task.getAssignee()));
        return reqDTO;
    }

    //此处不用 mapstruct 映射，因为 TaskEntityImpl 还有很多其他属性，这里我们只设置我们需要的
    //使用 mapstruct 会将里面嵌套的各个属性值都设置进去，会出现意想不到的问题
    default TaskEntityImpl convert(TaskEntityImpl task,TaskEntityImpl parentTask){
        task.setCategory(parentTask.getCategory());
        task.setDescription(parentTask.getDescription());
        task.setTenantId(parentTask.getTenantId());
        task.setName(parentTask.getName());
        task.setParentTaskId(parentTask.getId());
        task.setProcessDefinitionId(parentTask.getProcessDefinitionId());
        task.setProcessInstanceId(parentTask.getProcessInstanceId());
//        task.setExecutionId(parentTask.getExecutionId()); // TODO 芋艿：新加的，不太确定；尴尬，不加时，子任务不通过会失败（报错）；加了，子任务审批通过会失败（报错）
        task.setTaskDefinitionKey(parentTask.getTaskDefinitionKey());
        task.setTaskDefinitionId(parentTask.getTaskDefinitionId());
        task.setPriority(parentTask.getPriority());
        task.setCreateTime(new Date());
        return task;
    }

}

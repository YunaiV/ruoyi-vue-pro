package cn.iocoder.yudao.module.bpm.convert.task;

import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.module.bpm.service.message.dto.BpmMessageSendWhenTaskCreatedReqDTO;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.service.impl.persistence.entity.TaskEntityImpl;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Date;

/**
 * Bpm 任务 Convert
 *
 * @author 芋道源码
 */
@Mapper(uses = DateUtils.class)
public interface BpmTaskConvert {

    BpmTaskConvert INSTANCE = Mappers.getMapper(BpmTaskConvert.class);

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

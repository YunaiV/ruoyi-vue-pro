package cn.iocoder.yudao.adminserver.modules.bpm.convert.workflow;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.TaskStepVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.TodoTaskRespVO;
import org.activiti.api.task.model.Task;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TaskConvert {
    TaskConvert INSTANCE = Mappers.getMapper(TaskConvert.class);

    @Mappings(value = {
            @Mapping(source = "task.id", target = "id"),
            @Mapping(source = "task.businessKey", target = "businessKey"),
            @Mapping(source = "task.assignee", target = "status",qualifiedByName = "convertAssigneeToStatus"),
            @Mapping(source = "definition.name", target = "processName"),
            @Mapping(source = "definition.key", target = "processKey"),
            @Mapping(source = "definition.id", target = "processInstanceId")
    })
    TodoTaskRespVO convert(Task task, ProcessDefinition definition);


    @Named("convertAssigneeToStatus")
    default Integer convertAssigneeToStatus(String assignee) {
        //TODO 不应该通过 assignee 定义状态  需要定义更多的状态
        return assignee == null ?  1 :  2;
    }

    @Mappings(value = {
            @Mapping(source = "activityName", target = "stepName"),
            @Mapping(source = "assignee", target = "assignee")
    })
    TaskStepVO convert(HistoricActivityInstance instance);
}

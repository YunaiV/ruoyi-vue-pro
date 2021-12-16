package cn.iocoder.yudao.adminserver.modules.activiti.controller.workflow.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class TaskHandleVO {

    private Object formObject;


    private List<TaskStepVO> historyTask;


    private String taskVariable;
}

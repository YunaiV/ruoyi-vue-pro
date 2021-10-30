package cn.iocoder.yudao.adminserver.modules.activiti.controller.workflow.vo;

import lombok.Data;
import lombok.ToString;

import java.util.Map;

@Data
@ToString
public class TaskReqVO {

    private String taskId;

    private Map<String,Object> variables;

    private String comment;
}

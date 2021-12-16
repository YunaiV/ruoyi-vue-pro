package cn.iocoder.yudao.adminserver.modules.activiti.controller.workflow.vo;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class TaskStepVO {

    private String stepName;

    private Date startTime;

    private Date endTime;

    private String assignee;

    private String comment;

    private Integer status;

}

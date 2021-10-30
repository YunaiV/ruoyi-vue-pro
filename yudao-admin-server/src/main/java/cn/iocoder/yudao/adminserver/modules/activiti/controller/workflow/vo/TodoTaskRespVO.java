package cn.iocoder.yudao.adminserver.modules.activiti.controller.workflow.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ApiModel("待办任务 Response VO")
@Data
@ToString
public class TodoTaskRespVO {

    private String id;

    /**
     * 1:未签收
     * 2:已签收
     */
    private Integer status;


    private String processName;


    private String processKey;


    private String businessKey;


    private String formKey;

}

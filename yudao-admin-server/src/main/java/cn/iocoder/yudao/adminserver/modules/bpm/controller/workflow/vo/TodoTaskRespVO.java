package cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

@ApiModel("待办任务 Response VO")
@Data
@ToString
public class TodoTaskRespVO {

    // TODO @jason：swagger 注解。这样接口文档才完整哈

    private String id;


    private String processInstanceId;

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

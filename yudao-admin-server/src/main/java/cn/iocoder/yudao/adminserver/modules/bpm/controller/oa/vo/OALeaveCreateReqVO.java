package cn.iocoder.yudao.adminserver.modules.bpm.controller.oa.vo;

import lombok.*;
import io.swagger.annotations.*;

import java.util.Map;

@ApiModel("请假申请创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OALeaveCreateReqVO extends OALeaveBaseVO {

    /**
     * 对应 bpmn 文件 <process> 的 id
     */
    @ApiModelProperty(value = "流程key")
    private String processKey;


    @ApiModelProperty(value = "流程用户任务的变量")
    private Map<String,Object> taskVariables;
}

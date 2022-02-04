package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

@ApiModel("管理后台 - 流程实例的取消 Request VO")
@Data
public class BpmProcessInstanceCancelReqVO {

    @ApiModelProperty(value = "流程实例的编号", required = true, example = "1024")
    @NotEmpty(message = "流程实例的编号不能为空")
    private String id;

    @ApiModelProperty(value = "取消原因", required = true, example = "不请假了！")
    @NotEmpty(message = "取消原因不能为空")
    private String reason;

}

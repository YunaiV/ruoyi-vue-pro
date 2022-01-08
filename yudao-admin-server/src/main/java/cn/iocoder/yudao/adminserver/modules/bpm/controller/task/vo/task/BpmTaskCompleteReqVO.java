package cn.iocoder.yudao.adminserver.modules.bpm.controller.task.vo.task;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

@ApiModel("流程任务的 Done 已办的分页 Request VO")
@Data
public class BpmTaskCompleteReqVO {

    @ApiModelProperty(value = "任务编号", required = true, example = "1024")
    @NotEmpty(message = "任务编号不能为空")
    private String id;

    @ApiModelProperty(value = "是否通过", required = true, example = "true", notes = "true 通过；false 不通过")
    @NotNull(message = "是否通过不能为空")
    private Boolean pass;

    @ApiModelProperty(value = "审批意见", required = true, example = "不错不错！")
    @NotEmpty(message = "审批意见不能为空")
    private String comment;

}

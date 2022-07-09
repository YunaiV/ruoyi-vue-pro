package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.rule;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ApiModel("管理后台 - 流程任务分配规则的 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BpmTaskAssignRuleRespVO extends BpmTaskAssignRuleBaseVO {

    @ApiModelProperty(value = "任务分配规则的编号", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "流程模型的编号", required = true, example = "2048")
    private String modelId;

    @ApiModelProperty(value = "流程定义的编号", required = true, example = "4096")
    private String processDefinitionId;

    @ApiModelProperty(value = "流程任务定义的编号", required = true, example = "2048")
    private String taskDefinitionKey;
    @ApiModelProperty(value = "流程任务定义的名字", required = true, example = "关注芋道")
    private String taskDefinitionName;

}

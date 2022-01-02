package cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@ApiModel("流程定义 Response VO")
@Data
public class ProcessDefinitionRespVO {

    @ApiModelProperty(value = "编号", required = true, example = "1024")
    private String id;

    @ApiModelProperty(value = "版本", required = true, example = "1")
    private Integer version;

    @ApiModelProperty(value = "流程名称", required = true, example = "芋道")
    @NotEmpty(message = "流程名称不能为空")
    private String name;

    @ApiModelProperty(value = "流程描述", example = "我是描述")
    private String description;

    @ApiModelProperty(value = "流程分类", notes = "参见 bpm_model_category 数据字典", example = "1")
    @NotEmpty(message = "流程分类不能为空")
    private String category;

    @ApiModelProperty(value = "表单编号", example = "1024")
    private Long formId;

    @ApiModelProperty(value = "中断状态", required = true, example = "1", notes = "参见 SuspensionState 枚举")
    private Integer suspensionState;

}

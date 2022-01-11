package cn.iocoder.yudao.adminserver.modules.bpm.controller.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;

@ApiModel("流程模型的更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BpmModelUpdateReqVO extends BpmModelBaseVO {

    @ApiModelProperty(value = "编号", required = true, example = "1024")
    @NotEmpty(message = "编号不能为空")
    private String id;

    @ApiModelProperty(value = "流程名称", example = "芋道")
    private String name;

    @ApiModelProperty(value = "流程描述", example = "我是描述")
    private String description;

    @ApiModelProperty(value = "流程分类", notes = "参见 bpm_model_category 数据字典", example = "1")
    private String category;

    @ApiModelProperty(value = "表单编号", example = "1024")
    private Long formId;

    @ApiModelProperty(value = "BPMN XML", required = true)
    private String bpmnXml;

}

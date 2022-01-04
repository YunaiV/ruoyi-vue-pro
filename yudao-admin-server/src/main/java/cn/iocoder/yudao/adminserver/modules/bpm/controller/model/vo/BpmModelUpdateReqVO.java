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

    @ApiModelProperty(value = "BPMN XML", required = true)
    @NotEmpty(message = "BPMN XML 不能为空")
    private String bpmnXml;

}

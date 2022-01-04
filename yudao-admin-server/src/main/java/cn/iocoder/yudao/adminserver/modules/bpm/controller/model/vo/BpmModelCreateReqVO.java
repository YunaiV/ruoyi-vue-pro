package cn.iocoder.yudao.adminserver.modules.bpm.controller.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;

@ApiModel("流程模型的创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BpmModelCreateReqVO extends BpmModelBaseVO {

    @ApiModelProperty(value = "BPMN XML", required = true)
    @NotEmpty(message = "BPMN XML 不能为空")
    private String bpmnXml;

}

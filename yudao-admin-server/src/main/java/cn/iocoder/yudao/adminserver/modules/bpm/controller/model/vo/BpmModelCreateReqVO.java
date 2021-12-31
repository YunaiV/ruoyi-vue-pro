package cn.iocoder.yudao.adminserver.modules.bpm.controller.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;

@ApiModel("流程定义的创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BpmModelCreateReqVO extends BpmModelBaseVO {

    @ApiModelProperty(value = "流程标识", required = true, example = "process_yudao")
    @NotEmpty(message = "流程标识不能为空")
    private String key;

    @ApiModelProperty(value = "BPMN XML", required = true)
    @NotEmpty(message = "BPMN XML 不能为空")
    private String bpmnXml;

//    @ApiModelProperty(value = "版本号")
//    private Integer revision;
}

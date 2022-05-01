package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;

@ApiModel("管理后台 - 流程模型的创建 Request VO")
@Data
public class BpmModelCreateReqVO {

    @ApiModelProperty(value = "流程标识", required = true, example = "process_yudao")
    @NotEmpty(message = "流程标识不能为空")
    private String key;

    @ApiModelProperty(value = "流程名称", required = true, example = "芋道")
    @NotEmpty(message = "流程名称不能为空")
    private String name;

    @ApiModelProperty(value = "流程描述", example = "我是描述")
    private String description;

}

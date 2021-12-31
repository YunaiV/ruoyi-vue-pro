package cn.iocoder.yudao.adminserver.modules.bpm.controller.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
* 流程定义 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class BpmModelBaseVO {

    @ApiModelProperty(value = "流程名称", required = true, example = "芋道")
    @NotEmpty(message = "流程名称不能为空")
    private String name;

    @ApiModelProperty(value = "流程描述", example = "我是描述")
    @NotEmpty(message = "流程描述不能为空")
    private String description;

}

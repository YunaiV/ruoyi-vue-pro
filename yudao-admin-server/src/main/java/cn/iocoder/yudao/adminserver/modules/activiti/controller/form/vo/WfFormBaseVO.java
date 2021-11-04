package cn.iocoder.yudao.adminserver.modules.activiti.controller.form.vo;

import lombok.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;

/**
* 动态表单 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class WfFormBaseVO {

    @ApiModelProperty(value = "表单名称", required = true)
    @NotNull(message = "表单名称不能为空")
    private String name;

    @ApiModelProperty(value = "商户状态", required = true)
    @NotNull(message = "商户状态不能为空")
    private Integer status;

    @ApiModelProperty(value = "表单JSON")
    private String formJson;

    @ApiModelProperty(value = "备注")
    private String remark;

}

package cn.iocoder.yudao.module.system.controller.admin.errorcode.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
* 错误码 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class ErrorCodeBaseVO {

    @ApiModelProperty(value = "应用名", required = true, example = "dashboard")
    @NotNull(message = "应用名不能为空")
    private String applicationName;

    @ApiModelProperty(value = "错误码编码", required = true, example = "1234")
    @NotNull(message = "错误码编码不能为空")
    private Integer code;

    @ApiModelProperty(value = "错误码错误提示", required = true, example = "帅气")
    @NotNull(message = "错误码错误提示不能为空")
    private String message;

    @ApiModelProperty(value = "备注", example = "哈哈哈")
    private String memo;

}

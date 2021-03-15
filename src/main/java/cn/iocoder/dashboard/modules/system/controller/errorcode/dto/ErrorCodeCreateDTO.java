package cn.iocoder.dashboard.modules.system.controller.errorcode.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@ApiModel("错误码创建 DTO")
@Data
public class ErrorCodeCreateDTO {

    @ApiModelProperty(value = "错误码编码", required = true, example = "10086")
    @NotNull(message = "错误码编码不能为空")
    private Integer code;
    @ApiModelProperty(value = "错误码错误提示", required = true, example = "艿艿长的丑")
    @NotEmpty(message = "错误码错误提示不能为空")
    private String message;
    @ApiModelProperty(value = "错误码分组", required = true, example = "user-service")
    @NotEmpty(message = "错误码分组不能为空")
    private String group;
    @ApiModelProperty(value = "错误码备注", example = "我就是一个备注")
    private String memo;

}


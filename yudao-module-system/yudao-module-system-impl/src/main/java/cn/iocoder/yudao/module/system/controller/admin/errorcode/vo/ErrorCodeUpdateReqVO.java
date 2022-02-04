package cn.iocoder.yudao.module.system.controller.admin.errorcode.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@ApiModel("管理后台 - 错误码更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErrorCodeUpdateReqVO extends ErrorCodeBaseVO {

    @ApiModelProperty(value = "错误码编号", required = true, example = "1024")
    @NotNull(message = "错误码编号不能为空")
    private Long id;

}

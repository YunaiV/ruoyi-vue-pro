package cn.iocoder.yudao.module.system.controller.user.vo.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@ApiModel("用户更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserUpdateReqVO extends SysUserBaseVO {

    @ApiModelProperty(value = "用户编号", required = true, example = "1024")
    @NotNull(message = "用户编号不能为空")
    private Long id;

}

package cn.iocoder.yudao.module.system.controller.permission.vo.menu;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotNull;

@ApiModel("菜单更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysMenuUpdateReqVO extends SysMenuBaseVO {

    @ApiModelProperty(value = "菜单编号", required = true, example = "1024")
    @NotNull(message = "菜单编号不能为空")
    private Long id;

}

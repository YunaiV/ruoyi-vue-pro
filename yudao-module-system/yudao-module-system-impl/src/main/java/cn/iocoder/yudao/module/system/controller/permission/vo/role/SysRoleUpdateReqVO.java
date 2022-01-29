package cn.iocoder.yudao.module.system.controller.permission.vo.role;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@ApiModel("角色更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRoleUpdateReqVO extends SysRoleBaseVO {

    @ApiModelProperty(value = "角色编号", required = true, example = "1024")
    @NotNull(message = "角色编号不能为空")
    private Long id;

}

package cn.iocoder.yudao.module.system.controller.admin.permission.vo.role;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@ApiModel("管理后台 - 角色更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleUpdateReqVO extends RoleBaseVO {

    @ApiModelProperty(value = "角色编号", required = true, example = "1024")
    @NotNull(message = "角色编号不能为空")
    private Long id;

}

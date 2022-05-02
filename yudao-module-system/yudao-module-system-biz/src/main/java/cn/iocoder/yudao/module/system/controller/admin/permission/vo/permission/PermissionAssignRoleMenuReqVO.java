package cn.iocoder.yudao.module.system.controller.admin.permission.vo.permission;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Set;

@ApiModel("管理后台 - 赋予角色菜单 Request VO")
@Data
public class PermissionAssignRoleMenuReqVO {

    @ApiModelProperty(value = "角色编号", required = true, example = "1")
    @NotNull(message = "角色编号不能为空")
    private Long roleId;

    @ApiModelProperty(value = "菜单编号列表", example = "1,3,5")
    private Set<Long> menuIds = Collections.emptySet(); // 兜底

}

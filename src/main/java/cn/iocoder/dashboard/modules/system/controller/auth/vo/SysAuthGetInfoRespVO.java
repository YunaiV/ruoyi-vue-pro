package cn.iocoder.dashboard.modules.system.controller.auth.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Set;

@ApiModel("获得用户信息 Resp VO")
@Data
public class SysAuthGetInfoRespVO {

    @ApiModelProperty(value = "角色权限数组", required = true)
    private Set<String> roles;

    @ApiModelProperty(value = "菜单权限数组", required = true)
    private Set<String> permissions;

}

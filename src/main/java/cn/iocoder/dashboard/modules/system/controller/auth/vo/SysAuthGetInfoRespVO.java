package cn.iocoder.dashboard.modules.system.controller.auth.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@ApiModel("获得用户信息 Resp VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SysAuthGetInfoRespVO {

    @ApiModelProperty(value = "用户信息", required = true)
    private UserVO user;

    @ApiModelProperty(value = "角色权限数组", required = true)
    private Set<String> roles;

    @ApiModelProperty(value = "菜单权限数组", required = true)
    private Set<String> permissions;

    @ApiModel("用户信息 VO")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserVO {

        @ApiModelProperty(value = "用户昵称", required = true, example = "芋道源码")
        private String nickname;

        @ApiModelProperty(value = "用户头像", required = true, example = "http://www.iocoder.cn/xx.jpg")
        private String avatar;

    }

}

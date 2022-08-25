package cn.iocoder.yudao.module.system.controller.admin.auth.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@ApiModel(value = "管理后台 - 登录用户的权限信息 Response VO", description = "额外包括用户信息和角色列表")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthPermissionInfoRespVO {

    @ApiModelProperty(value = "用户信息", required = true)
    private UserVO user;

    @ApiModelProperty(value = "角色标识数组", required = true)
    private Set<String> roles;

    @ApiModelProperty(value = "操作权限数组", required = true)
    private Set<String> permissions;

    @ApiModel("用户信息 VO")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserVO {

        @ApiModelProperty(value = "用户编号", required = true, example = "1024")
        private Long id;

        @ApiModelProperty(value = "用户昵称", required = true, example = "芋道源码")
        private String nickname;

        @ApiModelProperty(value = "用户头像", required = true, example = "http://www.iocoder.cn/xx.jpg")
        private String avatar;

    }

}

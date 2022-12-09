package cn.iocoder.yudao.module.system.controller.admin.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Schema(title = "管理后台 - 登录用户的权限信息 Response VO", description = "额外包括用户信息和角色列表")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthPermissionInfoRespVO {

    @Schema(title = "用户信息", required = true)
    private UserVO user;

    @Schema(title = "角色标识数组", required = true)
    private Set<String> roles;

    @Schema(title = "操作权限数组", required = true)
    private Set<String> permissions;

    @Schema(title = "用户信息 VO")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserVO {

        @Schema(title = "用户编号", required = true, example = "1024")
        private Long id;

        @Schema(title = "用户昵称", required = true, example = "芋道源码")
        private String nickname;

        @Schema(title = "用户头像", required = true, example = "http://www.iocoder.cn/xx.jpg")
        private String avatar;

    }

}

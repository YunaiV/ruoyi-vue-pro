package cn.iocoder.yudao.module.system.controller.admin.oauth2.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(title = "管理后台 - OAuth2 获得用户基本信息 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2UserInfoRespVO {

    @Schema(title = "用户编号", required = true, example = "1")
    private Long id;

    @Schema(title = "用户账号", required = true, example = "芋艿")
    private String username;

    @Schema(title = "用户昵称", required = true, example = "芋道")
    private String nickname;

    @Schema(title = "用户邮箱", example = "yudao@iocoder.cn")
    private String email;
    @Schema(title = "手机号码", example = "15601691300")
    private String mobile;

    @Schema(title = "用户性别", example = "1", description = "参见 SexEnum 枚举类")
    private Integer sex;

    @Schema(title = "用户头像", example = "https://www.iocoder.cn/xxx.png")
    private String avatar;

    /**
     * 所在部门
     */
    private Dept dept;

    /**
     * 所属岗位数组
     */
    private List<Post> posts;

    @Schema(title = "部门")
    @Data
    public static class Dept {

        @Schema(title = "部门编号", required = true, example = "1")
        private Long id;

        @Schema(title = "部门名称", required = true, example = "研发部")
        private String name;

    }

    @Schema(title = "岗位")
    @Data
    public static class Post {

        @Schema(title = "岗位编号", required = true, example = "1")
        private Long id;

        @Schema(title = "岗位名称", required = true, example = "开发")
        private String name;

    }

}

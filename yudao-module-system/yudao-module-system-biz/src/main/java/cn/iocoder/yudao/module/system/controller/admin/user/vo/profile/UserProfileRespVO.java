package cn.iocoder.yudao.module.system.controller.admin.user.vo.profile;

import cn.iocoder.yudao.module.system.controller.admin.user.vo.user.UserBaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "管理后台 - 用户个人中心信息 Response VO")
public class UserProfileRespVO extends UserBaseVO {

    @Schema(description = "用户编号", required = true, example = "1")
    private Long id;

    @Schema(description = "状态,参见 CommonStatusEnum 枚举类", required = true, example = "1")
    private Integer status;

    @Schema(description = "最后登录 IP", required = true, example = "192.168.1.1")
    private String loginIp;

    @Schema(description = "最后登录时间", required = true, example = "时间戳格式")
    private LocalDateTime loginDate;

    @Schema(description = "创建时间", required = true, example = "时间戳格式")
    private LocalDateTime createTime;

    /**
     * 所属角色
     */
    private List<Role> roles;

    /**
     * 所在部门
     */
    private Dept dept;

    /**
     * 所属岗位数组
     */
    private List<Post> posts;
    /**
     * 社交用户数组
     */
    private List<SocialUser> socialUsers;

    @Schema(description = "角色")
    @Data
    public static class Role {

        @Schema(description = "角色编号", required = true, example = "1")
        private Long id;

        @Schema(description = "角色名称", required = true, example = "普通角色")
        private String name;

    }

    @Schema(description = "部门")
    @Data
    public static class Dept {

        @Schema(description = "部门编号", required = true, example = "1")
        private Long id;

        @Schema(description = "部门名称", required = true, example = "研发部")
        private String name;

    }

    @Schema(description = "岗位")
    @Data
    public static class Post {

        @Schema(description = "岗位编号", required = true, example = "1")
        private Long id;

        @Schema(description = "岗位名称", required = true, example = "开发")
        private String name;

    }

    @Schema(description = "社交用户")
    @Data
    public static class SocialUser {

        @Schema(description = "社交平台的类型,参见 SocialTypeEnum 枚举类", required = true, example = "10")
        private Integer type;

        @Schema(description = "社交用户的 openid", required = true, example = "IPRmJ0wvBptiPIlGEZiPewGwiEiE")
        private String openid;

    }

}

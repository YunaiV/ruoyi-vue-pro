package cn.iocoder.yudao.module.system.controller.admin.oauth2.vo.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@ApiModel("管理后台 - OAuth2 获得用户基本信息 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2UserInfoRespVO {

    @ApiModelProperty(value = "用户编号", required = true, example = "1")
    private Long id;

    @ApiModelProperty(value = "用户账号", required = true, example = "芋艿")
    private String username;

    @ApiModelProperty(value = "用户昵称", required = true, example = "芋道")
    private String nickname;

    @ApiModelProperty(value = "用户邮箱", example = "yudao@iocoder.cn")
    private String email;
    @ApiModelProperty(value = "手机号码", example = "15601691300")
    private String mobile;

    @ApiModelProperty(value = "用户性别", example = "1", notes = "参见 SexEnum 枚举类")
    private Integer sex;

    @ApiModelProperty(value = "用户头像", example = "https://www.iocoder.cn/xxx.png")
    private String avatar;

    /**
     * 所在部门
     */
    private Dept dept;

    /**
     * 所属岗位数组
     */
    private List<Post> posts;

    @ApiModel("部门")
    @Data
    public static class Dept {

        @ApiModelProperty(value = "部门编号", required = true, example = "1")
        private Long id;

        @ApiModelProperty(value = "部门名称", required = true, example = "研发部")
        private String name;

    }

    @ApiModel("岗位")
    @Data
    public static class Post {

        @ApiModelProperty(value = "岗位编号", required = true, example = "1")
        private Long id;

        @ApiModelProperty(value = "岗位名称", required = true, example = "开发")
        private String name;

    }

}

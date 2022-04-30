package cn.iocoder.yudao.module.system.controller.admin.user.vo.user;

import cn.iocoder.yudao.framework.common.validation.Mobile;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Set;

/**
 * 用户 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class UserBaseVO {

    @ApiModelProperty(value = "用户账号", required = true, example = "yudao")
    @NotBlank(message = "用户账号不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{4,30}$", message = "用户账号由 数字、字母 组成")
    @Size(min = 4, max = 30, message = "用户账号长度为 4-30 个字符")
    private String username;

    @ApiModelProperty(value = "用户昵称", required = true, example = "芋艿")
    @Size(max = 30, message = "用户昵称长度不能超过30个字符")
    private String nickname;

    @ApiModelProperty(value = "备注", example = "我是一个用户")
    private String remark;

    @ApiModelProperty(value = "部门ID", example = "我是一个用户")
    private Long deptId;

    @ApiModelProperty(value = "岗位编号数组", example = "1")
    private Set<Long> postIds;

    @ApiModelProperty(value = "用户邮箱", example = "yudao@iocoder.cn")
    @Email(message = "邮箱格式不正确")
    @Size(max = 50, message = "邮箱长度不能超过 50 个字符")
    private String email;

    @ApiModelProperty(value = "手机号码", example = "15601691300")
    @Mobile
    private String mobile;

    @ApiModelProperty(value = "用户性别", example = "1", notes = "参见 SexEnum 枚举类")
    private Integer sex;

    @ApiModelProperty(value = "用户头像", example = "https://www.iocoder.cn/xxx.png")
    private String avatar;

}

package cn.iocoder.yudao.module.system.controller.admin.mail.vo.account;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

/**
 * 邮箱账号 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class MailAccountBaseVO {

    @ApiModelProperty(value = "邮箱", required = true, example = "yudaoyuanma@123.com")
    @NotNull(message = "邮箱不能为空")
    @Email(message = "必须是 Email 格式")
    private String mail;

    @ApiModelProperty(value = "用户名", required = true, example = "yudao")
    @NotNull(message = "用户名不能为空")
    private String username;

    @ApiModelProperty(value = "密码", required = true, example = "123456")
    @NotNull(message = "密码必填")
    private String password;

    @ApiModelProperty(value = "SMTP 服务器域名", required = true, example = "www.iocoder.cn")
    @NotNull(message = "SMTP 服务器域名不能为空")
    private String host;

    @ApiModelProperty(value = "SMTP 服务器端口", required = true, example = "80")
    @NotNull(message = "SMTP 服务器端口不能为空")
    private Integer port;

    @ApiModelProperty(value = "是否开启 ssl", required = true, example = "true")
    @NotNull(message = "是否开启 ssl 必填")
    private Boolean sslEnable;

}

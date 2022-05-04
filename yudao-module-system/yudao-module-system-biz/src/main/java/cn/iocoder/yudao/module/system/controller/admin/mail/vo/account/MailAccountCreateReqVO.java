package cn.iocoder.yudao.module.system.controller.admin.mail.vo.account;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@ApiModel("管理后台 - 邮箱账号创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MailAccountCreateReqVO extends MailAccountBaseVO {

    @ApiModelProperty(value = "密码",required = true,example = "123456")
    @NotNull(message = "密码必填")
    private String password;
}

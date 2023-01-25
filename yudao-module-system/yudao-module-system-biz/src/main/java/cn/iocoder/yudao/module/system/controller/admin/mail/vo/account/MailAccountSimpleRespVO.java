package cn.iocoder.yudao.module.system.controller.admin.mail.vo.account;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("管理后台 - 邮箱账号的精简 Response VO")
@Data
public class MailAccountSimpleRespVO {

    @ApiModelProperty(value = "邮箱比那好", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "邮箱", required = true, example = "768541388@qq.com")
    private String mail;

}

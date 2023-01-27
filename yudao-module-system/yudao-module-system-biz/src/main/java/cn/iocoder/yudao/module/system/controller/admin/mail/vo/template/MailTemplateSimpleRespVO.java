package cn.iocoder.yudao.module.system.controller.admin.mail.vo.template;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("管理后台 - 邮件模版的精简 Response VO")
@Data
public class MailTemplateSimpleRespVO {

    @ApiModelProperty(value = "模版编号", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "模版名字", required = true, example = "哒哒哒")
    private String name;

}

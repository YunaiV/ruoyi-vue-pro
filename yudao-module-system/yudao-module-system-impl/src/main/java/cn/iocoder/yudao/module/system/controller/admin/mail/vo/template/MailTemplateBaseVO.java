package cn.iocoder.yudao.module.system.controller.admin.mail.vo.template;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@ApiModel("管理后台 - 邮箱模版基类 Base VO")
@Data
public class MailTemplateBaseVO {
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("标识")
    @NotNull(message = "邮箱模版code不能为空")
    private String code;

    @ApiModelProperty("发件人")
    private String username;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("内容")
    private String content;

    @ApiModelProperty("状态")
    private String status;

    @ApiModelProperty("备注")
    private String remark;
}

package cn.iocoder.yudao.module.system.controller.admin.mail.vo.template;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@ApiModel("管理后台 - 邮箱模版基类 Base VO")
@Data
public class MailTemplateBaseVO {
    @ApiModelProperty("主键")
    @NotNull(message = "主键不能为空")
    private Long id;

    @ApiModelProperty("名称")
    @NotNull(message = "名称不能为空")
    private String name;

    @ApiModelProperty("标识")
    @NotNull(message = "邮箱模版code不能为空")
    private String code;

    @ApiModelProperty("发件人")
    @NotNull(message = "发件人不能为空")
    @Email(message = "发件人格式有误")
    private String username;

    @ApiModelProperty("标题")
    @NotNull(message = "标题不能为空")
    private String title;

    @ApiModelProperty("内容")
    @NotNull(message = "内容不能为空")
    private String content;

    @ApiModelProperty("状态")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @ApiModelProperty("备注")
    private String remark;
}

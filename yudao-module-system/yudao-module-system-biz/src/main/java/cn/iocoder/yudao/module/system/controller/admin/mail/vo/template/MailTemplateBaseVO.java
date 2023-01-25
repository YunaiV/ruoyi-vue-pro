package cn.iocoder.yudao.module.system.controller.admin.mail.vo.template;

import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailAccountDO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 邮件模版 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class MailTemplateBaseVO {

    @ApiModelProperty(value = "模版名称", required = true, example = "测试名字")
    @NotNull(message = "名称不能为空")
    private String name;

    @ApiModelProperty(value = "模版编号", required = true, example = "test")
    @NotNull(message = "模版编号不能为空")
    private String code;

    @ApiModelProperty(value = "发送的邮箱账号编号", required = true, example = "1")
    @NotNull(message = "发送的邮箱账号编号不能为空")
    private Long accountId;

    @ApiModelProperty(value = "发送人名称", example = "芋头")
    private String nickname;

    @ApiModelProperty(value = "标题", required = true, example = "注册成功")
    @NotEmpty(message = "标题不能为空")
    private String title;

    @ApiModelProperty(value = "内容", required = true, example = "你好，注册成功啦")
    @NotEmpty(message = "内容不能为空")
    private String content;

    @ApiModelProperty(value = "状态", required = true, example = "1", notes = "参见 CommonStatusEnum 枚举")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @ApiModelProperty(value = "备注", example = "奥特曼")
    private String remark;

}

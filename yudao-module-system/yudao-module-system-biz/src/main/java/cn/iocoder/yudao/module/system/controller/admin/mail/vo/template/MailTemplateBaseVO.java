package cn.iocoder.yudao.module.system.controller.admin.mail.vo.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 邮件模版 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class MailTemplateBaseVO {

    @Schema(description = "模版名称", required = true, example = "测试名字")
    @NotNull(message = "名称不能为空")
    private String name;

    @Schema(description = "模版编号", required = true, example = "test")
    @NotNull(message = "模版编号不能为空")
    private String code;

    @Schema(description = "发送的邮箱账号编号", required = true, example = "1")
    @NotNull(message = "发送的邮箱账号编号不能为空")
    private Long accountId;

    @Schema(description = "发送人名称", example = "芋头")
    private String nickname;

    @Schema(description = "标题", required = true, example = "注册成功")
    @NotEmpty(message = "标题不能为空")
    private String title;

    @Schema(description = "内容", required = true, example = "你好，注册成功啦")
    @NotEmpty(message = "内容不能为空")
    private String content;

    @Schema(description = "状态 - 参见 CommonStatusEnum 枚举", required = true, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注", example = "奥特曼")
    private String remark;

}

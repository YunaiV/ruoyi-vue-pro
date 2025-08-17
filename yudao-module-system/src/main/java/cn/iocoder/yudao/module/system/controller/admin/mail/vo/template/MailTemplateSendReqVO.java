package cn.iocoder.yudao.module.system.controller.admin.mail.vo.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - 邮件发送 Req VO")
@Data
public class MailTemplateSendReqVO {

    @Schema(description = "接收邮箱", requiredMode = Schema.RequiredMode.REQUIRED, example = "[user1@example.com, user2@example.com]")
    @NotEmpty(message = "接收邮箱不能为空")
    private List<String> toMails;

    @Schema(description = "抄送邮箱", requiredMode = Schema.RequiredMode.REQUIRED, example = "[user3@example.com, user4@example.com]")
    private List<String> ccMails;

    @Schema(description = "密送邮箱", requiredMode = Schema.RequiredMode.REQUIRED, example = "[user5@example.com, user6@example.com]")
    private List<String> bccMails;

    @Schema(description = "模板编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "test_01")
    @NotNull(message = "模板编码不能为空")
    private String templateCode;

    @Schema(description = "模板参数")
    private Map<String, Object> templateParams;

}

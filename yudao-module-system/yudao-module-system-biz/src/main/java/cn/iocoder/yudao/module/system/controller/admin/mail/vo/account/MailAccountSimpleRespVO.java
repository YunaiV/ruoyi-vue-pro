package cn.iocoder.yudao.module.system.controller.admin.mail.vo.account;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 邮箱账号的精简 Response VO")
@Data
public class MailAccountSimpleRespVO {

    @Schema(description = "邮箱编号", required = true, example = "1024")
    private Long id;

    @Schema(description = "邮箱", required = true, example = "768541388@qq.com")
    private String mail;

}

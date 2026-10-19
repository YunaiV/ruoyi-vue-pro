package cn.iocoder.yudao.module.oa.controller.admin.mail.vo.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 邮件目录 Response VO")
@Data
public class OaMailFolderRespVO {

    @Schema(description = "目录查询标识", example = "INBOX")
    private String key;

    @Schema(description = "显示名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "收件箱")
    private String name;

    @Schema(description = "未读数量", example = "1")
    private Long unreadCount;

}

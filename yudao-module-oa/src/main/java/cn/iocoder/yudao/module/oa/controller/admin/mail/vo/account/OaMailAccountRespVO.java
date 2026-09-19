package cn.iocoder.yudao.module.oa.controller.admin.mail.vo.account;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "管理后台 - 企业邮箱账号 Response VO")
public class OaMailAccountRespVO {

    @Schema(description = "账号编号", example = "1024")
    private Long id;

    @Schema(description = "服务配置编号", example = "1024")
    private Long providerId;

    @Schema(description = "邮箱地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "user@example.com")
    private String mail;

    @Schema(description = "归属人姓名", example = "张三")
    private String userName;

    @Schema(description = "登录用户名", example = "user@example.com")
    private String username;

    @Schema(description = "是否默认发件账号", example = "false")
    private Boolean defaultStatus;

    @Schema(description = "状态", example = "0")
    private Integer status;

}

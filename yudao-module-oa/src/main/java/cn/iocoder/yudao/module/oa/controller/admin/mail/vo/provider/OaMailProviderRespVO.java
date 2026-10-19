package cn.iocoder.yudao.module.oa.controller.admin.mail.vo.provider;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 邮箱服务配置 Response VO")
@Data
public class OaMailProviderRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "企业邮箱")
    private String name;

    @Schema(description = "IMAP 连接配置")
    private OaMailProviderSaveReqVO.ConnectionConfig imap;

    @Schema(description = "SMTP 连接配置")
    private OaMailProviderSaveReqVO.ConnectionConfig smtp;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

}

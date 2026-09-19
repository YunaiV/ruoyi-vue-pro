package cn.iocoder.yudao.module.oa.controller.admin.mail.vo.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 企业邮箱邮件 Response VO")
@Data
public class OaMailMessageRespVO {

    @Schema(description = "邮件编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "邮箱账号编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long accountId;

    @Schema(description = "文件夹编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long folderId;

    @Schema(description = "远端邮件 UID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    private Long uid;

    @Schema(description = "远端 UID 有效期", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long uidValidity;

    @Schema(description = "邮件主题", requiredMode = Schema.RequiredMode.REQUIRED, example = "项目评审会议通知")
    private String subject;

    @Schema(description = "发件人", example = "sender@example.com")
    private String sender;

    @Schema(description = "收件人", example = "[\"recipient@example.com\"]")
    private List<String> recipients;

    @Schema(description = "抄送人", example = "[\"cc@example.com\"]")
    private List<String> ccs;

    @Schema(description = "回复地址", example = "[\"reply@example.com\"]")
    private List<String> replyTos;

    @Schema(description = "接收时间", type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime receiveTime;

    @Schema(description = "是否已读", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean readStatus;

    @Schema(description = "是否有附件", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean hasAttach;

    @Schema(description = "邮件大小（字节）", example = "1024")
    private Integer size;

    @Schema(description = "安全 HTML 正文", example = "<p>请查收本周工作安排。</p>")
    private String content;

    @Schema(description = "附件信息")
    private List<Attachment> attachments;

    @Schema(description = "管理后台 - 邮件附件 Response VO")
    @Data
    public static class Attachment {

        @Schema(description = "MIME 部件路径", example = "1.2")
        private String part;

        @Schema(description = "附件名称", example = "办公用品盘点结果.pdf")
        private String name;

        @Schema(description = "附件大小（字节）", example = "1024")
        private Integer size;

    }

}

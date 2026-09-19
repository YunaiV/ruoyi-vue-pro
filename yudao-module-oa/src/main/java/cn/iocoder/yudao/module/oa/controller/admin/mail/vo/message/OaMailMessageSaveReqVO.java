package cn.iocoder.yudao.module.oa.controller.admin.mail.vo.message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.web.multipart.MultipartFile;
import cn.iocoder.yudao.module.oa.dal.dataobject.mail.OaMailMessageDO;
import java.util.List;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.oa.enums.mail.OaMailComposeModeEnum;

@Schema(description = "管理后台 - 写信及草稿 Request VO")
@Data
public class OaMailMessageSaveReqVO {

    @Schema(description = "邮箱账号编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "邮箱账号编号不能为空")
    private Long accountId;

    @Schema(description = "原草稿编号", example = "1024")
    private Long draftId;

    @Schema(description = "收件人", example = "[\"recipient@example.com\"]")
    private List<@NotBlank(message = "收件人地址不能为空") String> recipients;

    @Schema(description = "抄送人", example = "[\"cc@example.com\"]")
    private List<@NotBlank(message = "抄送人地址不能为空") String> ccs;

    @Schema(description = "主题", example = "项目评审会议通知")
    @Size(max = 65535, message = "邮件主题不能超过 65535 个字符")
    private String subject;

    @Schema(description = "正文 HTML", example = "<p>请查收本周工作安排。</p>")
    @Size(max = 500000, message = "正文 HTML长度不能超过 {max} 个字符")
    private String content;

    @Schema(description = "回复或转发的原邮件编号", example = "1024")
    private Long sourceId;

    @Schema(description = "写信方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "new")
    @InEnum(value = OaMailComposeModeEnum.class, message = "写信方式必须是 {value}")
    @NotBlank(message = "写信方式不能为空")
    private String mode;

    @Schema(description = "保留的原邮件附件路径；空列表表示移除全部原附件")
    private List<String> attachmentParts;

    @Schema(description = "原邮件附件，仅用于写信回显", accessMode = Schema.AccessMode.READ_ONLY)
    private List<OaMailMessageDO.Attachment> attachments;

    @JsonIgnore
    @Schema(hidden = true)
    private List<MultipartFile> files;

}

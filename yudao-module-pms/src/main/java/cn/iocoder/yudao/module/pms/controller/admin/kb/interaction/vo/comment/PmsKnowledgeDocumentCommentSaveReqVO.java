package cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - PMS 知识库文档评论保存 Request VO")
@Data
public class PmsKnowledgeDocumentCommentSaveReqVO {

    @Schema(description = "文档编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "文档编号不能为空")
    private Long documentId;

    @Schema(description = "主评论编号", example = "0")
    private Long mainId;

    @Schema(description = "回复对象用户编号", example = "1")
    private Long replyUserId;

    @Schema(description = "评论内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "文档内容很清楚")
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 2000, message = "评论内容不能超过 2000 个字符")
    private String content;

}

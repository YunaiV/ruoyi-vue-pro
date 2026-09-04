package cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - PMS 知识库文档评论 Response VO")
@Data
public class PmsKnowledgeDocumentCommentRespVO {

    @Schema(description = "评论编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "文档编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long documentId;

    @Schema(description = "评论人用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "评论人昵称", example = "芋道")
    private String userName;

    @Schema(description = "主评论编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Long mainId;

    @Schema(description = "回复对象用户编号", example = "2")
    private Long replyUserId;

    @Schema(description = "回复对象昵称", example = "源码")
    private String replyUserName;

    @Schema(description = "评论内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "回复列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<PmsKnowledgeDocumentCommentRespVO> children;

}

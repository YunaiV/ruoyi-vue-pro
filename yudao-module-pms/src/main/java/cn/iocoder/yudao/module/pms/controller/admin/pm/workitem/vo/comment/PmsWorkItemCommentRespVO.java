package cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - PMS 工作项评论 Response VO")
@Data
public class PmsWorkItemCommentRespVO {

    @Schema(description = "评论编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "工作项编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long workItemId;

    @Schema(description = "评论人用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long userId;

    @Schema(description = "评论人姓名", example = "芋道源码")
    private String userName;

    @Schema(description = "主评论编号", example = "1024")
    private Long mainId;

    @Schema(description = "回复对象用户编号", example = "2048")
    private Long replyUserId;

    @Schema(description = "回复对象姓名", example = "源码")
    private String replyUserName;

    @Schema(description = "评论内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "回复列表")
    private List<PmsWorkItemCommentRespVO> children;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}

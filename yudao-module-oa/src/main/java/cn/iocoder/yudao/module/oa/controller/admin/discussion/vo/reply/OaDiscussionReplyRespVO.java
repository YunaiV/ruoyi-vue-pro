package cn.iocoder.yudao.module.oa.controller.admin.discussion.vo.reply;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - OA 讨论回复 Response VO")
@Data
public class OaDiscussionReplyRespVO {

    @Schema(description = "回复编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "讨论编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long discussionId;

    @Schema(description = "回复人用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "回复人用户昵称", example = "芋道源码")
    private String userName;

    @Schema(description = "父回复编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Long parentId;

    @Schema(description = "被回复人用户编号", example = "2")
    private Long replyUserId;

    @Schema(description = "被回复人用户昵称", example = "管理员")
    private String replyUserName;

    @Schema(description = "回复内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "已收到，将按时完成。")
    private String content;

    @Schema(description = "点赞数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer likeCount;

    @Schema(description = "当前用户是否已点赞", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean liked;

    @Schema(description = "点赞人用户昵称列表", example = "[\"张三\", \"李四\"]")
    private List<String> likeUserNames;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime createTime;

    @Schema(description = "楼层内的子回复")
    private List<OaDiscussionReplyRespVO> children;

}

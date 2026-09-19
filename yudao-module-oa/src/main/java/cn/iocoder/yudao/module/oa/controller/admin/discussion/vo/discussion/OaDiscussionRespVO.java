package cn.iocoder.yudao.module.oa.controller.admin.discussion.vo.discussion;

import cn.iocoder.yudao.module.oa.controller.admin.discussion.vo.vote.OaVoteOptionRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - OA 讨论 Response VO")
@Data
public class OaDiscussionRespVO {

    @Schema(description = "讨论编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "发布人用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "发布人用户昵称", example = "芋道源码")
    private String userName;

    @Schema(description = "讨论类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "下周团队会议时间征集")
    private String title;

    @Schema(description = "内容", example = "请大家反馈下周团队会议的时间安排。")
    private String content;

    @Schema(description = "附件地址列表", example = "[\"https://example.com/file.pdf\"]")
    private List<String> fileUrls;

    @Schema(description = "访问次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer visitCount;

    @Schema(description = "回复数", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Integer replyCount;

    @Schema(description = "点赞数", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer likeCount;

    @Schema(description = "当前用户是否已点赞", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean liked;

    @Schema(description = "点赞人用户昵称列表", example = "[\"张三\", \"李四\"]")
    private List<String> likeUserNames;

    @Schema(description = "投票是否允许多选", example = "false")
    private Boolean voteMultiple;

    @Schema(description = "投票开始时间", type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime voteStartTime;

    @Schema(description = "投票结束时间", type = "integer", format = "int64", example = "1789380000000")
    private LocalDateTime voteEndTime;

    @Schema(description = "投票选项列表")
    private List<OaVoteOptionRespVO> voteOptions;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime createTime;

}

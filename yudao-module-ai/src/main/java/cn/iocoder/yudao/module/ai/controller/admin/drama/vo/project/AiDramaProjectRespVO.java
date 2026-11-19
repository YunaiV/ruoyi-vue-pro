package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.project;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - AI 短剧项目 Response VO")
@Data
public class AiDramaProjectRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "用户昵称", example = "芋道源码")
    private String userNickname;

    @Schema(description = "项目名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "末班地铁")
    private String name;

    @Schema(description = "项目简介", example = "都市悬疑短剧")
    private String description;

    @Schema(description = "故事想法", example = "末班地铁上……")
    private String idea;

    @Schema(description = "画风", example = "2")
    private Integer style;

    @Schema(description = "美学技能编号", example = "1")
    private Long aestheticSkillId;

    @Schema(description = "导演技能编号", example = "2")
    private Long directorSkillId;

    @Schema(description = "分镜技能编号：有序")
    private List<Long> storyboardSkillIds;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer status;

    @Schema(description = "是否公开", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean publicStatus;

    @Schema(description = "画幅：项目的设置，成片使用", example = "9:16")
    private String composeAspectRatio;

    @Schema(description = "转场类型", example = "1")
    private Integer composeTransitionType;

    @Schema(description = "转场时长（秒）", example = "0.5")
    private Double composeTransitionDurationSeconds;

    @Schema(description = "成片状态", example = "40")
    private Integer composeStatus;

    @Schema(description = "合成进度（百分比）", example = "100")
    private Integer composeProgress;

    @Schema(description = "成片地址", example = "https://www.iocoder.cn/1.mp4")
    private String composeFileUrl;

    @Schema(description = "成片时长（秒）", example = "42.5")
    private Double composeDuration;

    @Schema(description = "成片镜头数：成片实际拼接的", example = "2")
    private Integer composeStoryboardCount;

    @Schema(description = "合成失败原因", example = "FFmpeg 执行失败")
    private String composeErrorMessage;

    @Schema(description = "合成完成时间")
    private LocalDateTime composeFinishTime;

    @Schema(description = "成片是否需要重新合成：成片使用的镜头视频被替换、镜头被删除，或之后新增了已完成视频的镜头；只在项目详情返回", example = "false")
    private Boolean composeStale;

    @Schema(description = "需要重新合成的原因：只在项目详情返回", example = "镜头视频已更换")
    private String composeStaleReason;

    @Schema(description = "镜头数", example = "8")
    private Integer storyboardCount;

    @Schema(description = "已完成视频的镜头数", example = "6")
    private Integer storyboardSuccessCount;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

}

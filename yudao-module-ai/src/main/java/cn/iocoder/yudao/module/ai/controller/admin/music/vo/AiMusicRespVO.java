package cn.iocoder.yudao.module.ai.controller.admin.music.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - AI 音乐 Response VO")
@Data
public class AiMusicRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10245")
    private Long id;

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "用户昵称", example = "芋道源码")
    private String userNickname;

    @Schema(description = "生成模式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer generateMode;

    @Schema(description = "平台", requiredMode = Schema.RequiredMode.REQUIRED, example = "SUNO")
    private String platform;

    @Schema(description = "模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31")
    private Long modelId;

    @Schema(description = "模型标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "chirp-crow")
    private String model;

    @Schema(description = "API 密钥编号（仅管理端）", example = "12")
    private Long apiKeyId;

    @Schema(description = "API 密钥名称（仅管理端）", example = "Suno 中转（new-api）")
    private String apiKeyName;

    @Schema(description = "音乐名称", example = "夏夜晚风")
    private String title;

    @Schema(description = "歌曲描述（灵感模式）", example = "一首关于毕业旅行的轻快流行歌")
    private String prompt;

    @Schema(description = "歌词", example = "[Verse]\n晚风吹过街角的路灯")
    private String lyric;

    @Schema(description = "音乐风格", example = "pop,female vocals,summer")
    private String tags;

    @Schema(description = "是否纯音乐", example = "false")
    private Boolean makeInstrumental;

    @Schema(description = "来源音乐编号（续写、拼接）", example = "10245")
    private Long refMusicId;

    @Schema(description = "续写起点（秒）", example = "176")
    private Integer extendSeconds;

    @Schema(description = "封面地址", example = "https://www.iocoder.cn/cover.png")
    private String coverUrl;

    @Schema(description = "音频地址", example = "https://www.iocoder.cn/audio.mp3")
    private String audioUrl;

    @Schema(description = "视频地址（中转返回，可能为空）", example = "https://www.iocoder.cn/video.mp4")
    private String videoUrl;

    @Schema(description = "时长（秒）", example = "176.4")
    private Double duration;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "40")
    private Integer status;

    @Schema(description = "进度（0-100）", example = "62")
    private Integer progress;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "是否公开", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean publicStatus;

    @Schema(description = "播放次数", example = "12")
    private Integer playCount;

    @Schema(description = "供应商任务编号（仅管理端）", example = "7f3c2b0e")
    private String taskId;

    @Schema(description = "供应商歌曲编号（仅管理端）", example = "5d1be7c0")
    private String clipId;

    @Schema(description = "完成时间")
    private LocalDateTime finishTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}

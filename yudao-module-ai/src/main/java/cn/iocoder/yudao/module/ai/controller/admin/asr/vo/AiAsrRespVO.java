package cn.iocoder.yudao.module.ai.controller.admin.asr.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - AI 语音识别 Response VO")
@Data
public class AiAsrRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "用户昵称（仅管理端）", example = "芋艿")
    private String userNickname;

    @Schema(description = "模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long modelId;

    @Schema(description = "模型名字", example = "腾讯云一句话识别")
    private String modelName;

    @Schema(description = "平台", requiredMode = Schema.RequiredMode.REQUIRED, example = "TENCENT")
    private String platform;

    @Schema(description = "模型标识", example = "16k_zh")
    private String model;

    @Schema(description = "音频文件名", requiredMode = Schema.RequiredMode.REQUIRED, example = "meeting.wav")
    private String fileName;

    @Schema(description = "音频地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/1.wav")
    private String audioUrl;

    @Schema(description = "识别状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Integer status;

    @Schema(description = "识别出的原文", example = "芋道源码")
    private String originalText;

    @Schema(description = "失败原因", example = "音频不正确：音频必须是 WAV 格式")
    private String errorMessage;

    @Schema(description = "识别耗时，单位：毫秒", example = "1200")
    private Integer duration;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

}

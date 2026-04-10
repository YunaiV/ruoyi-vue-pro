package cn.iocoder.yudao.module.ai.controller.admin.write.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - AI 写作 Response VO")
@Data
public class AiWriteRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "5311")
    private Long id;

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "28404")
    private Long userId;

    @Schema(description = "写作类型", example = "1")
    private Integer type;

    @Schema(description = "平台", requiredMode = Schema.RequiredMode.REQUIRED, example = "TongYi")
    private String platform;

    @Schema(description = "模型", requiredMode = Schema.RequiredMode.REQUIRED, example = "qwen")
    private String model;

    @Schema(description = "生成内容提示", requiredMode = Schema.RequiredMode.REQUIRED, example = "撰写：田忌赛马")
    private String prompt;

    @Schema(description = "生成的内容", example = "你非常不错")
    private String generatedContent;

    @Schema(description = "原文", example = "真的么？")
    private String originalContent;

    @Schema(description = "长度提示词", example = "1")
    private Integer length;

    @Schema(description = "格式提示词", example = "2")
    private Integer format;

    @Schema(description = "语气提示词", example = "3")
    private Integer tone;

    @Schema(description = "语言提示词", example = "4")
    private Integer language;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
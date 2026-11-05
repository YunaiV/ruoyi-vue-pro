package cn.iocoder.yudao.module.ai.controller.admin.mindmap.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - AI 思维导图生成 Response VO")
@Data
public class AiMindMapGenerateRespVO {

    @Schema(description = "思维导图编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "本次生成的内容片段", requiredMode = Schema.RequiredMode.REQUIRED, example = "# 主题")
    private String content;

    @Schema(description = "输入 Token，未知为空", example = "120")
    private Long inputTokens;

    @Schema(description = "输出 Token，未知为空", example = "80")
    private Long outputTokens;

    @Schema(description = "合计 Token，未知为空", example = "200")
    private Long totalTokens;

}

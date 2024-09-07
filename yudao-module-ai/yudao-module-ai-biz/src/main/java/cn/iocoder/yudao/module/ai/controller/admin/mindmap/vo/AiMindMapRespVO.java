package cn.iocoder.yudao.module.ai.controller.admin.mindmap.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - AI 思维导图 Response VO")
@Data
public class AiMindMapRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3373")
    private Long id;

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "4325")
    private Long userId;

    @Schema(description = "生成内容提示", requiredMode = Schema.RequiredMode.REQUIRED, example = "Java 学习路线")
    private String prompt;

    @Schema(description = "生成的思维导图内容")
    private String generatedContent;

    @Schema(description = "平台", requiredMode = Schema.RequiredMode.REQUIRED, example = "OpenAI")
    private String platform;

    @Schema(description = "模型", requiredMode = Schema.RequiredMode.REQUIRED, example = "gpt-3.5-turbo-0125")
    private String model;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
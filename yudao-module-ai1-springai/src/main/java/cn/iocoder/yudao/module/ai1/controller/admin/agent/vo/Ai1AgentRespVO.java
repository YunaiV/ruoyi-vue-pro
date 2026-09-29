package cn.iocoder.yudao.module.ai1.controller.admin.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - AI1 Agent Response VO")
@Data
public class Ai1AgentRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "客服助手")
    private String name;

    @Schema(description = "介绍", example = "解答产品使用问题")
    private String introduction;

    @Schema(description = "模型所属 Provider 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long providerId;

    @Schema(description = "模型所属 Provider 名称", example = "DeepSeek")
    private String providerName;

    @Schema(description = "对话模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long modelId;

    @Schema(description = "对话模型名称", example = "DeepSeek Chat")
    private String modelName;

    @Schema(description = "系统指令", example = "你是一名专业的客服")
    private String systemPrompt;

    @Schema(description = "知识库编号集合", example = "[1, 2]")
    private List<Long> knowledgeBaseIds;

    @Schema(description = "MCP 编号集合", example = "[1]")
    private List<Long> mcpIds;

    @Schema(description = "SKILL 编号集合", example = "[1]")
    private List<Long> skillIds;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

}

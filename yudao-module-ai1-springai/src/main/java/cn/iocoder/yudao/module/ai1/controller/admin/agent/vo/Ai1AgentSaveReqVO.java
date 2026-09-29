package cn.iocoder.yudao.module.ai1.controller.admin.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - AI1 Agent 新增/修改 Request VO")
@Data
public class Ai1AgentSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "客服助手")
    @NotEmpty(message = "名称不能为空")
    @Size(max = 100, message = "名称长度不能超过 100 个字符")
    private String name;

    @Schema(description = "介绍", example = "解答产品使用问题")
    @Size(max = 500, message = "介绍长度不能超过 500 个字符")
    private String introduction;

    @Schema(description = "模型所属 Provider 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "Provider 不能为空")
    private Long providerId;

    @Schema(description = "对话模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "对话模型不能为空")
    private Long modelId;

    @Schema(description = "系统指令", example = "你是一名专业的客服")
    private String systemPrompt;

    @Schema(description = "知识库编号集合", example = "[1, 2]")
    private List<Long> knowledgeBaseIds;

    @Schema(description = "MCP 编号集合", example = "[1]")
    private List<Long> mcpIds;

    @Schema(description = "SKILL 编号集合", example = "[1]")
    private List<Long> skillIds;

}

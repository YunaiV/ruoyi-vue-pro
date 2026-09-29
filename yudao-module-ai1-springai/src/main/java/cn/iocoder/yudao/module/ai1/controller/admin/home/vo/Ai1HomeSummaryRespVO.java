package cn.iocoder.yudao.module.ai1.controller.admin.home.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

// TODO @AI：stat？看看这 3 个 vo，按照项目的习惯，应该怎么命名噢
@Schema(description = "管理后台 - AI1 首页总量统计 Response VO")
@Data
public class Ai1HomeSummaryRespVO {

    @Schema(description = "Agent 数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "4")
    private Long agentCount;

    @Schema(description = "SKILL 数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Long skillCount;

    @Schema(description = "MCP 数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "4")
    private Long mcpCount;

    @Schema(description = "模型数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long modelCount;

}

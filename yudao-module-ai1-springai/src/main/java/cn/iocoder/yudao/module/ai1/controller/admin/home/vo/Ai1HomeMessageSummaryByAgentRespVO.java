package cn.iocoder.yudao.module.ai1.controller.admin.home.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - AI1 首页按 Agent 消息统计 Response VO")
@Data
public class Ai1HomeMessageSummaryByAgentRespVO {

    @Schema(description = "Agent 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long agentId;

    @Schema(description = "Agent 名称，Agent 已删除时为空", example = "Hi Agent")
    private String agentName;

    @Schema(description = "消息数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "36")
    private Long count;

}

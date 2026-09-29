package cn.iocoder.yudao.module.ai1.controller.admin.home.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

// TODO @AI：这个是 share 么？应该是 summary 把？
@Schema(description = "管理后台 - AI1 首页 Agent 消息占比 Response VO")
@Data
public class Ai1HomeMessageShareRespVO {

    @Schema(description = "Agent 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long agentId;

    @Schema(description = "Agent 名称，Agent 已删除时为 Agent#编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "Hi Agent")
    private String agentName;

    @Schema(description = "消息数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "36")
    private Long count;

}

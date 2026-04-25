package cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * AI 服装设计智能体聊天响应 VO
 *
 * <p>前端收到此响应后：</p>
 * <ol>
 *   <li>展示 {@link #agentReply}（智能体自然语言回复）</li>
 *   <li>通过 {@link #sseUrl} 建立SSE长连接，实时接收后续进度推送</li>
 *   <li>无需任何手动操作，等待SSE推送 {@code chain_done} 事件即可</li>
 * </ol>
 *
 * @author deepay
 */
@Data
@Builder
@Schema(description = "AI 服装设计智能体聊天响应")
public class AiFashionAgentChatRespVO {

    @Schema(description = "链路编号（全局唯一，用于SSE订阅和状态查询）", example = "CHAIN_A1B2C3D4")
    private String chainId;

    @Schema(description = "智能体自然语言回复（直接展示给用户）",
            example = "好的！我已理解您的需求，正在为您自动执行：生成5款甜酷风红色连衣裙 → 转3D → 旋转90°。全程自动完成，进度实时推送中…")
    private String agentReply;

    @Schema(description = "SSE进度订阅URL（前端用EventSource连接此URL）",
            example = "/admin-api/ai/fashion/agent/progress/CHAIN_A1B2C3D4")
    private String sseUrl;

    @Schema(description = "链路步骤列表（按执行顺序）")
    private List<StepSummary> steps;

    @Schema(description = "会话令牌（前端持久化，下次对话携带）")
    private String sessionToken;

    @Schema(description = "解析出的意图描述（给用户确认智能体是否理解正确）",
            example = "批量生成5款 + 3D转换 + 旋转查看")
    private String parsedIntentDesc;

    @Schema(description = "链路总进度 0-100")
    private Integer totalProgress;

    @Schema(description = "链路状态：PENDING/RUNNING/SUCCESS/FAIL")
    private String chainStatus;

    // ─────────────────────────────────────────────────────────────────────────
    // 内嵌 VO
    // ─────────────────────────────────────────────────────────────────────────

    @Data
    @Builder
    @Schema(description = "链路步骤摘要")
    public static class StepSummary {
        @Schema(description = "步骤顺序", example = "0")
        private int stepOrder;

        @Schema(description = "意图类型", example = "BATCH_GENERATE")
        private String intent;

        @Schema(description = "步骤描述", example = "批量生成5款甜酷风红色连衣裙")
        private String desc;

        @Schema(description = "步骤状态", example = "PENDING")
        private String status;

        @Schema(description = "步骤进度 0-100")
        private int progress;

        @Schema(description = "完成后的结果摘要（可能包含图片URL等）")
        private String resultSummary;
    }

}

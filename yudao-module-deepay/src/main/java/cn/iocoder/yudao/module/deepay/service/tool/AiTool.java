package cn.iocoder.yudao.module.deepay.service.tool;

import java.util.Map;

/**
 * AI 工具接口 — 所有工具均须实现此接口。
 *
 * <h3>工具定义要素</h3>
 * <ul>
 *   <li>{@link #getName()}        — 工具唯一名称（英文驼峰）</li>
 *   <li>{@link #getDescription()} — 工具自然语言描述（供 LLM 理解）</li>
 *   <li>{@link #getParamsSchema()} — 参数 JSON Schema（供 LLM 填参）</li>
 *   <li>{@link #getRiskLevel()}   — 风险等级（LOW / MEDIUM / HIGH）</li>
 *   <li>{@link #execute(ToolCallContext, Map)} — 实际执行逻辑</li>
 * </ul>
 */
public interface AiTool {

    /** 工具名称（唯一标识） */
    String getName();

    /** 工具描述（自然语言，供 LLM 理解） */
    String getDescription();

    /**
     * 参数 JSON Schema（简版，不依赖第三方库）。
     * 返回 Map 结构，字段：type/properties/required。
     */
    Map<String, Object> getParamsSchema();

    /** 风险等级 */
    RiskLevel getRiskLevel();

    /**
     * 执行工具。
     *
     * @param ctx    执行上下文（tenantId / customerId / sessionId 等）
     * @param params 经过 LLM 填充的参数
     * @return 执行结果（Map，可被序列化后返回给 LLM）
     * @throws ToolExecutionException 执行失败时抛出（含错误描述，LLM 可据此重试）
     */
    Map<String, Object> execute(ToolCallContext ctx, Map<String, Object> params) throws ToolExecutionException;

    // =========================================================================
    // 枚举
    // =========================================================================

    enum RiskLevel {
        /** 只读操作，无需确认 */
        LOW,
        /** 写入操作，建议告知用户 */
        MEDIUM,
        /** 高风险操作（退款/发货/改价/删除），必须二次确认 */
        HIGH
    }

}

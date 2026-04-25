package cn.iocoder.yudao.module.ai.service.image;

import lombok.Builder;
import lombok.Getter;

/**
 * AI 服装设计会话上下文（NLP 解析使用）
 *
 * <p>由 {@link AiFashionConversationServiceImpl} 从 {@code AiFashionSessionDO} 转换而来，
 * 传入 {@link AiFashionNlpParserService#parse} 以支持上下文感知的意图解析。</p>
 *
 * @author deepay
 */
@Getter
@Builder
public final class AiFashionSessionContext {

    /** 最近一次完整 SD Prompt（用于 MODIFY 类意图） */
    private final String lastPrompt;

    /** 当前颜色 SD 关键词 */
    private final String currentColors;

    /** 当前风格 SD 关键词 */
    private final String currentStyle;

    /** 当前面料 SD 关键词 */
    private final String currentFabric;

    /** 当前长度 SD 关键词 */
    private final String currentLength;

    /** 当前版型 SD 关键词 */
    private final String currentFit;

    /** 最近一次任务 ID */
    private final Long lastTaskId;

}

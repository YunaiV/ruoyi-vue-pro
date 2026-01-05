package cn.iocoder.yudao.module.ai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI 知识库文档切片策略枚举
 *
 * @author runzhen
 */
@AllArgsConstructor
@Getter
public enum AiDocumentSplitStrategyEnum {

    /**
     * 自动识别文档类型并选择最佳切片策略
     */
    AUTO("auto", "自动识别"),

    /**
     * 基于 Token 数量机械切分（默认策略）
     */
    TOKEN("token", "Token 切分"),

    /**
     * 按段落切分（以双换行符为分隔）
     */
    PARAGRAPH("paragraph", "段落切分"),

    /**
     * Markdown QA 格式专用切片器
     * 识别二级标题作为问题，保持问答对完整性
     * 长答案智能切分但保留问题作为上下文
     */
    MARKDOWN_QA("markdown_qa", "Markdown QA 切分"),

    /**
     * 语义化切分，保留句子完整性
     * 在段落和句子边界处切分，避免截断
     */
    SEMANTIC("semantic", "语义切分");

    /**
     * 策略代码
     */
    private final String code;

    /**
     * 策略名称
     */
    private final String name;

}

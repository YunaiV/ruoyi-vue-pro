package cn.iocoder.yudao.module.ai.enums.billing;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * AI 业务类型枚举
 *
 * @author 芋道源码
 */
@Getter
@RequiredArgsConstructor
public enum AiBizTypeEnum implements ArrayValuable<String> {

    CHAT_MESSAGE("CHAT_MESSAGE", "聊天消息"),
    WRITE("WRITE", "写作"),
    MIND_MAP("MIND_MAP", "思维导图"),
    KNOWLEDGE_EMBED("KNOWLEDGE_EMBED", "知识库向量化"),
    KNOWLEDGE_RERANK("KNOWLEDGE_RERANK", "知识库重排序"),
    WORKFLOW_RUN("WORKFLOW_RUN", "工作流运行");

    /**
     * 业务类型值
     */
    private final String type;
    /**
     * 业务类型名
     */
    private final String name;

    public static final String[] ARRAYS = Arrays.stream(values()).map(AiBizTypeEnum::getType).toArray(String[]::new);

    @Override
    public String[] array() {
        return ARRAYS;
    }

}

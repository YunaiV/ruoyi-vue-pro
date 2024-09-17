package cn.iocoder.yudao.module.ai.enums.knowledge;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 知识库-文档状态的枚举
 *
 * @author xiaoxin
 */
@AllArgsConstructor
@Getter
public enum AiKnowledgeDocumentStatusEnum implements IntArrayValuable {

    IN_PROGRESS(10, "索引中"),
    SUCCESS(20, "可用"),
    FAIL(30, "失败");

    /**
     * 状态
     */
    private final Integer status;

    /**
     * 状态名
     */
    private final String name;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(AiKnowledgeDocumentStatusEnum::getStatus).toArray();

    @Override
    public int[] array() {
        return ARRAYS;
    }

}

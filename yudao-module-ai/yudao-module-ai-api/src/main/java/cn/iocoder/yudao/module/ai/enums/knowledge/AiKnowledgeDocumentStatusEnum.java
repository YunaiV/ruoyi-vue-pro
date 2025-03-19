package cn.iocoder.yudao.module.ai.enums.knowledge;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
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
public enum AiKnowledgeDocumentStatusEnum implements ArrayValuable<Integer> {

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

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(AiKnowledgeDocumentStatusEnum::getStatus).toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}

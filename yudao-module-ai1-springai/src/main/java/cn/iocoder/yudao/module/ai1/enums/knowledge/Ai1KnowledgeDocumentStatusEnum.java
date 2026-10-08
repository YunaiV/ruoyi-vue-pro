package cn.iocoder.yudao.module.ai1.enums.knowledge;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI1 知识文档向量化状态的枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum Ai1KnowledgeDocumentStatusEnum implements ArrayValuable<Integer> {

    UNPROCESSED(0, "未处理"),
    VECTORIZED(1, "已向量化"),
    FAILED(2, "失败");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(Ai1KnowledgeDocumentStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}

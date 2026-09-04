package cn.iocoder.yudao.module.pms.enums.kb.content;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * PMS 知识库文档类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum PmsKnowledgeDocumentTypeEnum implements ArrayValuable<Integer> {

    RICH_TEXT(3, "富文本"),
    FILE(4, "文件");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(PmsKnowledgeDocumentTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static PmsKnowledgeDocumentTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}

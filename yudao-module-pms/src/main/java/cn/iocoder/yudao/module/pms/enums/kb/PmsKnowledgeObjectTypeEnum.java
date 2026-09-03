package cn.iocoder.yudao.module.pms.enums.kb;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * PMS 知识对象类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum PmsKnowledgeObjectTypeEnum implements ArrayValuable<Integer> {

    LIBRARY(1), // 知识库
    FOLDER(2), // 文件夹
    DOCUMENT(3), // 文档
    FILE(4); // 文件

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(PmsKnowledgeObjectTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static PmsKnowledgeObjectTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}

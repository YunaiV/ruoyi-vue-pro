package cn.iocoder.yudao.module.bpm.enums.definition;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * BPM 模型的类型的枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum BpmModelTypeEnum implements ArrayValuable<Integer> {

    BPMN(10, "BPMN 设计器"), // https://bpmn.io/toolkit/bpmn-js/
    SIMPLE(20, "SIMPLE 设计器"); // 参考钉钉、飞书工作流的设计器

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(BpmModelTypeEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
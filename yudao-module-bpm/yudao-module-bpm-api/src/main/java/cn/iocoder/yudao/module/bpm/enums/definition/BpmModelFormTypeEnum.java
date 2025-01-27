package cn.iocoder.yudao.module.bpm.enums.definition;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * BPM 模型的表单类型的枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum BpmModelFormTypeEnum implements ArrayValuable<Integer> {

    NORMAL(10, "流程表单"), // 对应 BpmFormDO
    CUSTOM(20, "业务表单") // 业务自己定义的表单，自己进行数据的存储
    ;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(BpmModelFormTypeEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}

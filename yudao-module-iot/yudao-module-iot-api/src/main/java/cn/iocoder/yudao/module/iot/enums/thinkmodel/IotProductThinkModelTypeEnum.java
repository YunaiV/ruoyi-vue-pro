package cn.iocoder.yudao.module.iot.enums.thinkmodel;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

// TODO @芋艿：纠结下，到底叫 thinkmodel 好，还是 function 好
/**
 * IOT 产品功能（物模型）类型枚举类
 *
 * @author ahh
 */
@AllArgsConstructor
@Getter
public enum IotProductThinkModelTypeEnum implements IntArrayValuable {

    PROPERTY(1, "属性"),
    SERVICE(2, "服务"),
    EVENT(3, "事件");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(IotProductThinkModelTypeEnum::getType).toArray();

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 描述
     */
    private final String description;

    public static IotProductThinkModelTypeEnum valueOfType(Integer type) {
        for (IotProductThinkModelTypeEnum value : values()) {
            if (value.getType().equals(type)) {
                return value;
            }
        }
        return null;
    }

    @Override
    public int[] array() {
        return ARRAYS;
    }

}

package cn.iocoder.yudao.module.iot.enums.product;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * IoT 产品脚本语言枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum IotProductScriptLanguageEnum implements ArrayValuable<String> {

    JAVASCRIPT("javascript", "JavaScript"),
    JAVA("java", "Java"),
    PYTHON("python", "Python"),
    ;

    public static final String[] ARRAYS = Arrays.stream(values()).map(IotProductScriptLanguageEnum::getCode)
            .toArray(String[]::new);

    /**
     * 编码
     */
    private final String code;
    /**
     * 名称
     */
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    public static IotProductScriptLanguageEnum getByCode(String code) {
        return Arrays.stream(values())
                .filter(type -> type.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}
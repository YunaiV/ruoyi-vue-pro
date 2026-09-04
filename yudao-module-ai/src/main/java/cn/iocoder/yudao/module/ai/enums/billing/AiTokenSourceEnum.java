package cn.iocoder.yudao.module.ai.enums.billing;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * AI Token 来源枚举
 *
 * @author 芋道源码
 */
@Getter
@RequiredArgsConstructor
public enum AiTokenSourceEnum implements ArrayValuable<String> {

    PROVIDER("PROVIDER", "厂商返回"),
    ESTIMATED("ESTIMATED", "估算"),
    NONE("NONE", "无");

    /**
     * 来源值
     */
    private final String source;
    /**
     * 来源名
     */
    private final String name;

    public static final String[] ARRAYS = Arrays.stream(values()).map(AiTokenSourceEnum::getSource).toArray(String[]::new);

    @Override
    public String[] array() {
        return ARRAYS;
    }

}

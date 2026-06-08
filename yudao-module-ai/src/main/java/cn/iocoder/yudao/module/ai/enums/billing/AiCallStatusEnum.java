package cn.iocoder.yudao.module.ai.enums.billing;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * AI 调用状态枚举
 *
 * @author 芋道源码
 */
@Getter
@RequiredArgsConstructor
public enum AiCallStatusEnum implements ArrayValuable<String> {

    SUCCESS("SUCCESS", "成功"),
    FAIL("FAIL", "失败"),
    CANCEL("CANCEL", "取消");

    /**
     * 状态值
     */
    private final String status;
    /**
     * 状态名
     */
    private final String name;

    public static final String[] ARRAYS = Arrays.stream(values()).map(AiCallStatusEnum::getStatus).toArray(String[]::new);

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
